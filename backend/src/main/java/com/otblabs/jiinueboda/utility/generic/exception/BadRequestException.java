package com.otblabs.jiinueboda.utility.generic.exception;

public class BadRequestException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    private String message = "Bad Request.";

    public BadRequestException() {
        super();
    }

    public BadRequestException(String message) {
        super(message);
        this.message = message;
    }

    public BadRequestException(Throwable cause) {
        super(cause);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
