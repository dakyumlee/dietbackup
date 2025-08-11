package com.mydiet.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inbody_data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InbodyData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private Double weight;
    
    @Column(name = "body_fat_percentage")
    private Double bodyFatPercentage;
    
    @Column(name = "muscle_mass")
    private Double muscleMass;
    
    @Column(name = "body_water_percentage")
    private Double bodyWaterPercentage;
    
    @Column(name = "basal_metabolic_rate")
    private Double basalMetabolicRate;
    
    @Column(name = "visceral_fat_level")
    private Double visceralFatLevel;
    
    private String notes;
    
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
    
    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }
}