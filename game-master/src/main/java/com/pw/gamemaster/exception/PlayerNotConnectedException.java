package com.pw.gamemaster.exception;

public class PlayerNotConnectedException extends Exception{
    public PlayerNotConnectedException(String msg) {
        super(msg);
    }

    public PlayerNotConnectedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
