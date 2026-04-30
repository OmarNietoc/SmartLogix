package com.smartlogix.inventario.exception;

/** Excepción controlada para reglas de negocio del microservicio. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
