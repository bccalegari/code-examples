package br.com.brunodacostacalegari.httpconnector.pipeline;

import br.com.brunodacostacalegari.httpconnector.model.HttpRequest;
import br.com.brunodacostacalegari.httpconnector.model.HttpResponse;

public class HttpExchange {
    private final HttpRequest request;
    private final HttpResponse response;

    public HttpExchange(HttpRequest request, HttpResponse response) {
        this.request = request;
        this.response = response;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public HttpResponse getResponse() {
        return response;
    }
}
