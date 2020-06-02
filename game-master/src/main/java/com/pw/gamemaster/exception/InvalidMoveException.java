package com.pw.gamemaster.exception;

public class InvalidMoveException extends Exception {
    public InvalidMoveException(String msg) {
        super(msg);
    }

    public InvalidMoveException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
