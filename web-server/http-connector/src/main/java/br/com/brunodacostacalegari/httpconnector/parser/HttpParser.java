package br.com.brunodacostacalegari.httpconnector.parser;

import br.com.brunodacostacalegari.httpconnector.exception.HttpException;
import br.com.brunodacostacalegari.httpconnector.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpParser {
    private static final int MAX_BODY_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final int MAX_HEADER_SIZE = 8192; // 8 KB

    private static final Set<String> SUPPORTED_METHODS = Set.of(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE"
    );
    private static final Set<String> METHODS_WITH_BODY = Set.of(
            "POST",
            "PUT",
            "PATCH"
    );

    public HttpRequest parse(InputStream in, String remoteAddress, int remotePort) throws IOException {
        byte[] headerBytes = readUntilDoubleCrlf(in);

        String headerText = new String(headerBytes, StandardCharsets.ISO_8859_1);
        String trimmed = headerText.replaceAll("(\r\n)+$", "");
        String[] lines = trimmed.split("\r\n");

        if (lines.length == 0 || lines[0].isBlank()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Request line cannot be empty");
        }


        if (lines.length < 2 || lines[1].isBlank()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Header line cannot be empty");
        }

        String[] requestLineParts = lines[0].trim().split("\\s+", 3);
        if (requestLineParts.length != 3) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Invalid request line: " + lines[0]);
        }

        String method = requestLineParts[0].toUpperCase();

        if (!SUPPORTED_METHODS.contains(method)) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Unsupported HTTP method: " + method);
        }

        String fullPath = requestLineParts[1];

        if (!fullPath.startsWith("/")) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Invalid path: " + fullPath);
        }

        int idx = fullPath.indexOf('?');

        String path = idx >= 0 ? fullPath.substring(0, idx) : fullPath;
        String queryString = idx >= 0 ? fullPath.substring(idx + 1) : null;

        Map<String, List<String>> queryParams = new HashMap<>();

        if (queryString != null) {
            for (String pair : queryString.split("&")) {
                if (pair.isEmpty()) continue;

                String[] kv = pair.split("=", 2);

                try {
                    String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                    String value = kv.length > 1
                            ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                            : "";

                    queryParams.computeIfAbsent(key, _ -> new ArrayList<>()).add(value);

                } catch (IllegalArgumentException e) {
                    throw new HttpException(HttpStatus.BAD_REQUEST, "Invalid query encoding: " + e.getMessage());
                }
            }
        }


        String protocol = requestLineParts[2].toUpperCase(Locale.ROOT);

        if (!protocol.equals("HTTP/1.1")) {
            throw new HttpException(HttpStatus.HTTP_VERSION_NOT_SUPPORTED, "Unsupported HTTP version: " + protocol);
        }

        HttpHeaders headers = new HttpHeaders();

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isBlank()) continue;

            int colon = line.indexOf(":");
            if (colon <= 0) {
                throw new HttpException(HttpStatus.BAD_REQUEST, "Malformed header: " + line);
            }

            String name = line.substring(0, colon).trim();
            String value = line.substring(colon + 1).trim();

            headers.add(name, value);
        }

        if (!headers.contains("Host")) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Missing Host header");
        }

        if (headers.contains("Transfer-Encoding")) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Transfer-Encoding header not supported");
        }

        byte[] body = new byte[0];

        List<String> contentLengthValues = headers.get("Content-Length");

        if (contentLengthValues.size() > 1) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Multiple Content-Length not allowed");
        }

        String contentLength = contentLengthValues.isEmpty()
                ? null
                : contentLengthValues.getFirst();

        if (METHODS_WITH_BODY.contains(method) && contentLength == null) {
            throw new HttpException(HttpStatus.LENGTH_REQUIRED, "Content-Length required when method is " + method);
        }

        if (contentLength != null) {
            int length;

            try {
                length = Integer.parseInt(contentLength.trim());
            } catch (NumberFormatException e) {
                throw new HttpException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid Content-Length value: " + contentLength + " with error: " + e.getMessage()
                );
            }

            if (length < 0) {
                throw new HttpException(HttpStatus.BAD_REQUEST, "Negative Content-Length");
            }

            if (length > MAX_BODY_SIZE) {
                throw new HttpException(HttpStatus.PAYLOAD_TOO_LARGE, "Request body too large: " + length);
            }

            body = in.readNBytes(length);

            if (body.length != length) {
                throw new HttpException(
                        HttpStatus.BAD_REQUEST,
                        "Unexpected end of stream while reading body, body length: " + body.length + ", expected: " + length
                );
            }
        }

        return new HttpRequest(
                method, path, queryString, protocol,
                headers, queryParams, body,
                remoteAddress, remotePort
        );
    }

    private byte[] readUntilDoubleCrlf(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int curr;
        final int normalState = 0;
        final int singleCr = 1; // Saw '\r', expecting '\n'
        final int singleCrlf = 2; // Saw '\r\n', expecting '\r' for potential end of headers or more headers
        final int singleCrlfPlusCr = 3; // Saw '\r\n\r', expecting '\n' to confirm end of headers
        int state = normalState;

        while ((curr = in.read()) != -1) {
            buffer.write(curr);

            if (buffer.size() > MAX_HEADER_SIZE) {
                throw new HttpException(
                        HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE,
                        HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE.getMessage()
                );
            }

            switch (state) {
                case normalState -> {
                    if (curr == '\r') {
                        state = singleCr;
                    } else if (curr == '\n') {
                        throw new HttpException(
                                HttpStatus.BAD_REQUEST,
                                "Expected CRLF sequence, got LF without preceding CR"
                        );
                    }
                }
                case singleCr -> {
                    if (curr == '\n') {
                        state = singleCrlf;
                    } else {
                        throw new HttpException(HttpStatus.BAD_REQUEST, "Expected LF after CR, got: " + (char) curr);
                    }
                }
                case singleCrlf -> {
                    if (curr == '\r') {
                        state = singleCrlfPlusCr;
                    } else if (curr == '\n') {
                        throw new HttpException(
                                HttpStatus.BAD_REQUEST,
                                "Expected CRLF sequence, got LF without preceding CR"
                        );
                    } else {
                        state = normalState;
                    }
                }
                case singleCrlfPlusCr -> {
                    if (curr == '\n') {
                        return buffer.toByteArray();
                    } else {
                        throw new HttpException(HttpStatus.BAD_REQUEST, "Expected LF after CR, got: " + (char) curr);
                    }
                }
            }
        }

        throw new HttpException(
                HttpStatus.BAD_REQUEST,
                "Headers not terminated properly, expected CRLF sequence at the end of headers"
        );
    }
}