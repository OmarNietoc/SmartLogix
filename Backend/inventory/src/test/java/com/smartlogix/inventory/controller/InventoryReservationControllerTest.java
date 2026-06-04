package com.smartlogix.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogix.inventory.config.SecurityConfig;
import com.smartlogix.inventory.dto.InventoryReservationDTO;
import com.smartlogix.inventory.dto.StockReservationRequestDTO;
import com.smartlogix.inventory.mapper.InventoryReservationMapper;
import com.smartlogix.inventory.model.InventoryReservation;
import com.smartlogix.inventory.service.InventoryReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryReservationController.class)
@Import(SecurityConfig.class)
class InventoryReservationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean InventoryReservationService reservationService;
    @MockBean InventoryReservationMapper reservationMapper;

    @Test
    void getAllReservations_noFilter_returnsListWith200() throws Exception {
        InventoryReservation reservation = new InventoryReservation();
        InventoryReservationDTO dto = buildReservationDTO("res-1");

        when(reservationService.getAllReservations(null, null)).thenReturn(List.of(reservation));
        when(reservationMapper.toDto(reservation)).thenReturn(dto);

        mockMvc.perform(get("/smartlogix/inventory/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("res-1"))
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    void getReservationById_found_returns200() throws Exception {
        InventoryReservation reservation = new InventoryReservation();
        InventoryReservationDTO dto = buildReservationDTO("res-1");
        when(reservationService.getReservationById("res-1")).thenReturn(reservation);
        when(reservationMapper.toDto(reservation)).thenReturn(dto);

        mockMvc.perform(get("/smartlogix/inventory/reservations/res-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("res-1"));
    }

    @Test
    void reserveStock_validRequest_returns201() throws Exception {
        StockReservationRequestDTO request = StockReservationRequestDTO.builder()
                .orderId("order-1").productId("prod-1").warehouseId("wh-1").quantity(5).build();
        InventoryReservation created = new InventoryReservation();
        InventoryReservationDTO dto = buildReservationDTO("res-new");

        when(reservationService.reserveStock(any())).thenReturn(created);
        when(reservationMapper.toDto(created)).thenReturn(dto);

        mockMvc.perform(post("/smartlogix/inventory/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("res-new"));
    }

    @Test
    void compensateReservation_existing_returns200() throws Exception {
        InventoryReservation reservation = new InventoryReservation();
        InventoryReservationDTO dto = buildReservationDTO("res-1");
        when(reservationService.compensateReservation("res-1")).thenReturn(reservation);
        when(reservationMapper.toDto(reservation)).thenReturn(dto);

        mockMvc.perform(patch("/smartlogix/inventory/reservations/res-1/compensate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reserva compensada exitosamente"));
    }

    @Test
    void confirmReservationAsOutput_existing_returns200() throws Exception {
        InventoryReservation reservation = new InventoryReservation();
        InventoryReservationDTO dto = buildReservationDTO("res-1");
        when(reservationService.confirmReservationAsOutput("res-1")).thenReturn(reservation);
        when(reservationMapper.toDto(reservation)).thenReturn(dto);

        mockMvc.perform(patch("/smartlogix/inventory/reservations/res-1/confirm-output"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reserva confirmada como salida definitiva"));
    }

    private InventoryReservationDTO buildReservationDTO(String id) {
        InventoryReservationDTO dto = new InventoryReservationDTO();
        dto.setId(id);
        dto.setOrderId("order-1");
        dto.setStatus("RESERVED");
        return dto;
    }
}
