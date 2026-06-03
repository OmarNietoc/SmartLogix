package com.smartlogix.shipping.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogix.shipping.config.SecurityConfig;
import com.smartlogix.shipping.dto.RouteCreationRequestDTO;
import com.smartlogix.shipping.dto.RouteDTO;
import com.smartlogix.shipping.enums.RouteStatus;
import com.smartlogix.shipping.mapper.RouteMapper;
import com.smartlogix.shipping.model.Route;
import com.smartlogix.shipping.service.RouteService;
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

@WebMvcTest(RouteController.class)
@Import(SecurityConfig.class)
class RouteControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean RouteService routeService;
    @MockBean RouteMapper routeMapper;

    private static final String COMPANY_ID = "company-1";

    @Test
    void getAllRoutes_noFilter_returnsListWith200() throws Exception {
        // arrange
        Route route = new Route();
        RouteDTO dto = buildRouteDTO("route-1");
        when(routeService.getAllRoutes(COMPANY_ID, null)).thenReturn(List.of(route));
        when(routeMapper.toDto(route)).thenReturn(dto);

        // act & assert
        mockMvc.perform(get("/smartlogix/shipping/routes")
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("route-1"))
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    void getRouteById_found_returns200() throws Exception {
        // arrange
        Route route = new Route();
        RouteDTO dto = buildRouteDTO("route-1");
        when(routeService.getRouteById("route-1", COMPANY_ID)).thenReturn(route);
        when(routeMapper.toDto(route)).thenReturn(dto);

        // act & assert
        mockMvc.perform(get("/smartlogix/shipping/routes/route-1")
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("route-1"));
    }

    @Test
    void createRoute_validRequest_returns201() throws Exception {
        // arrange
        RouteCreationRequestDTO request = new RouteCreationRequestDTO();
        request.setCarrierId("LOCAL");
        request.setOriginAddress("Av. Las Condes 1234, Santiago");
        request.setShipmentIds(List.of("ship-1"));
        request.setOptimizeRoute(false);

        Route created = new Route();
        RouteDTO dto = buildRouteDTO("route-new");
        when(routeService.createRoute(eq(COMPANY_ID), anyString(), anyString(), anyList(), anyBoolean())).thenReturn(created);
        when(routeMapper.toDto(created)).thenReturn(dto);

        // act & assert
        mockMvc.perform(post("/smartlogix/shipping/routes")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("route-new"))
                .andExpect(jsonPath("$.statusCode").value(201));
    }

    @Test
    void updateRouteStatus_validStatus_returns200() throws Exception {
        // arrange
        Route updated = new Route();
        RouteDTO dto = buildRouteDTO("route-1");
        dto.setStatus("IN_PROGRESS");
        when(routeService.updateRouteStatus("route-1", RouteStatus.IN_PROGRESS, COMPANY_ID)).thenReturn(updated);
        when(routeMapper.toDto(updated)).thenReturn(dto);

        // act & assert
        mockMvc.perform(patch("/smartlogix/shipping/routes/route-1/status")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString("IN_PROGRESS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    void deleteRoute_existing_returns200() throws Exception {
        // arrange
        doNothing().when(routeService).deleteRoute("route-1", COMPANY_ID);

        // act & assert
        mockMvc.perform(delete("/smartlogix/shipping/routes/route-1")
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Ruta cancelada exitosamente y envíos liberados"));
    }

    private RouteDTO buildRouteDTO(String id) {
        RouteDTO dto = new RouteDTO();
        dto.setId(id);
        dto.setCompanyId(COMPANY_ID);
        dto.setCarrierId("LOCAL");
        dto.setStatus("PENDING");
        return dto;
    }
}
