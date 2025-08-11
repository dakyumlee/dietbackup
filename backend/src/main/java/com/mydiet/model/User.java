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

    @Column(name = "weight_goal")
    private Double weightGoal;
    
    @Column(name = "emotion_mode")
    private String emotionMode;  
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;  

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}