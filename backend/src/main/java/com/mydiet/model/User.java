package com.mydiet.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
    private String nickname;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private String password;
    
    private String provider;
    
    private String providerId;
    
    @Builder.Default
    private String role = "USER";
    
    @Builder.Default
    private Double weightGoal = 70.0;
    
    @Builder.Default
    private Double currentWeight = 70.0;
    
    @Builder.Default
    private String emotionMode = "다정함";
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (this.role == null) {
            this.role = "USER";
        }
        if (this.weightGoal == null) {
            this.weightGoal = 70.0;
        }
        if (this.currentWeight == null) {
            this.currentWeight = 70.0;
        }
        if (this.emotionMode == null) {
            this.emotionMode = "다정함";
        }
    }
}