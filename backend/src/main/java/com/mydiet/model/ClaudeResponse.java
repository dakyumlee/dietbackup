package com.mydiet.model;
import com.mydiet.model.Role;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "claude_responses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ClaudeResponse {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String type;
    
    @Column(length = 2000)
    private String content;
    
    @Column(length = 1000)
    private String prompt;
    
    private String emotionMode;
    
    private Boolean isPositive;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    public String getTypeKorean() {
        switch (type != null ? type : "daily") {
            case "daily": return "일일 피드백";
            case "question": return "질문 응답";
            case "advice": return "조언";
            case "motivation": return "동기부여";
            default: return "기타";
        }
    }
    
    public boolean isRecent() {
        if (createdAt == null) return false;
        return createdAt.isAfter(LocalDateTime.now().minusHours(24));
    }
}