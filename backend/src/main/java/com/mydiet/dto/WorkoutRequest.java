package com.mydiet.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class WorkoutRequest {
    private Long userId;
    private String type;
    private String intensity;
    private Integer duration;
    private Integer caloriesBurned;
    private LocalDate date;
    private String category;
}