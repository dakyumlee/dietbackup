package com.mydiet.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    private String message;
    private String code;
    private long timestamp;
    
    public static ErrorResponse of(String message) {
        return ErrorResponse.builder()
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }
}