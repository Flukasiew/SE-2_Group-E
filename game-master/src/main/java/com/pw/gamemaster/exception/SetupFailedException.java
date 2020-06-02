package com.pw.gamemaster.exception;

public class SetupFailedException extends Exception {
    public SetupFailedException(String msg) {
        super(msg);
    }

    public SetupFailedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
