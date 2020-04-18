package com.pw.server.exception;

public class GameMasterSetupException extends Exception {
    public GameMasterSetupException(String message) {
        super(message);
    }

    public GameMasterSetupException(String message, Throwable cause) {
        super(message, cause);
    }
}
