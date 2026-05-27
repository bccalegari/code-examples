package br.com.brunodacostacalegari.httpconnector.model;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpResponse {
    private int statusCode = 200;
    private String statusMessage = "OK";

    private final HttpHeaders headers = new HttpHeaders();
    private byte[] body = new byte[0];

    public HttpResponse() {
        setInternalHeader("Connection", "close");
        setInternalHeader("Content-Type", "text/plain; charset=UTF-8");
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatus(int code) {
        if (code < 100 || code > 599) {
            throw new IllegalArgumentException("Invalid HTTP status code");
        }
        this.statusCode = code;
    }

    public void setStatus(int code, String message) {
        setStatus(code);
        this.statusMessage = (message == null || message.isBlank()) ? "" : message;
    }

    public void setHeader(String name, String value) {
        validateInternalHeaders(name, value);

        if (name.equalsIgnoreCase("Content-Type") && !value.toLowerCase(Locale.ROOT).contains("charset")) {
            value = value + "; charset=UTF-8";
        }

        headers.set(name, value);
    }

    public void addHeader(String name, String value) {
        validateInternalHeaders(name, value);

        if (name.equalsIgnoreCase("Content-Type") && headers.contains(name)) {
            throw new IllegalArgumentException("The 'Content-Type' header does not support multiple values.");
        }

        headers.add(name, value);
    }

    public void setBody(byte[] body) {
        this.body = body == null ? new byte[0] : body.clone();
    }

    public void setBody(String body) {
        setBody(body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8));
    }

    public void write(OutputStream out) throws IOException {
        setInternalHeader("Content-Length", String.valueOf(body.length));

        out.write(("HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n")
                .getBytes(StandardCharsets.ISO_8859_1));

        for (Map.Entry<String, List<String>> entry : headers.entries()) {
            for (String value : entry.getValue()) {
                byte[] headerBytes = (headers.getOriginalName(entry.getKey()) + ": " + value + "\r\n")
                        .getBytes(StandardCharsets.ISO_8859_1);
                out.write(headerBytes);
            }
        }

        out.write("\r\n".getBytes(StandardCharsets.ISO_8859_1));

        out.write(body);
        out.flush();
    }

    private void setInternalHeader(String name, String value) {
        headers.set(name, value);
    }

    private void validateInternalHeaders(String name, String value) {
        if (name.equalsIgnoreCase("Connection")) {
            throw new IllegalArgumentException("The 'Connection' header is managed internally and cannot be set directly.");
        }

        if (name.equalsIgnoreCase("Content-Length")) {
            throw new IllegalArgumentException("The 'Content-Length' header is managed internally and cannot be set directly.");
        }

        if (name.equalsIgnoreCase("Content-Type") && value.contains("charset=")) {
            throw new IllegalArgumentException("The 'Content-Type' header is managed internally and cannot be set directly with charset.");
        }
    }
}