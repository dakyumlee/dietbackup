package com.mydiet.dto.request;
import com.mydiet.model.Role;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutRequest {
    private String type;
    private Integer duration;
    private Integer caloriesBurned;
    private String intensity;
    private String category;
    private String sets;
    private Double distance;
    private String note;
}