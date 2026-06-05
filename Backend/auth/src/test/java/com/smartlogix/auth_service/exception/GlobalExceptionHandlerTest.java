package com.smartlogix.auth_service.exception;

import com.smartlogix.auth_service.dto.MessageResponse;
import org.junit.jupiter.api.Test;
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
    void handleIllegalArgument_returnsBadRequestMessage() {
        var response = handler.handleIllegalArgument(new IllegalArgumentException("Credenciales invalidas"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(body(response).getStatusCode()).isEqualTo(400);
        assertThat(body(response).getMessage()).isEqualTo("Credenciales invalidas");
    }

    @Test
    void handleValidation_returnsFirstFieldErrorMessage() {
        var response = handler.handleValidation(validationException("email", "Ingresa un correo electronico valido"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(body(response).getMessage()).isEqualTo("Ingresa un correo electronico valido");
    }

    @Test
    void handleGeneral_returnsInternalServerErrorContract() {
        var response = handler.handleGeneral(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(body(response).getStatusCode()).isEqualTo(500);
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
