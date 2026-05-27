package br.com.brunodacostacalegari.httpconnector.server;

import br.com.brunodacostacalegari.httpconnector.handler.HttpHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpConnector {
    private final int port;
    private final HttpConnectionHandler connectionHandler;
    private final int workers;
    private final ExecutorService pool;
    private final AtomicInteger counter = new AtomicInteger();

    private volatile boolean running = true;
    private ServerSocket serverSocket;

    public HttpConnector(int port, int workers, HttpHandler handler) {
        this.port = port;

        if (workers <= 0) {
            throw new IllegalArgumentException("Number of workers must be positive");
        }

        this.workers = workers;

        this.pool = Executors.newFixedThreadPool(
                workers,
                r -> {
                    Thread t = new Thread(r);
                    t.setName("http-worker-" + counter.incrementAndGet());
                    return t;
                }
        );

        this.connectionHandler = new HttpConnectionHandler(handler);
    }

    public HttpConnector(int port, HttpHandler handler) {
        this(port, Runtime.getRuntime().availableProcessors() * 2, handler);
    }

    public void start() throws IOException {
        final long start = System.nanoTime();

        serverSocket = new ServerSocket(port);

        long durationMs = (System.nanoTime() - start) / 1_000_000;

        System.out.println(
                "Server started on port " + port +
                        " in " + durationMs + "ms with " + workers + " worker threads"
        );

        while (running) {
            try {
                Socket socket = serverSocket.accept();
                pool.submit(() -> connectionHandler.handle(socket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("Accept error: " + e.getMessage());
                }
            }
        }
    }

    public void stop() {
        final long start = System.nanoTime();

        running = false;

        System.out.println("Shutting down server gracefully...");

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error while closing server socket: " + e.getMessage());
        }

        pool.shutdown();

        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("Forcing shutdown...");
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        long durationMs = (System.nanoTime() - start) / 1_000_000;

        System.out.println("Server stopped in " + durationMs + "ms");
    }
}