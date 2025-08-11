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
    
    @Column(name = "role")
    @Builder.Default
    private String role = "USER";  

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (role == null) {
            role = "USER";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getRole() {
        return this.role != null ? this.role : "USER";
    }
    
    public boolean isAdmin() {
        return "ADMIN".equals(this.role);
    }
    
    public boolean isUser() {
        return "USER".equals(this.role);
    }
}