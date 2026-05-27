package br.com.brunodacostacalegari.httpconnector.server;

import br.com.brunodacostacalegari.httpconnector.handler.HttpHandler;
import br.com.brunodacostacalegari.httpconnector.model.HttpRequest;
import br.com.brunodacostacalegari.httpconnector.model.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class HttpConnectorIntTest {
    private HttpConnector connector;
    private final HttpClient client = HttpClient.newHttpClient();

    @AfterEach
    void tearDown() {
        if (connector != null) {
            connector.stop();
        }
    }

    @Test
    void shouldHandleGetRequest() throws Exception {
        int port = randomPort();

        HttpHandler handler = (HttpRequest request, HttpResponse response) -> {
            assertEquals("GET", request.getMethod());
            assertEquals("/users", request.getPath());

            response.setStatus(200);
            response.setBody("GET OK");
        };

        startConnector(port, handler);

        var request = java.net.http.HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/users"))
                .GET()
                .build();

        var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("GET OK", response.body());
    }

    @Test
    void shouldHandlePostRequest() throws Exception {
        int port = randomPort();

        HttpHandler handler = (HttpRequest request, HttpResponse response) -> {
            assertEquals("POST", request.getMethod());

            String body = new String(request.getBodyInputStream().readAllBytes());
            assertEquals("hello", body);

            response.setStatus(201);
            response.setBody("POST OK");
        };

        startConnector(port, handler);

        var request = java.net.http.HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/posts"))
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString("hello"))
                .build();

        var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals("POST OK", response.body());
    }

    @Test
    void shouldHandlePutRequest() throws Exception {
        int port = randomPort();

        HttpHandler handler = (HttpRequest request, HttpResponse response) -> {
            assertEquals("PUT", request.getMethod());

            response.setStatus(200);
            response.setBody("PUT OK");
        };

        startConnector(port, handler);

        var request = java.net.http.HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/users/1"))
                .PUT(java.net.http.HttpRequest.BodyPublishers.ofString("updated"))
                .build();

        var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("PUT OK", response.body());
    }

    @Test
    void shouldHandlePatchRequest() throws Exception {
        int port = randomPort();

        HttpHandler handler = (HttpRequest request, HttpResponse response) -> {
            assertEquals("PATCH", request.getMethod());

            String body = new String(request.getBodyInputStream().readAllBytes());
            assertEquals("patched", body);

            response.setStatus(200);
            response.setBody("PATCH OK");
        };

        startConnector(port, handler);

        var request = java.net.http.HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/users/1"))
                .method("PATCH", java.net.http.HttpRequest.BodyPublishers.ofString("patched"))
                .build();

        var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("PATCH OK", response.body());
    }

    @Test
    void shouldHandleDeleteRequest() throws Exception {
        int port = randomPort();

        HttpHandler handler = (HttpRequest request, HttpResponse response) -> {
            assertEquals("DELETE", request.getMethod());

            response.setStatus(204);
            response.setBody("");
        };

        startConnector(port, handler);

        var request = java.net.http.HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/users/1"))
                .DELETE()
                .build();

        var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode());
        assertEquals("", response.body());
    }

    @Test
    void shouldHandleQueryParameters() throws Exception {
        int port = randomPort();

        HttpHandler handler = (HttpRequest request, HttpResponse response) -> {
            assertEquals("Bruno", request.getQueryParameter("name"));
            assertEquals("18", request.getQueryParameter("age"));

            response.setBody("QUERY OK");
        };

        startConnector(port, handler);

        var request = java.net.http.HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/users?name=Bruno&age=18"))
                .GET()
                .build();

        var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("QUERY OK", response.body());
    }

    @Test
    void shouldHandleMultipleQueryParametersWithSameKey() throws Exception {
        int port = randomPort();

        HttpHandler handler = (HttpRequest request, HttpResponse response) -> {
            var tags = request.getQueryParameters("tag");
            assertEquals(2, tags.size());
            assertTrue(tags.contains("java"));
            assertTrue(tags.contains("spring"));

            response.setBody("QUERY OK");
        };

        startConnector(port, handler);

        var request = java.net.http.HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/users?tag=java&tag=spring"))
                .GET()
                .build();

        var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("QUERY OK", response.body());
    }

    @Test
    void shouldHandleMultipleHeadersWithSameKey() throws Exception {
        int port = randomPort();

        HttpHandler handler = (HttpRequest request, HttpResponse response) -> {
            var values = request.getHeaders("X-Tag");
            assertEquals(2, values.size());
            assertTrue(values.contains("java"));
            assertTrue(values.contains("spring"));

            response.setBody("HEADER OK");
        };

        startConnector(port, handler);

        var request = java.net.http.HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/users"))
                .GET()
                .header("X-Tag", "java")
                .header("X-Tag", "spring")
                .build();

        var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("HEADER OK", response.body());
    }

    @Test
    void shouldReceiveCustomHeaderInHandler() throws Exception {
        int port = randomPort();

        HttpHandler handler = (HttpRequest request, HttpResponse response) -> {
            assertEquals("my-value", request.getHeader("X-Custom-Header"));
            response.setBody("OK");
        };

        startConnector(port, handler);

        var request = java.net.http.HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/users"))
                .GET()
                .header("X-Custom-Header", "my-value")
                .build();

        var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    @Test
    void shouldReturnCustomContentType() throws Exception {
        int port = randomPort();

        HttpHandler handler = (_, response) -> {
            response.setHeader("Content-Type", "application/json");
            response.setBody("{\"ok\": true}");
        };

        startConnector(port, handler);

        var request = java.net.http.HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/users"))
                .GET()
                .build();

        var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("application/json; charset=UTF-8", response.headers().firstValue("Content-Type").orElseThrow());
        assertEquals("{\"ok\": true}", response.body());
    }

    @Test
    void shouldReturnConnectionCloseHeader() throws Exception {
        int port = randomPort();

        startConnector(port, (_, response) -> response.setBody("OK"));

        var request = java.net.http.HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port))
                .GET()
                .build();

        var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        assertEquals("close", response.headers().firstValue("Connection").orElseThrow());
    }

    @Test
    void shouldReturnContentLengthHeader() throws Exception {
        int port = randomPort();

        startConnector(port, (_, response) -> response.setBody("hello"));

        var request = java.net.http.HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port))
                .GET()
                .build();

        var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        assertEquals("5", response.headers().firstValue("Content-Length").orElseThrow());
    }

    @Test
    void shouldReturnZeroContentLengthWhenBodyIsEmpty() throws Exception {
        int port = randomPort();

        startConnector(port, (_, response) -> response.setStatus(200));

        var request = java.net.http.HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port))
                .GET()
                .build();

        var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("0", response.headers().firstValue("Content-Length").orElseThrow());
        assertEquals("", response.body());
    }

    @Test
    void shouldReturnInternalServerErrorWhenHandlerThrowsException() throws Exception {
        int port = randomPort();

        HttpHandler handler = (_, _) -> {
            throw new RuntimeException("unexpected");
        };

        startConnector(port, handler);

        var request = java.net.http.HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port))
                .GET()
                .build();

        var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode());
        assertEquals("Internal Server Error", response.body());
    }

    @Test
    void shouldReturnBadRequestWhenMethodIsNotSupported() throws Exception {
        int port = randomPort();

        startConnector(port, (_, response) -> response.setBody("OK"));

        var request = java.net.http.HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/users"))
                .method("OPTIONS", java.net.http.HttpRequest.BodyPublishers.noBody())
                .build();

        var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldReturnBadRequestWhenHostHeaderIsMissing() throws Exception {
        int port = randomPort();

        startConnector(port, (_, response) -> response.setBody("OK"));

        try (var socket = new Socket("localhost", port)) {
            var out = socket.getOutputStream();
            out.write("GET / HTTP/1.1\r\nUser-Agent: test\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
            out.flush();

            var responseText = new String(socket.getInputStream().readAllBytes(), StandardCharsets.ISO_8859_1);
            assertTrue(responseText.startsWith("HTTP/1.1 400"));
        }
    }

    @Test
    void shouldReturnBadRequestWhenTransferEncodingIsPresent() throws Exception {
        int port = randomPort();

        startConnector(port, (_, response) -> response.setBody("OK"));

        try (var socket = new Socket("localhost", port)) {
            var out = socket.getOutputStream();
            out.write("POST / HTTP/1.1\r\nHost: localhost\r\nTransfer-Encoding: chunked\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
            out.flush();

            var responseText = new String(socket.getInputStream().readAllBytes(), StandardCharsets.ISO_8859_1);
            assertTrue(responseText.startsWith("HTTP/1.1 400"));
        }
    }

    @Test
    void shouldReturnHttpVersionNotSupportedWhenProtocolIsInvalid() throws Exception {
        int port = randomPort();

        startConnector(port, (_, response) -> response.setBody("OK"));

        try (var socket = new Socket("localhost", port)) {
            var out = socket.getOutputStream();
            out.write("GET / HTTP/2.0\r\nHost: localhost\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
            out.flush();

            var responseText = new String(socket.getInputStream().readAllBytes(), StandardCharsets.ISO_8859_1);
            assertTrue(responseText.startsWith("HTTP/1.1 505"));
        }
    }

    @Test
    void shouldReturnPayloadTooLargeWhenBodyExceedsLimit() throws Exception {
        int port = randomPort();

        startConnector(port, (_, response) -> response.setBody("OK"));

        try (var socket = new Socket("localhost", port)) {
            int bodySize = 11 * 1024 * 1024;

            var out = socket.getOutputStream();
            out.write((
                    "POST /users HTTP/1.1\r\n" +
                            "Host: localhost\r\n" +
                            "Content-Length: " + bodySize + "\r\n" +
                            "\r\n"
            ).getBytes(StandardCharsets.ISO_8859_1));
            out.flush();

            var responseText = new String(socket.getInputStream().readAllBytes(), StandardCharsets.ISO_8859_1);
            assertTrue(responseText.startsWith("HTTP/1.1 413"));
        }
    }

    @Test
    void shouldReturnRequestHeaderFieldsTooLargeWhenHeadersExceedLimit() throws Exception {
        int port = randomPort();

        startConnector(port, (_, response) -> response.setBody("OK"));

        var request = java.net.http.HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/users"))
                .GET()
                .header("X-Large-Header", "a".repeat(9000))
                .build();

        var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        assertEquals(431, response.statusCode());
    }

    @Test
    void shouldReturnLengthRequiredWhenPostRequestDoesNotHaveContentLength() throws Exception {
        int port = randomPort();

        startConnector(port, (_, response) -> response.setBody("OK"));

        try (var socket = new Socket("localhost", port)) {
            var out = socket.getOutputStream();
            out.write((
                    "POST /users HTTP/1.1\r\n" +
                            "Host: localhost\r\n" +
                            "\r\n"
            ).getBytes(StandardCharsets.ISO_8859_1));
            out.flush();

            var responseText = new String(socket.getInputStream().readAllBytes(), StandardCharsets.ISO_8859_1);
            assertTrue(responseText.startsWith("HTTP/1.1 411"));
        }
    }

    @Test
    void shouldReturnRequestTimeoutWhenClientIsTooSlow() throws Exception {
        int port = randomPort();

        startConnector(port, (_, response) -> response.setBody("OK"));

        try (var socket = new Socket("localhost", port)) {
            socket.getOutputStream().write("GET /".getBytes(StandardCharsets.ISO_8859_1));
            socket.getOutputStream().flush();

            Thread.sleep(6000);

            var responseText = new String(socket.getInputStream().readAllBytes(), StandardCharsets.ISO_8859_1);
            assertTrue(responseText.startsWith("HTTP/1.1 408"));
        }
    }

    @Test
    void shouldHandleMultipleSimultaneousRequests() throws Exception {
        int port = randomPort();
        int requestCount = 20;

        startConnector(port, (HttpRequest _, HttpResponse response) -> {
            response.setStatus(200);
            response.setBody("OK");
        });

        var futures = new ArrayList<CompletableFuture<java.net.http.HttpResponse<String>>>();

        for (int i = 0; i < requestCount; i++) {
            var request = java.net.http.HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:" + port + "/users"))
                    .GET()
                    .build();

            futures.add(client.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString()));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        for (var future : futures) {
            assertEquals(200, future.get().statusCode());
            assertEquals("OK", future.get().body());
        }
    }

    private void startConnector(int port, HttpHandler handler) throws InterruptedException {
        connector = new HttpConnector(port, handler);

        Thread.startVirtualThread(() -> {
            try {
                connector.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Thread.sleep(200);
    }

    private int randomPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
