package com.mydiet.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MealRequest {
    private Long userId;
    private String description;
    private String photoUrl;
    private Integer caloriesEstimate;
    private LocalDate date;
    
    private String mealType;
}
