package com.mydiet.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String nickname;
    private String email;
    private String password;
    private Double weightGoal;
    private String emotionMode;
}