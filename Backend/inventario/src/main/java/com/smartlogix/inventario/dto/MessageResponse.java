package com.smartlogix.inventario.dto;

import lombok.*;

/** Respuesta estándar igual a ms-shipping: statusCode, message y data. */
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class MessageResponse<T> {
    private int statusCode;
    private String message;
    private T data;
}
