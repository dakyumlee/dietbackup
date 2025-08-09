package com.mydiet.model;
import com.mydiet.model.Role;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "meal_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MealLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String description;
    
    private Integer caloriesEstimate;
    
    private String photoUrl;
    
    private String mealType;
    
    @Column(nullable = false)
    private LocalDate date;
    
    private Double protein;
    private Double carbohydrate;
    private Double fat;
    private Double fiber;
    private Double sugar;
    private Double sodium;
    
    private String note;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public String getMealTypeKorean() {
        switch (mealType != null ? mealType.toLowerCase() : "snack") {
            case "breakfast": return "아침";
            case "lunch": return "점심";
            case "dinner": return "저녁";
            default: return "간식";
        }
    }
    
    public boolean isToday() {
        return date != null && date.equals(LocalDate.now());
    }
}