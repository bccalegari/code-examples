package br.com.brunodacostacalegari.httpconnector.exception;

import br.com.brunodacostacalegari.httpconnector.model.HttpResponse;
import br.com.brunodacostacalegari.httpconnector.model.HttpStatus;

public class HttpExceptionHandler {
    public static HttpResponse handle(Exception e) {
        HttpResponse response = new HttpResponse();

        if (e instanceof HttpException httpException) {
            System.out.println("Handling HttpException: " + httpException.getStatus() + " - " + httpException.getReason());
            response.setStatus(httpException.getStatus().getCode(), httpException.getStatus().getMessage());
            response.setBody(httpException.getMessage());
            return response;
        }

        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), HttpStatus.INTERNAL_SERVER_ERROR.getMessage());
        response.setBody(HttpStatus.INTERNAL_SERVER_ERROR.getMessage());
        return response;
    }
}
