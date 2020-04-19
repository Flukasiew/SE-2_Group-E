package com.pw.server.exception;

public class PlayerSetupException extends Exception {
    public PlayerSetupException(String message) {
        super(message);
    }

    public PlayerSetupException(String message, Throwable cause) {
        super(message, cause);
    }
}
