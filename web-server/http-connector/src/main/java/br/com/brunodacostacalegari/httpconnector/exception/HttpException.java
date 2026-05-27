package br.com.brunodacostacalegari.httpconnector.exception;

import br.com.brunodacostacalegari.httpconnector.model.HttpStatus;

public class HttpException extends RuntimeException {
    private final HttpStatus status;
    private final String reason;

    public HttpException(HttpStatus status, String reason) {
        super(status.getMessage());
        this.status = status;
        this.reason = reason;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }
}
