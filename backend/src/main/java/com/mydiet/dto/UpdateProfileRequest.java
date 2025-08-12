package com.mydiet.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    private String nickname;
    private Double weightGoal;
    private String emotionMode;
    private Double height;
    private Double currentWeight;

    @Override
    public String toString() {
        return "UpdateProfileRequest{" +
                "nickname='" + nickname + '\'' +
                ", weightGoal=" + weightGoal +
                ", emotionMode='" + emotionMode + '\'' +
                ", height=" + height +
                ", currentWeight=" + currentWeight +
                '}';
    }
}