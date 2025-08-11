package com.mydiet.dto;

import lombok.Data;

@Data
public class InbodyDataRequest {
    private Double weight;
    private Double bodyFatPercentage;
    private Double muscleMass;
    private Double bodyWaterPercentage;
    private Double basalMetabolicRate;
    private Double visceralFatLevel;
    private String notes;
}