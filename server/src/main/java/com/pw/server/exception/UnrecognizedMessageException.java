package com.pw.server.exception;

public class UnrecognizedMessageException extends Exception {
    public UnrecognizedMessageException(String message) {
        super(message);
    }

    public UnrecognizedMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
