package br.com.brunodacostacalegari.httpconnector.parser;

import br.com.brunodacostacalegari.httpconnector.exception.HttpException;
import br.com.brunodacostacalegari.httpconnector.model.HttpRequest;
import br.com.brunodacostacalegari.httpconnector.model.HttpStatus;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpParserUnitTest {
    private final HttpParser parser = new HttpParser();

    @Test
    public void shouldThrowBadRequestWhenRequestLineEndsWithSingleCR() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\rHost: localhost\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenRequestLineEndsWithDoubleCR() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\rHost: localhost\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenRequestLineEndsWithSingleLF() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\nHost: localhost\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenRequestLineEndsWithDoubleLF() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\n\nHost: localhost\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenRequestLineEndsWithDoubleCRPlusLF() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\r\nHost: localhost\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenRequestLineEndsWithDoubleCRAndLF() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\r\n\nHost: localhost\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenFirstHeaderEndsWithSingleCR() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nHost: localhost\rContent-Length: 0\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenFirstHeaderEndsWithDoubleCR() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nHost: localhost\r\rContent-Length: 0\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenFirstHeaderEndsWithLF() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nHost: localhost\nContent-Length: 0\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenFirstHeaderEndsWithDoubleLF() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nHost: localhost\n\nContent-Length: 0\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenFirstHeaderEndsWithDoubleCRPlusLF() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nHost: localhost\r\r\nContent-Length: 0\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenFirstHeaderEndsWithDoubleCRAndLF() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nHost: localhost\r\r\n\nContent-Length: 0\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenSecondHeaderEndsWithCR() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nHost: localhost\r\nContent-Length: 0\rX-Test: value\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenSecondHeaderEndsWithDoubleCR() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nHost: localhost\r\nContent-Length: 0\r\rX-Test: value\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenSecondHeaderEndsWithLF() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nHost: localhost\r\nContent-Length: 0\nX-Test: value\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenSecondHeaderEndsWithDoubleLF() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nHost: localhost\r\nContent-Length: 0\n\nX-Test: value\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenSecondHeaderEndsWithDoubleCRPlusLF() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nHost: localhost\r\nContent-Length: 0\r\r\nX-Test: value\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenSecondHeaderEndsWithDoubleCRAndLF() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nHost: localhost\r\nContent-Length: 0\r\r\n\nX-Test: value\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenLastHeaderEndsWithCR() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nHost: localhost\r")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenLastHeaderEndsWithDoubleCR() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nHost: localhost\r\r")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenLastHeaderEndsWithLF() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nHost: localhost\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenLastHeaderEndsWithDoubleLF() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nHost: localhost\n\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenLastHeaderEndsWithDoubleCRPlusLF() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nHost: localhost\r\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenLastHeaderEndsWithDoubleCRAndLF() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nHost: localhost\r\r\n\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenLastHeaderEndsWithCRLF() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nHost: localhost\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenLastHeaderDoesNotHaveLineEnding() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nHost: localhost")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowRequestHeaderFieldsTooLargeWhenHeaderExceedsLimit() {
        var headerBuilder = new StringBuilder("GET / HTTP/1.1\r\n");
        for (int i = 0; i < 100; i++) {
            headerBuilder.append("X-Header-").append(i).append(": ").repeat("a", 20000).append("\r\n");
        }
        headerBuilder.append("Host: localhost\r\n\r\n");
        var exception = assertThrows(
                HttpException.class,
                () -> parse(headerBuilder.toString())
        );
        assertEquals(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE, exception.getStatus());
        assertEquals(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenRequestIsEmpty() {
        var exception = assertThrows(HttpException.class, () -> parse(""));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenRequestLineIsEmpty() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse(" \r\nHost: localhost\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenHeaderLineIsEmpty() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\n \r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenRequestLineDoesNotHaveLineEnding() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1 Host: localhost\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenRequestLineEndsWithDoubleCrlf() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\n\r\nHost: localhost\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenRequestLineDoesNotHaveMethod() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("/ HTTP/1.1\r\nHost: localhost\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenRequestLineDoesNotHavePath() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET HTTP/1.1\r\nHost: localhost\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenRequestLineDoesNotHaveProtocol() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET /\r\nHost: localhost\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenMethodIsNotSupported() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("OPTIONS /example HTTP/1.1\r\nHost: localhost\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenRequestPathIsInvalid() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET INVALID HTTP/1.1\r\nHost: localhost\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenQueryParameterHasInvalidEncoding() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET /search?q=%ZZ HTTP/1.1\r\nHost: localhost\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowHttpVersionNotSupportedWhenProtocolIsNotSupported() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/2.0\r\nHost: localhost\r\n\r\n")
        );
        assertEquals(HttpStatus.HTTP_VERSION_NOT_SUPPORTED, exception.getStatus());
        assertEquals(HttpStatus.HTTP_VERSION_NOT_SUPPORTED.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenRequestHeaderDoesNotHaveColon() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nHost localhost\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenRequestHasHeaderWithEmptyName() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\n: value\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenRequestDoesNotHaveHostHeader() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nUser-Agent: Test\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenRequestHasTransferEncodingHeader() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nHost:localhost\r\nTransfer-Encoding: chunked\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenRequestHasMultipleContentLengthHeaders() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("GET / HTTP/1.1\r\nHost:localhost\r\nContent-Length: 10\r\nContent-Length: 20\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowLengthRequiredWhenPostRequestDoesNotHaveContentLengthHeader() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("POST / HTTP/1.1\r\nHost:localhost\r\n\r\n")
        );
        assertEquals(HttpStatus.LENGTH_REQUIRED, exception.getStatus());
        assertEquals(HttpStatus.LENGTH_REQUIRED.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowLengthRequiredWhenPutRequestDoesNotHaveContentLengthHeader() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("PUT / HTTP/1.1\r\nHost:localhost\r\n\r\n")
        );
        assertEquals(HttpStatus.LENGTH_REQUIRED, exception.getStatus());
        assertEquals(HttpStatus.LENGTH_REQUIRED.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowLengthRequiredWhenPatchRequestDoesNotHaveContentLengthHeader() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("PATCH / HTTP/1.1\r\nHost:localhost\r\n\r\n")
        );
        assertEquals(HttpStatus.LENGTH_REQUIRED, exception.getStatus());
        assertEquals(HttpStatus.LENGTH_REQUIRED.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenRequestHasInvalidContentLengthValue() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("POST / HTTP/1.1\r\nHost:localhost\r\nContent-Length: invalid\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenRequestHasNegativeContentLengthValue() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("POST / HTTP/1.1\r\nHost:localhost\r\nContent-Length: -10\r\n\r\n")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowPayloadTooLargeWhenRequestBodyExceedsLimit() {
        var bodyBuilder = new StringBuilder();
        bodyBuilder.repeat("a", 11 * 1024 * 1024);
        var exception = assertThrows(
                HttpException.class,
                () -> parse("POST / HTTP/1.1\r\nHost:localhost\r\nContent-Length: " + bodyBuilder.length() + "\r\n\r\n" + bodyBuilder)
        );
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, exception.getStatus());
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenRequestBodyHasDifferentLengthThanContentLength() {
        var exception = assertThrows(
                HttpException.class,
                () -> parse("POST / HTTP/1.1\r\nHost:localhost\r\nContent-Length: 5\r\n\r\nabc")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldParseValidGetRequestWithoutQueryParameters() throws IOException {
        var request = parse("GET / HTTP/1.1\r\nHost: localhost\r\n\r\n");
        assertEquals("GET", request.getMethod());
        assertEquals("/", request.getPath());
        assertEquals("HTTP/1.1", request.getProtocol());
        assertTrue(request.getAllQueryParameters().isEmpty());
        assertTrue(request.getHeaderNames().contains("Host"));
        assertEquals("localhost", request.getHeader("Host"));
        assertEquals("127.0.0.1", request.getRemoteAddress());
        assertEquals(8080, request.getRemotePort());
    }

    @Test
    public void shouldParseValidGetRequestWithQueryParameters() throws IOException {
        var request = parse("GET /search?q=test&lang=en HTTP/1.1\r\nHost: localhost\r\n\r\n");
        assertEquals("GET", request.getMethod());
        assertEquals("/search", request.getPath());
        assertEquals("HTTP/1.1", request.getProtocol());
        assertTrue(request.getAllQueryParameters().containsKey("q"));
        assertEquals("test", request.getQueryParameter("q"));
        assertTrue(request.getAllQueryParameters().containsKey("lang"));
        assertEquals("en", request.getQueryParameter("lang"));
        assertTrue(request.getHeaderNames().contains("Host"));
        assertEquals("localhost", request.getHeader("Host"));
        assertEquals("127.0.0.1", request.getRemoteAddress());
        assertEquals(8080, request.getRemotePort());
    }

    @Test
    public void shouldParseValidGetRequestWithEncodedQueryParameters() throws IOException {
        var request = parse("GET /search?q=hello%20world&lang=en%2FUS HTTP/1.1\r\nHost: localhost\r\n\r\n");
        assertEquals("GET", request.getMethod());
        assertEquals("/search", request.getPath());
        assertEquals("HTTP/1.1", request.getProtocol());
        assertTrue(request.getAllQueryParameters().containsKey("q"));
        assertEquals("hello world", request.getQueryParameter("q"));
        assertTrue(request.getAllQueryParameters().containsKey("lang"));
        assertEquals("en/US", request.getQueryParameter("lang"));
        assertTrue(request.getHeaderNames().contains("Host"));
        assertEquals("localhost", request.getHeader("Host"));
        assertEquals("127.0.0.1", request.getRemoteAddress());
        assertEquals(8080, request.getRemotePort());
    }

    @Test
    public void shouldParseValidGetRequestWithMultipleValuesForSameQueryParameter() throws IOException {
        var request = parse("GET /search?q=test1&q=test2 HTTP/1.1\r\nHost: localhost\r\n\r\n");
        assertEquals("GET", request.getMethod());
        assertEquals("/search", request.getPath());
        assertEquals("HTTP/1.1", request.getProtocol());
        assertTrue(request.getAllQueryParameters().containsKey("q"));
        assertEquals("test1", request.getQueryParameter("q"));
        assertEquals(2, request.getAllQueryParameters().get("q").size());
        assertTrue(request.getHeaderNames().contains("Host"));
        assertEquals("localhost", request.getHeader("Host"));
        assertEquals("127.0.0.1", request.getRemoteAddress());
        assertEquals(8080, request.getRemotePort());
    }

    @Test
    public void shouldParseValidGetRequestWithMultipleHeaderValues() throws IOException {
        var request = parse("GET / HTTP/1.1\r\nHost: localhost\r\nX-Test: value1\r\nX-Test: value2\r\n\r\n");
        assertEquals("GET", request.getMethod());
        assertEquals("/", request.getPath());
        assertEquals("HTTP/1.1", request.getProtocol());
        assertTrue(request.getAllQueryParameters().isEmpty());
        assertTrue(request.getHeaderNames().contains("Host"));
        assertEquals("localhost", request.getHeader("Host"));
        assertTrue(request.getHeaderNames().contains("X-Test"));
        assertEquals(List.of("value1", "value2"), request.getHeaders("X-Test"));
        assertEquals("127.0.0.1", request.getRemoteAddress());
        assertEquals(8080, request.getRemotePort());
    }

    @Test
    public void shouldParseValidDeleteRequest() throws IOException {
        var request = parse("DELETE /resource/123 HTTP/1.1\r\nHost: localhost\r\n\r\n");
        assertEquals("DELETE", request.getMethod());
        assertEquals("/resource/123", request.getPath());
        assertEquals("HTTP/1.1", request.getProtocol());
        assertTrue(request.getAllQueryParameters().isEmpty());
        assertTrue(request.getHeaderNames().contains("Host"));
        assertEquals("localhost", request.getHeader("Host"));
        assertEquals("127.0.0.1", request.getRemoteAddress());
        assertEquals(8080, request.getRemotePort());
    }

    @Test
    public void shouldParseValidPostRequestWithBody() throws IOException {
        var body = "{\"test\": \"example\", \"foo\": 30}";
        var request = parse("POST /submit HTTP/1.1\r\nHost: localhost\r\nContent-Length: 30\r\n\r\n" + body);
        assertEquals("POST", request.getMethod());
        assertEquals("/submit", request.getPath());
        assertEquals("HTTP/1.1", request.getProtocol());
        assertTrue(request.getAllQueryParameters().isEmpty());
        assertTrue(request.getHeaderNames().contains("Host"));
        assertEquals("localhost", request.getHeader("Host"));
        assertTrue(request.getHeaderNames().contains("Content-Length"));
        assertEquals("30", request.getHeader("Content-Length"));
        assertEquals(
                "{\"test\": \"example\", \"foo\": 30}",
                new String(request.getBodyInputStream().readAllBytes(), StandardCharsets.UTF_8)
        );
        assertEquals("127.0.0.1", request.getRemoteAddress());
        assertEquals(8080, request.getRemotePort());
    }

    @Test
    public void shouldParseValidPutRequestWithBody() throws IOException {
        var request = parse("PUT /update HTTP/1.1\r\nHost: localhost\r\nContent-Length: 5\r\n\r\nHello");
        assertEquals("PUT", request.getMethod());
        assertEquals("/update", request.getPath());
        assertEquals("HTTP/1.1", request.getProtocol());
        assertTrue(request.getAllQueryParameters().isEmpty());
        assertTrue(request.getHeaderNames().contains("Host"));
        assertEquals("localhost", request.getHeader("Host"));
        assertTrue(request.getHeaderNames().contains("Content-Length"));
        assertEquals("5", request.getHeader("Content-Length"));
        assertEquals("Hello", new String(request.getBodyInputStream().readAllBytes(), StandardCharsets.UTF_8));
        assertEquals("127.0.0.1", request.getRemoteAddress());
        assertEquals(8080, request.getRemotePort());
    }

    @Test
    public void shouldParseValidPatchRequestWithBody() throws IOException {
        var request = parse("PATCH /update HTTP/1.1\r\nHost: localhost\r\nContent-Length: 5\r\n\r\nHello");
        assertEquals("PATCH", request.getMethod());
        assertEquals("/update", request.getPath());
        assertEquals("HTTP/1.1", request.getProtocol());
        assertTrue(request.getAllQueryParameters().isEmpty());
        assertTrue(request.getHeaderNames().contains("Host"));
        assertEquals("localhost", request.getHeader("Host"));
        assertTrue(request.getHeaderNames().contains("Content-Length"));
        assertEquals("5", request.getHeader("Content-Length"));
        assertEquals("Hello", new String(request.getBodyInputStream().readAllBytes(), StandardCharsets.UTF_8));
        assertEquals("127.0.0.1", request.getRemoteAddress());
        assertEquals(8080, request.getRemotePort());
    }

    private HttpRequest parse(String raw) throws IOException {
        return parser.parse(toStream(raw), "127.0.0.1", 8080);
    }

    private InputStream toStream(String headers) throws IOException {
        return toStream(headers, "");
    }

    private InputStream toStream(String headers, String body) throws IOException {
        var out = new ByteArrayOutputStream();
        out.write(headers.getBytes(StandardCharsets.ISO_8859_1));
        out.write(body.getBytes(StandardCharsets.UTF_8));
        return new ByteArrayInputStream(out.toByteArray());
    }
}
