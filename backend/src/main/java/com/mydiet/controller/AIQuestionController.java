package com.mydiet.controller;

import com.mydiet.config.ClaudeApiClient;
import com.mydiet.model.User;
import com.mydiet.model.MealLog;
import com.mydiet.model.WorkoutLog;
import com.mydiet.model.EmotionLog;
import com.mydiet.repository.UserRepository;
import com.mydiet.repository.MealLogRepository;
import com.mydiet.repository.WorkoutLogRepository;
import com.mydiet.repository.EmotionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AIQuestionController {
    
    private final ClaudeApiClient claudeApiClient;
    private final UserRepository userRepository;
    private final MealLogRepository mealLogRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final EmotionLogRepository emotionLogRepository;
    
    @PostMapping("/ask")
    public ResponseEntity<?> askQuestion(@RequestBody Map<String, Object> request, HttpSession session) {
        try {
            String question = (String) request.get("question");
            Long userId = (Long) session.getAttribute("userId");
            
            log.info("AI question from user {}: {}", userId, question);
            
            if (question == null || question.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "error", "질문을 입력해주세요."
                ));
            }
            
            String context = getUserContext(userId);
            
            String fullPrompt = context + "\n\n사용자 질문: " + question + "\n\n위 정보를 바탕으로 답변해주세요.";
            String answer = claudeApiClient.askClaude(fullPrompt);
            
            log.info("Claude response: {}", answer);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "answer", answer != null ? answer : "죄송합니다. 현재 응답을 생성할 수 없습니다."
            ));
            
        } catch (Exception e) {
            log.error("AI 질문 처리 실패", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "answer", "오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            ));
        }
    }
    
    private String getUserContext(Long userId) {
        try {
            if (userId == null) {
                return "사용자 정보가 없습니다.";
            }
            
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return "사용자를 찾을 수 없습니다.";
            }
            
            LocalDate today = LocalDate.now();
            List<MealLog> todayMeals = mealLogRepository.findByUserIdAndDate(userId, today);
            List<WorkoutLog> todayWorkouts = workoutLogRepository.findByUserIdAndDate(userId, today);
            List<EmotionLog> todayEmotions = emotionLogRepository.findByUserIdAndDate(userId, today);
            
            StringBuilder context = new StringBuilder();
            context.append("사용자 정보:\n");
            context.append("- 닉네임: ").append(user.getNickname()).append("\n");
            context.append("- 목표 체중: ").append(user.getWeightGoal()).append("kg\n");
            context.append("- 감정 모드: ").append(user.getEmotionMode()).append("\n\n");
            
            context.append("오늘의 기록:\n");
            
            if (!todayMeals.isEmpty()) {
                context.append("식단: ");
                int totalCalories = 0;
                for (MealLog meal : todayMeals) {
                    context.append(meal.getDescription()).append(" ");
                    totalCalories += meal.getCaloriesEstimate() != null ? meal.getCaloriesEstimate() : 0;
                }
                context.append("(총 ").append(totalCalories).append("kcal)\n");
            } else {
                context.append("식단: 기록 없음\n");
            }
            
            if (!todayWorkouts.isEmpty()) {
                context.append("운동: ");
                int totalDuration = 0;
                for (WorkoutLog workout : todayWorkouts) {
                    context.append(workout.getType()).append(" ");
                    totalDuration += workout.getDuration() != null ? workout.getDuration() : 0;
                }
                context.append("(총 ").append(totalDuration).append("분)\n");
            } else {
                context.append("운동: 기록 없음\n");
            }
            
            if (!todayEmotions.isEmpty()) {
                context.append("감정: ");
                for (EmotionLog emotion : todayEmotions) {
                    context.append(emotion.getMood()).append(" ");
                }
                context.append("\n");
            } else {
                context.append("감정: 기록 없음\n");
            }
            
            return context.toString();
            
        } catch (Exception e) {
            log.error("사용자 컨텍스트 생성 실패", e);
            return "사용자 정보를 가져오는 데 실패했습니다.";
        }
    }
}