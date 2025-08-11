package com.mydiet.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EmotionRequest {
    private Long userId;
    private String mood;
    private String note;
    private Integer stressLevel;
    private LocalDate date;
    private Integer energyLevel;
}