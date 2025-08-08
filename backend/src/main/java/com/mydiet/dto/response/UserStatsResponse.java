package com.mydiet.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatsResponse {
    private Long userId;
    private String nickname;
    private String email;
    private Double currentWeight;
    private Double weightGoal;
    private Integer dailyCalorieGoal;
    private Integer weeklyWorkoutGoal;
    private String emotionMode;
    private Double bmi;
    private Double remainingWeight;
    
    // 오늘의 통계
    private Integer consumedCalories;
    private Integer burnedCalories;
    private Integer workoutMinutes;
    private Integer mealCount;
    private String todayMood;
    private Integer goalAchievement;
}