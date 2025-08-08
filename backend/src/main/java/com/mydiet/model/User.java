package com.mydiet.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String nickname;
    
    private String provider;
    private String providerId;
    private String profileImageUrl;
    
    private Double height; // cm
    private Double currentWeight; // kg
    private Double weightGoal; // kg
    private String gender; // MALE, FEMALE, OTHER
    private Integer age;
    
    // 목표 설정
    private Integer dailyCalorieGoal = 1500;
    private Integer weeklyWorkoutGoal = 3;
    private Integer dailyProteinGoal = 60;
    private Integer dailyWaterGoal = 8;
    private String activityLevel = "MODERATE";
    
    // AI 설정
    private String emotionMode = "다정함"; // 다정함, 츤데레, 무자비, 격려
    private String adviceFrequency = "MEDIUM";
    private Boolean dietAdvice = true;
    private Boolean workoutAdvice = true;
    private Boolean emotionAdvice = true;
    private Boolean motivationAdvice = true;
    
    // 알림 설정
    private Boolean mealNotifications = true;
    private Boolean workoutNotifications = true;
    private Boolean waterNotifications = true;
    private Boolean goalNotifications = true;
    private Boolean aiNotifications = true;
    
    // 개인정보 설정
    private Boolean dataAnalysis = true;
    private Boolean personalizedAds = false;
    private Boolean dataSharing = false;
    
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // 연관관계
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MealLog> mealLogs;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkoutLog> workoutLogs;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmotionLog> emotionLogs;
    
    public enum Role {
        USER, ADMIN
    }
    
    // 편의 메서드
    public String getDisplayName() {
        return nickname != null ? nickname : email;
    }
    
    public boolean isAdmin() {
        return Role.ADMIN.equals(role);
    }
    
    // BMI 계산
    public Double getBmi() {
        if (height != null && currentWeight != null && height > 0) {
            double heightInM = height / 100.0;
            return currentWeight / (heightInM * heightInM);
        }
        return null;
    }
    
    // 목표까지 남은 체중
    public Double getRemainingWeight() {
        if (currentWeight != null && weightGoal != null) {
            return currentWeight - weightGoal;
        }
        return null;
    }
}