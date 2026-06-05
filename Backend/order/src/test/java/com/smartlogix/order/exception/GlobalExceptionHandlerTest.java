package com.smartlogix.order.exception;

import com.smartlogix.order.dto.MessageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFound_returns404() {
        var response = handler.handleResourceNotFound(new ResourceNotFoundException("Pedido no encontrado"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(body(response).getMessage()).isEqualTo("Pedido no encontrado");
    }

    @Test
    void handleValidation_returns400WithFieldDetail() {
        var response = handler.handleValidation(validationException("customerEmail", "must be a well-formed email address"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(body(response).getMessage()).isEqualTo("customerEmail: must be a well-formed email address");
    }

    @Test
    void handleIllegalState_returns400() {
        var response = handler.handleIllegalState(new IllegalStateException("Transicion invalida"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(body(response).getMessage()).isEqualTo("Transicion invalida");
    }

    @Test
    void handleDataIntegrity_returns409() {
        var response = handler.handleDataIntegrity(new DataIntegrityViolationException("duplicate"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(body(response).getMessage()).isEqualTo("Recurso duplicado o conflicto de datos");
    }

    @Test
    void handleGeneral_returns500() {
        var response = handler.handleGeneral(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(body(response).getMessage()).isEqualTo("Error interno del servidor");
    }

    private MethodArgumentNotValidException validationException(String field, String message) {
        var exception = mock(MethodArgumentNotValidException.class);
        var bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", field, message));
        when(exception.getBindingResult()).thenReturn(bindingResult);
        return exception;
    }

    private MessageResponse<Void> body(org.springframework.http.ResponseEntity<MessageResponse<Void>> response) {
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }
}
