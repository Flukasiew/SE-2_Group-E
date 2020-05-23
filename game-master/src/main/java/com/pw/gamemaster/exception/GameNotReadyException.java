package com.pw.gamemaster.exception;

public class GameNotReadyException extends Exception {
    public GameNotReadyException(String msg) {
        super(msg);
    }

    public GameNotReadyException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
