package com.mydiet.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String email;
    
    private String nickname;
    private String password;
    private String role = "USER";
    
    @Column(name = "weight_goal")
    private Double weightGoal = 70.0;
    
    @Column(name = "current_weight")
    private Double currentWeight = 70.0;
    
    @Column(name = "emotion_mode")
    private String emotionMode = "다정함";
    
    private String provider;
    
    @Column(name = "provider_id")
    private String providerId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (role == null) {
            role = "USER";
        }
        if (weightGoal == null) {
            weightGoal = 70.0;
        }
        if (currentWeight == null) {
            currentWeight = 70.0;
        }
        if (emotionMode == null) {
            emotionMode = "다정함";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}