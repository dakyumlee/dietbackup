package com.mydiet.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "emotion_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmotionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;
    
    @Column(nullable = false)
    private String mood;
    
    @Column(name = "stress_level")
    private Integer stressLevel;
    
    @Column(columnDefinition = "TEXT")
    private String note;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (date == null) {
            date = LocalDate.now();
        }
    }
}