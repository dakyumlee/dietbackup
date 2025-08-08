package com.mydiet.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "emotion_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class EmotionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String mood;
    
    private Integer stressLevel; // 스트레스 레벨 (1-10)
    
    private Integer energyLevel; // 에너지 레벨 (1-10)
    
    private Integer sleepQuality; // 수면 품질 (1-10)
    
    @Column(nullable = false)
    private LocalDate date; // 기록 날짜
    
    @Column(length = 1000)
    private String note; // 상세 메모
    
    @Column(length = 1000)
    private String dietFeeling;
    private String tags;
    
    private String triggers;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public String getMoodEmoji() {
        switch (mood != null ? mood : "보통") {
            case "매우좋음": return "😍";
            case "좋음": return "😊";
            case "보통": return "😐";
            case "나쁨": return "😞";
            case "매우나쁨": return "😭";
            case "스트레스": return "😤";
            case "피곤함": return "😴";
            case "활기참": return "🤗";
            default: return "😐";
        }
    }
    
    public String getStressLevelText() {
        if (stressLevel == null) return "보통";
        if (stressLevel <= 3) return "낮음";
        if (stressLevel <= 7) return "보통";
        return "높음";
    }
    
    public boolean isToday() {
        return date != null && date.equals(LocalDate.now());
    }
    
    public boolean isPositive() {
        return mood != null && (mood.contains("좋음") || mood.equals("활기참"));
    }
}
