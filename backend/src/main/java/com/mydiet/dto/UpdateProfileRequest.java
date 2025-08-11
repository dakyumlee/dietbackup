package com.mydiet.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String nickname;
    private String email;
    private Double weightGoal;
    private String emotionMode;
}