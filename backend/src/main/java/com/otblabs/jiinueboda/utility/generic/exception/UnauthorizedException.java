package com.otblabs.jiinueboda.utility.generic.exception;

public class UnauthorizedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String message = "Unauthorized Error";

    public UnauthorizedException() {
        super();
    }

    public UnauthorizedException(String message) {
        super(message);
        this.message = message;
    }

    public UnauthorizedException(Throwable cause) {
        super(cause);
    }

    public UnauthorizedException(String message, Throwable cause) {
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
