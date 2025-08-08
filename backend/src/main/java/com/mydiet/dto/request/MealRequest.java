package com.mydiet.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealRequest {
    private String description;
    private Integer caloriesEstimate;
    private String photoUrl;
    private String mealType;
    private Double protein;
    private Double carbohydrate;
    private Double fat;
    private String note;
}