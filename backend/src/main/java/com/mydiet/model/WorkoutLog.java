package com.mydiet.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "workout_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class WorkoutLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String type; // 운동 종류 (걷기, 달리기, 웨이트 등)
    
    private Integer duration; // 운동 시간 (분)
    
    private Integer caloriesBurned; // 소모 칼로리
    
    private String intensity; // 강도 (낮음, 보통, 높음)
    
    private String category; // cardio, strength, flexibility, sports
    
    @Column(nullable = false)
    private LocalDate date; // 운동 날짜
    
    private String sets; // 세트 정보
    private Double distance; // 거리 (km)
    private Integer heartRate; // 평균 심박수
    private String location; // 운동 장소
    
    private String note; // 메모
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public String getCategoryKorean() {
        switch (category != null ? category.toLowerCase() : "cardio") {
            case "cardio": return "유산소";
            case "strength": return "근력";
            case "flexibility": return "유연성";
            case "sports": return "스포츠";
            default: return "기타";
        }
    }
    
    public String getIntensityKorean() {
        switch (intensity != null ? intensity : "보통") {
            case "낮음": return "🟢 낮음";
            case "보통": return "🟡 보통";
            case "높음": return "🔴 높음";
            default: return "🟡 보통";
        }
    }
    
    public boolean isToday() {
        return date != null && date.equals(LocalDate.now());
    }
}
