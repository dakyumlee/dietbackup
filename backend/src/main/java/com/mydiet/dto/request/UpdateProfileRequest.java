package com.mydiet.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    private String nickname;
    private String email;
    private Double weightGoal;
    private Double currentWeight;
    private Double height;
    private String emotionMode;
    private Integer dailyCalorieGoal;
    private Integer weeklyWorkoutGoal;
    private Integer dailyProteinGoal;
    private Integer dailyWaterGoal;
    private String activityLevel;
    private String gender;
    private Integer age;
}