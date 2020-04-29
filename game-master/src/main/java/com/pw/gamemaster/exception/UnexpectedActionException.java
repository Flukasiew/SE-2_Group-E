package com.pw.gamemaster.exception;

public class UnexpectedActionException extends Exception {
    public UnexpectedActionException(String msg) {
        super(msg);
    }

    public UnexpectedActionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
