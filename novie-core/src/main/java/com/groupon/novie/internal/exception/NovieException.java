package com.groupon.novie.internal.exception;

/**
 * Supertype of all Novie exception
 *
 * @author Thomas
 */
public abstract class NovieException extends Exception {

    protected NovieException(String message) {
        super(message);
    }

    protected NovieException(String message, Throwable cause) {
        super(message, cause);
    }
}
