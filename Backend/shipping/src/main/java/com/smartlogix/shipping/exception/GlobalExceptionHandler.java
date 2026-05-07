package com.smartlogix.shipping.exception;

import com.smartlogix.shipping.dto.MessageResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RouteNotFoundException.class)
    public ResponseEntity<MessageResponse<Void>> handleRouteNotFoundException(RouteNotFoundException ex) {
        MessageResponse<Void> response = MessageResponse.<Void>builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ShipmentNotFoundException.class)
    public ResponseEntity<MessageResponse<Void>> handleShipmentNotFoundException(ShipmentNotFoundException ex) {
        MessageResponse<Void> response = MessageResponse.<Void>builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<MessageResponse<Void>> handleExternalApiException(ExternalApiException ex) {
        MessageResponse<Void> response = MessageResponse.<Void>builder()
                .statusCode(HttpStatus.SERVICE_UNAVAILABLE.value())
                .message(ex.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst().orElse("Datos inválidos");
        MessageResponse<Void> response = MessageResponse.<Void>builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(detail)
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<MessageResponse<Void>> handleIllegalState(IllegalStateException ex) {
        MessageResponse<Void> response = MessageResponse.<Void>builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MessageResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        MessageResponse<Void> response = MessageResponse.<Void>builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<MessageResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        MessageResponse<Void> response = MessageResponse.<Void>builder()
                .statusCode(HttpStatus.CONFLICT.value())
                .message("Recurso duplicado o conflicto de datos")
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse<Void>> handleGeneralException(Exception ex) {
        MessageResponse<Void> response = MessageResponse.<Void>builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Ocurrió un error interno en el servidor")
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
