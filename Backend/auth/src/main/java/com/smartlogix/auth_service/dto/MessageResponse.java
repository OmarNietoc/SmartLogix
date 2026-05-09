package com.smartlogix.auth_service.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse<T> {
    private int statusCode;
    private String message;
    private T data;
}
