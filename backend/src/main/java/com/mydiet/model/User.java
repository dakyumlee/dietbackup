package com.mydiet.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    public enum Role {
        USER, ADMIN
    }
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nickname;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private String password;
    private String provider;  
    private String providerId;  

    // 기본 정보
    @Column(name = "weight_goal")
    private Double weightGoal;
    
    @Column(name = "current_weight")
    private Double currentWeight;
    
    private Double height; // cm
    
    private Integer age;
    
    @Column(name = "activity_level")
    @Builder.Default
    private String activityLevel = "MODERATE"; // LOW, MODERATE, HIGH, VERY_HIGH
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @Column(name = "emotion_mode")
    @Builder.Default
    private String emotionMode = "다정함";
    
    @Builder.Default
    private String gender = "기타";
    
    @Column(name = "daily_calorie_goal")
    @Builder.Default
    private Integer dailyCalorieGoal = 2000;
    
    @Column(name = "weekly_workout_goal")
    @Builder.Default
    private Integer weeklyWorkoutGoal = 3;
    
    @Column(name = "daily_water_goal")
    @Builder.Default
    private Integer dailyWaterGoal = 8;
    
    @Column(name = "sleep_goal")
    @Builder.Default
    private Integer sleepGoal = 8;
    
    @Column(name = "health_conditions")
    private String healthConditions; // 질병, 알레르기 등
    
    @Column(name = "medications")
    private String medications; // 복용 중인 약물
    
    @Column(name = "dietary_restrictions")
    private String dietaryRestrictions;
    
    @Column(name = "work_schedule")
    @Builder.Default
    private String workSchedule = "일반";
    
    @Column(name = "stress_level")
    @Builder.Default
    private String stressLevel = "보통";
    
    @Column(name = "sleep_quality")
    @Builder.Default
    private String sleepQuality = "보통";
    
    @Column(name = "preferred_workout_types")
    private String preferredWorkoutTypes;
    
    @Column(name = "workout_time_preference")
    @Builder.Default
    private String workoutTimePreference = "저녁";
    
    @Column(name = "notification_enabled")
    @Builder.Default
    private Boolean notificationEnabled = true;
    
    @Column(name = "reminder_frequency")
    @Builder.Default
    private String reminderFrequency = "일반";
    
    @Column(name = "start_weight")
    private Double startWeight;
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "target_date")
    private LocalDateTime targetDate;
    
    @Column(columnDefinition = "TEXT")
    private String personalNotes;
    
    @Column(columnDefinition = "TEXT")
    private String motivationQuote;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (startDate == null) {
            startDate = now;
        }
        if (startWeight == null && currentWeight != null) {
            startWeight = currentWeight;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public Double getBmi() {
        if (height != null && currentWeight != null && height > 0) {
            return Math.round((currentWeight / Math.pow(height / 100, 2)) * 10.0) / 10.0;
        }
        return null;
    }
    
    public String getBmiCategory() {
        Double bmi = getBmi();
        if (bmi == null) return "정보 없음";
        
        if (bmi < 18.5) return "저체중";
        else if (bmi < 23) return "정상";
        else if (bmi < 25) return "과체중";
        else if (bmi < 30) return "비만 1단계";
        else return "비만 2단계";
    }
    
    public Double getRemainingWeight() {
        if (currentWeight != null && weightGoal != null) {
            return Math.round((currentWeight - weightGoal) * 10.0) / 10.0;
        }
        return null;
    }
    
    public Double getWeightProgress() {
        if (startWeight != null && currentWeight != null && weightGoal != null) {
            double total = Math.abs(startWeight - weightGoal);
            double progress = Math.abs(startWeight - currentWeight);
            return total > 0 ? Math.round((progress / total * 100) * 10.0) / 10.0 : 0.0;
        }
        return null;
    }
}