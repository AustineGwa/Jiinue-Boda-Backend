package com.otblabs.jiinueboda.security.generic.exception;

public class InvalidInputException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String message = "Invalid input provided";

    public InvalidInputException() {
        super();
    }

    public InvalidInputException(String message) {
        super(message);
        this.message = message;
    }

    public InvalidInputException(Throwable cause) {
        super(cause);
    }

    public InvalidInputException(String message, Throwable cause) {
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
