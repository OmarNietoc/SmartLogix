package com.smartlogix.auth_service.exception;

import com.smartlogix.auth_service.dto.MessageResponse;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MessageResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                MessageResponse.<Void>builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(ex.getMessage())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Datos de registro invalidos");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                MessageResponse.<Void>builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(message)
                        .data(null)
                        .build());
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<MessageResponse<Void>> handleFeign(FeignException ex) {
        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null || status.is5xxServerError()) {
            status = HttpStatus.BAD_REQUEST;
        }
        return ResponseEntity.status(status).body(
                MessageResponse.<Void>builder()
                        .statusCode(status.value())
                        .message(extractMessage(ex))
                        .data(null)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse<Void>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                MessageResponse.<Void>builder()
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message("Error interno del servidor")
                        .data(null)
                        .build());
    }

    private String extractMessage(FeignException ex) {
        String content = ex.contentUTF8();
        if (content != null) {
            int messageKey = content.indexOf("\"message\"");
            if (messageKey >= 0) {
                int colon = content.indexOf(':', messageKey);
                int start = colon >= 0 ? content.indexOf('"', colon + 1) : -1;
                int end = start >= 0 ? content.indexOf('"', start + 1) : -1;
                if (start >= 0 && end > start) {
                    return content.substring(start + 1, end);
                }
            }
        }
        if (ex.status() == HttpStatus.CONFLICT.value()) {
            return "El recurso ya existe o hay un conflicto de datos";
        }
        return "No se pudo completar el registro de empresa";
    }
}
