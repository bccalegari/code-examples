package br.com.brunodacostacalegari.httpconnector.handler;

import br.com.brunodacostacalegari.httpconnector.model.HttpRequest;
import br.com.brunodacostacalegari.httpconnector.model.HttpResponse;

import java.io.IOException;

public interface HttpHandler {
    void handle(HttpRequest request, HttpResponse response) throws IOException;
}
