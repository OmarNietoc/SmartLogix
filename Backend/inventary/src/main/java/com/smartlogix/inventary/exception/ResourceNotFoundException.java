package com.smartlogix.inventary.exception;

/** Excepción controlada para recursos inexistentes. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
