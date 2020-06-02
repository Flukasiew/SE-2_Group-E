package com.pw.gamemaster.exception;

public class TimeLimitExceededException extends Exception {
    public TimeLimitExceededException(String msg) {
        super(msg);
    }

    public TimeLimitExceededException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
