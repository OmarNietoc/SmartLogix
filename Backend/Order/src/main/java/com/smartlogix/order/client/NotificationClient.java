package com.smartlogix.order.client;

import com.smartlogix.order.dto.CreateNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-notification", path = "/smartlogix/notification/notifications")
public interface NotificationClient {

    @PostMapping
    void sendNotification(@RequestBody CreateNotificationRequest request);
}