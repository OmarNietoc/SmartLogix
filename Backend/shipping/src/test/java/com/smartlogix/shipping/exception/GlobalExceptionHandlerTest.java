package com.smartlogix.shipping.exception;

import com.smartlogix.shipping.dto.MessageResponse;
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
    void handleRouteNotFoundException_returns404() {
        var response = handler.handleRouteNotFoundException(new RouteNotFoundException("Ruta no encontrada"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(body(response).getMessage()).isEqualTo("Ruta no encontrada");
    }

    @Test
    void handleShipmentNotFoundException_returns404() {
        var response = handler.handleShipmentNotFoundException(new ShipmentNotFoundException("Envio no encontrado"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(body(response).getMessage()).isEqualTo("Envio no encontrado");
    }

    @Test
    void handleExternalApiException_returns503() {
        var response = handler.handleExternalApiException(new ExternalApiException("OSRM no disponible"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(body(response).getMessage()).isEqualTo("OSRM no disponible");
    }

    @Test
    void handleValidation_returns400() {
        var response = handler.handleValidation(validationException("originAddress", "must not be blank"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(body(response).getMessage()).isEqualTo("originAddress: must not be blank");
    }

    @Test
    void handleIllegalState_returns400() {
        var response = handler.handleIllegalState(new IllegalStateException("Ruta completada"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(body(response).getMessage()).isEqualTo("Ruta completada");
    }

    @Test
    void handleIllegalArgument_returns400() {
        var response = handler.handleIllegalArgument(new IllegalArgumentException("Estado invalido"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(body(response).getMessage()).isEqualTo("Estado invalido");
    }

    @Test
    void handleDataIntegrity_returns409() {
        var response = handler.handleDataIntegrity(new DataIntegrityViolationException("duplicate"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(body(response).getMessage()).isEqualTo("Recurso duplicado o conflicto de datos");
    }

    @Test
    void handleGeneralException_returns500() {
        var response = handler.handleGeneralException(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(body(response).getMessage()).isEqualTo("Ocurrió un error interno en el servidor");
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
