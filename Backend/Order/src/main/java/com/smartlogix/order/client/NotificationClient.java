package com.smartlogix.order.client;

import com.smartlogix.order.dto.CreateNotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class NotificationClient {

    private final RestTemplate restTemplate;

    @Value("${notification.service.url}")
    private String notificationServiceUrl;

    public void sendNotification(CreateNotificationRequest request) {
        try {
            String url = notificationServiceUrl + "/api/notifications";
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            System.out.println("==========================================");
            System.out.println("NOTIFICACIÓN ENVIADA DESDE ORDER SERVICE");
            System.out.println("Respuesta del notification-service: " + response.getStatusCode());
            System.out.println("==========================================");
        } catch (Exception e) {
            System.out.println("==========================================");
            System.out.println("ERROR AL ENVIAR NOTIFICACIÓN");
            System.out.println("Detalle: " + e.getMessage());
            System.out.println("==========================================");
        }
    }
}