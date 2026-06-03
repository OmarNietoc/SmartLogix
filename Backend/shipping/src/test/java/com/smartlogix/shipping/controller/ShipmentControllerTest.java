package com.smartlogix.shipping.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogix.shipping.config.SecurityConfig;
import com.smartlogix.shipping.dto.MessageResponse;
import com.smartlogix.shipping.dto.ShipmentDTO;
import com.smartlogix.shipping.enums.DeliveryStatus;
import com.smartlogix.shipping.mapper.ShipmentMapper;
import com.smartlogix.shipping.model.Shipment;
import com.smartlogix.shipping.service.ShipmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShipmentController.class)
@Import(SecurityConfig.class)
class ShipmentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ShipmentService shipmentService;
    @MockBean ShipmentMapper shipmentMapper;

    private static final String COMPANY_ID = "company-1";

    @Test
    void getAllShipments_noFilter_returnsListWith200() throws Exception {
        // arrange
        Shipment shipment = new Shipment();
        ShipmentDTO dto = buildShipmentDTO("ship-1");
        when(shipmentService.getAllShipments(COMPANY_ID, null)).thenReturn(List.of(shipment));
        when(shipmentMapper.toDto(shipment)).thenReturn(dto);

        // act & assert
        mockMvc.perform(get("/smartlogix/shipping/shipments")
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("ship-1"))
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    void getShipmentById_found_returns200() throws Exception {
        // arrange
        Shipment shipment = new Shipment();
        ShipmentDTO dto = buildShipmentDTO("ship-1");
        when(shipmentService.getShipmentById("ship-1", COMPANY_ID)).thenReturn(shipment);
        when(shipmentMapper.toDto(shipment)).thenReturn(dto);

        // act & assert
        mockMvc.perform(get("/smartlogix/shipping/shipments/ship-1")
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("ship-1"));
    }

    @Test
    void getShipmentByTrackingNumber_found_returns200() throws Exception {
        // arrange
        Shipment shipment = new Shipment();
        ShipmentDTO dto = buildShipmentDTO("ship-1");
        dto.setTrackingNumber("TRK-001");
        when(shipmentService.getShipmentByTrackingNumber("TRK-001", COMPANY_ID)).thenReturn(shipment);
        when(shipmentMapper.toDto(shipment)).thenReturn(dto);

        // act & assert
        mockMvc.perform(get("/smartlogix/shipping/shipments/tracking/TRK-001")
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.trackingNumber").value("TRK-001"));
    }

    @Test
    void createShipment_validRequest_returns201() throws Exception {
        // arrange
        ShipmentDTO request = buildShipmentDTO(null);
        Shipment entity = new Shipment();
        Shipment created = new Shipment();
        ShipmentDTO responseDto = buildShipmentDTO("ship-new");

        when(shipmentMapper.toEntity(any())).thenReturn(entity);
        when(shipmentService.createShipment(entity)).thenReturn(created);
        when(shipmentMapper.toDto(created)).thenReturn(responseDto);

        // act & assert
        mockMvc.perform(post("/smartlogix/shipping/shipments")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("ship-new"))
                .andExpect(jsonPath("$.statusCode").value(201));
    }

    @Test
    void updateShipmentStatus_validStatus_returns200() throws Exception {
        // arrange
        Shipment updated = new Shipment();
        ShipmentDTO dto = buildShipmentDTO("ship-1");
        dto.setDeliveryStatus("DISPATCHED");
        when(shipmentService.updateShipmentStatus("ship-1", DeliveryStatus.DISPATCHED, COMPANY_ID)).thenReturn(updated);
        when(shipmentMapper.toDto(updated)).thenReturn(dto);

        // act & assert
        mockMvc.perform(patch("/smartlogix/shipping/shipments/ship-1/status")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString("DISPATCHED")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deliveryStatus").value("DISPATCHED"));
    }

    @Test
    void deleteShipment_existing_returns200() throws Exception {
        // arrange
        doNothing().when(shipmentService).deleteShipment("ship-1", COMPANY_ID);

        // act & assert
        mockMvc.perform(delete("/smartlogix/shipping/shipments/ship-1")
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Envío eliminado exitosamente"));
    }

    private ShipmentDTO buildShipmentDTO(String id) {
        ShipmentDTO dto = new ShipmentDTO();
        dto.setId(id);
        dto.setOrderId("order-1");
        dto.setCompanyId(COMPANY_ID);
        dto.setCustomerName("Juan Pérez");
        dto.setShippingAddress("Av. Principal 123");
        dto.setDeliveryStatus("PENDING");
        return dto;
    }
}
