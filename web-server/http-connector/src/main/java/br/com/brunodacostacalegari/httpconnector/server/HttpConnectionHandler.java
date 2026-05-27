package br.com.brunodacostacalegari.httpconnector.server;

import br.com.brunodacostacalegari.httpconnector.exception.HttpException;
import br.com.brunodacostacalegari.httpconnector.exception.HttpExceptionHandler;
import br.com.brunodacostacalegari.httpconnector.handler.HttpHandler;
import br.com.brunodacostacalegari.httpconnector.model.HttpResponse;
import br.com.brunodacostacalegari.httpconnector.model.HttpStatus;
import br.com.brunodacostacalegari.httpconnector.pipeline.HttpExchange;
import br.com.brunodacostacalegari.httpconnector.pipeline.HttpPipeline;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class HttpConnectionHandler {
    private final HttpPipeline pipeline;

    public HttpConnectionHandler(HttpHandler handler) {
        this.pipeline = new HttpPipeline(handler);
    }

    public void handle(Socket socket) {
        long start = System.nanoTime();

        try (socket) {
            socket.setSoTimeout(5000);
            processRequest(socket, socket.getOutputStream(), start);
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    private void processRequest(Socket socket, OutputStream outputStream, long start) {
        try {
            HttpExchange exchange = pipeline.execute(socket);
            exchange.getResponse().write(outputStream);

            long durationMs = (System.nanoTime() - start) / 1_000_000;

            System.out.println(
                    exchange.getRequest().getMethod() + " " +
                            exchange.getRequest().getPath() + " -> " +
                            exchange.getResponse().getStatusCode() + " (" + durationMs + "ms)"
            );
        } catch (SocketTimeoutException e) {
            writeError(outputStream, new HttpException(HttpStatus.REQUEST_TIMEOUT, "Request timed out"), start);
        } catch (Exception e) {
            writeError(outputStream, e, start);
        }
    }

    private void writeError(OutputStream outputStream, Exception e, long start) {
        try {
            HttpResponse response = HttpExceptionHandler.handle(e);
            response.write(outputStream);

            long durationMs = (System.nanoTime() - start) / 1_000_000;

            System.out.println("ERROR " + response.getStatusCode() + " (" + durationMs + "ms)");
        } catch (IOException writeException) {
            System.err.println("Failed to write error response: " + writeException.getMessage());
        }
    }
}
