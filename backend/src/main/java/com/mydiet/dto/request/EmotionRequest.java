package com.mydiet.dto.request;
import com.mydiet.model.Role;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmotionRequest {
    private String mood;
    private Integer stressLevel;
    private Integer energyLevel;
    private Integer sleepQuality;
    private String note;
    private String dietFeeling;
    private String tags;
    private String triggers;
}