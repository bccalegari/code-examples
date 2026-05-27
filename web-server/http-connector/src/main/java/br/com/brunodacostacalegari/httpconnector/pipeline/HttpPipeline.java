package br.com.brunodacostacalegari.httpconnector.pipeline;

import br.com.brunodacostacalegari.httpconnector.handler.HttpHandler;
import br.com.brunodacostacalegari.httpconnector.parser.HttpParser;
import br.com.brunodacostacalegari.httpconnector.model.HttpRequest;
import br.com.brunodacostacalegari.httpconnector.model.HttpResponse;

import java.io.IOException;
import java.net.Socket;

public class HttpPipeline {
    private final HttpParser parser;
    private final HttpHandler handler;

    public HttpPipeline(HttpHandler handler) {
        this.parser = new HttpParser();
        this.handler = handler;
    }

    public HttpExchange execute(Socket socket) throws IOException {
        HttpRequest request = parser.parse(
                socket.getInputStream(), socket.getInetAddress().getHostAddress(), socket.getPort()
        );

        HttpResponse response = new HttpResponse();
        handler.handle(request, response);

        return new HttpExchange(request, response);
    }
}