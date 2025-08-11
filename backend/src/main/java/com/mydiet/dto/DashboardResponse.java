package com.mydiet.dto;

import com.mydiet.model.EmotionLog;
import com.mydiet.model.MealLog;
import com.mydiet.model.WorkoutLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private LocalDate date;
    private List<MealLog> todayMeals;
    private List<WorkoutLog> todayWorkouts;
    private List<EmotionLog> todayEmotions;
    private Integer totalCalories;
    private Integer totalCaloriesBurned;
    private Integer totalWorkoutDuration;
    private String overallMood;
}
