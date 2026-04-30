package com.smartlogix.inventario.exception;

import com.smartlogix.inventario.dto.MessageResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/** Manejo centralizado de errores con el mismo contrato usado por ms-shipping. */
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({ProductNotFoundException.class, WarehouseNotFoundException.class, InventoryNotFoundException.class, InventoryReservationNotFoundException.class})
    public ResponseEntity<MessageResponse<Void>> handleNotFound(RuntimeException ex) {
        return new ResponseEntity<>(MessageResponse.<Void>builder().statusCode(HttpStatus.NOT_FOUND.value()).message(ex.getMessage()).data(null).build(), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, InsufficientStockException.class})
    public ResponseEntity<MessageResponse<Void>> handleBusiness(RuntimeException ex) {
        return new ResponseEntity<>(MessageResponse.<Void>builder().statusCode(HttpStatus.BAD_REQUEST.value()).message(ex.getMessage()).data(null).build(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse<Void>> handleGeneral(Exception ex) {
        return new ResponseEntity<>(MessageResponse.<Void>builder().statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value()).message("Ocurrió un error interno en el servidor: " + ex.getMessage()).data(null).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
