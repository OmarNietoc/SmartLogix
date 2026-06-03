package com.smartlogix.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogix.order.config.SecurityConfig;
import com.smartlogix.order.dto.*;
import com.smartlogix.order.model.OrderStatus;
import com.smartlogix.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean OrderService orderService;

    private static final String COMPANY_ID = "company-1";

    @Test
    void getAllOrders_returnsListWith200() throws Exception {
        // arrange
        OrderResponse order = buildOrderResponse("order-1");
        when(orderService.getAllOrders(COMPANY_ID)).thenReturn(List.of(order));

        // act & assert
        mockMvc.perform(get("/smartlogix/order/orders")
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("order-1"))
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    void getOrderById_found_returns200() throws Exception {
        // arrange
        OrderResponse order = buildOrderResponse("order-1");
        when(orderService.getOrderById("order-1", COMPANY_ID)).thenReturn(order);

        // act & assert
        mockMvc.perform(get("/smartlogix/order/orders/order-1")
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("order-1"));
    }

    @Test
    void createOrder_validRequest_returns201() throws Exception {
        // arrange
        OrderItemRequest item = new OrderItemRequest("prod-1", "wh-1", "Producto X", 2, new BigDecimal("1000"));
        CreateOrderRequest request = new CreateOrderRequest("Juan Pérez", "juan@empresa.cl", "Av. Principal 123", 1, List.of(item), null);
        OrderResponse response = buildOrderResponse("order-new");
        when(orderService.createOrder(any(), eq(COMPANY_ID))).thenReturn(response);

        // act & assert
        mockMvc.perform(post("/smartlogix/order/orders")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("order-new"))
                .andExpect(jsonPath("$.statusCode").value(201));
    }

    @Test
    void updateOrderStatus_validTransition_returns200() throws Exception {
        // arrange
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.CONFIRMED);
        OrderResponse updated = buildOrderResponse("order-1");
        when(orderService.updateOrderStatus(eq("order-1"), any(), eq(COMPANY_ID))).thenReturn(updated);

        // act & assert
        mockMvc.perform(put("/smartlogix/order/orders/order-1/status")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    void getAllRegiones_returns200WithList() throws Exception {
        // arrange
        when(orderService.getAllRegiones()).thenReturn(List.of(new RegionResponse(1, "Región Metropolitana")));

        // act & assert
        mockMvc.perform(get("/smartlogix/order/regiones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].nombre").value("Región Metropolitana"));
    }

    @Test
    void getComunasByRegion_returns200WithList() throws Exception {
        // arrange
        when(orderService.getComunasByRegion(1)).thenReturn(List.of(new ComunaResponse(101, "Santiago", 1)));

        // act & assert
        mockMvc.perform(get("/smartlogix/order/comunas").param("regionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].nombre").value("Santiago"));
    }

    private OrderResponse buildOrderResponse(String id) {
        return new OrderResponse(id, "Juan Pérez", "juan@empresa.cl", "Av. 123",
                101, "Santiago", "Región Metropolitana",
                OrderStatus.PENDING, new BigDecimal("1000"),
                LocalDateTime.now(), LocalDateTime.now(), List.of(), COMPANY_ID);
    }
}
