package com.groupon.novie.internal.exception;

/**
 * Novie Specific Runtime Exception
 *
 */
public class NovieRuntimeException extends NovieException {

    public NovieRuntimeException(String message) {
        super(message);
    }

    public NovieRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
