package com.mydiet.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private Long userId;
    private String message;
    private String chatType;
}