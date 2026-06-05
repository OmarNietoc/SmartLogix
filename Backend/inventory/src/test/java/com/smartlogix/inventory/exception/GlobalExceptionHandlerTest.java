package com.smartlogix.inventory.exception;

import com.smartlogix.inventory.dto.MessageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_returns404() {
        var response = handler.handleNotFound(new ProductNotFoundException("Producto no encontrado"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(body(response).getMessage()).isEqualTo("Producto no encontrado");
    }

    @Test
    void handleBusiness_returns400() {
        var response = handler.handleBusiness(new InsufficientStockException("Stock insuficiente"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(body(response).getMessage()).isEqualTo("Stock insuficiente");
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
        assertThat(body(response).getMessage()).isEqualTo("Ocurrió un error interno en el servidor");
    }

    private MessageResponse<Void> body(org.springframework.http.ResponseEntity<MessageResponse<Void>> response) {
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }
}
