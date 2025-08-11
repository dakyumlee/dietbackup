package com.mydiet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String error;
    private String message;
    private int status;
    
    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.status = 500;
    }
}