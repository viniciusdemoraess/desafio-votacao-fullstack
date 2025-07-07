package br.com.desafio_votacao.exception;

import org.springframework.http.HttpStatus;

public class UnableToVoteException extends RuntimeException {
    private final HttpStatus status;

    public UnableToVoteException(HttpStatus status) {
        super("UNABLE_TO_VOTE");
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}