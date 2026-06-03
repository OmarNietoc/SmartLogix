package com.smartlogix.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogix.notification.config.SecurityConfig;
import com.smartlogix.notification.dto.CreateNotificationRequest;
import com.smartlogix.notification.dto.NotificationResponse;
import com.smartlogix.notification.model.NotificationStatus;
import com.smartlogix.notification.model.NotificationType;
import com.smartlogix.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import(SecurityConfig.class)
class NotificationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean NotificationService notificationService;

    @Test
    void createNotification_validRequest_returns201() throws Exception {
        // arrange
        CreateNotificationRequest request = new CreateNotificationRequest("order-1", "user@empresa.cl", "Pedido creado", "Tu pedido fue creado");
        NotificationResponse response = buildNotificationResponse("notif-1");
        when(notificationService.createNotification(any())).thenReturn(response);

        // act & assert
        mockMvc.perform(post("/smartlogix/notification/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("notif-1"))
                .andExpect(jsonPath("$.statusCode").value(201));
    }

    @Test
    void createNotification_invalidEmail_returns400() throws Exception {
        // arrange
        CreateNotificationRequest request = new CreateNotificationRequest("order-1", "not-valid", "Asunto", "Mensaje");

        // act & assert
        mockMvc.perform(post("/smartlogix/notification/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllNotifications_returns200WithList() throws Exception {
        // arrange
        when(notificationService.getAllNotifications()).thenReturn(List.of(buildNotificationResponse("notif-1")));

        // act & assert
        mockMvc.perform(get("/smartlogix/notification/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("notif-1"))
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    void getUnread_returns200WithUnreadList() throws Exception {
        // arrange
        when(notificationService.getUnreadInApp()).thenReturn(List.of(buildNotificationResponse("notif-2")));

        // act & assert
        mockMvc.perform(get("/smartlogix/notification/notifications/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("notif-2"));
    }

    @Test
    void markAsRead_existingNotification_returns200() throws Exception {
        // arrange
        NotificationResponse read = buildNotificationResponse("notif-1");
        when(notificationService.markAsRead("notif-1")).thenReturn(read);

        // act & assert
        mockMvc.perform(patch("/smartlogix/notification/notifications/notif-1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("notif-1"));
    }

    @Test
    void getNotificationById_found_returns200() throws Exception {
        // arrange
        when(notificationService.getNotificationById("notif-1")).thenReturn(buildNotificationResponse("notif-1"));

        // act & assert
        mockMvc.perform(get("/smartlogix/notification/notifications/notif-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("notif-1"));
    }

    @Test
    void getNotificationsByOrderId_returns200WithList() throws Exception {
        // arrange
        when(notificationService.getNotificationsByOrderId("order-1")).thenReturn(List.of(buildNotificationResponse("notif-1")));

        // act & assert
        mockMvc.perform(get("/smartlogix/notification/notifications/order/order-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("notif-1"));
    }

    private NotificationResponse buildNotificationResponse(String id) {
        return new NotificationResponse(id, "order-1", "user@empresa.cl",
                "Pedido creado", "Tu pedido fue procesado",
                NotificationStatus.SENT, NotificationType.IN_APP, false, LocalDateTime.now());
    }
}
