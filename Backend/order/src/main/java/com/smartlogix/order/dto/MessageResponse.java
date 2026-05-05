package com.smartlogix.order.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponse<T> {
    private int statusCode;
    private String message;
    private T data;
}
