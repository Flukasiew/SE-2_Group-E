package com.pw.gamemaster.exception;

public class GameSetupException extends Exception {
    public GameSetupException(String msg) {
        super(msg);
}

    public GameSetupException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
