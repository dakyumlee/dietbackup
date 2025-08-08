package com.mydiet.controller;

import com.mydiet.config.ClaudeApiClient;
import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import com.mydiet.repository.MealLogRepository;
import com.mydiet.repository.WorkoutLogRepository;
import com.mydiet.repository.EmotionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ClaudeController {

    private final ClaudeApiClient claudeApiClient;
    private final UserRepository userRepository;
    private final MealLogRepository mealLogRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final EmotionLogRepository emotionLogRepository;

    @GetMapping("/claude/message")
    public ResponseEntity<String> getClaudeMessage(@RequestParam(defaultValue = "1") Long userId) {
        log.info("=== Claude 메시지 요청: userId={} ===", userId);
        
        return ResponseEntity.ok("오늘도 건강한 하루 보내세요! 💪 화이팅!");
    }

    @PostMapping("/ai/ask")
    public ResponseEntity<Map<String, Object>> askClaude(@RequestBody Map<String, Object> request) {
        log.info("=== AI 질문 요청 ===");
        
        try {
            String question = (String) request.get("question");
            Long userId = request.get("userId") != null ? 
                Long.valueOf(request.get("userId").toString()) : 1L;
            
            if (question == null || question.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "질문을 입력해주세요"
                ));
            }
            
            User user = userRepository.findById(userId).orElse(null);
            
            LocalDate today = LocalDate.now();
            var meals = mealLogRepository.findByUserIdAndDate(userId, today);
            var workouts = workoutLogRepository.findByUserIdAndDate(userId, today);
            var emotions = emotionLogRepository.findByUserIdAndDate(userId, today);
            
            StringBuilder context = new StringBuilder();
            context.append("=== 사용자 정보 ===\n");
            if (user != null) {
                context.append("닉네임: ").append(user.getNickname()).append("\n");
                context.append("목표 체중: ").append(user.getWeightGoal()).append("kg\n");
                context.append("감정 모드: ").append(user.getEmotionMode()).append("\n");
            } else {
                context.append("새로운 사용자\n");
            }
            
            context.append("\n=== 오늘의 식단 ===\n");
            if (meals.isEmpty()) {
                context.append("아직 기록된 식단이 없습니다.\n");
            } else {
                int totalCalories = 0;
                for (var meal : meals) {
                    context.append("- ").append(meal.getDescription())
                           .append(" (").append(meal.getCaloriesEstimate()).append("kcal)\n");
                    totalCalories += meal.getCaloriesEstimate() != null ? meal.getCaloriesEstimate() : 0;
                }
                context.append("총 섭취: ").append(totalCalories).append("kcal\n");
            }
            
            context.append("\n=== 오늘의 운동 ===\n");
            if (workouts.isEmpty()) {
                context.append("아직 기록된 운동이 없습니다.\n");
            } else {
                int totalBurned = 0;
                for (var workout : workouts) {
                    context.append("- ").append(workout.getType())
                           .append(" ").append(workout.getDuration()).append("분")
                           .append(" (").append(workout.getCaloriesBurned()).append("kcal)\n");
                    totalBurned += workout.getCaloriesBurned() != null ? workout.getCaloriesBurned() : 0;
                }
                context.append("총 소모: ").append(totalBurned).append("kcal\n");
            }
            
            context.append("\n=== 오늘의 감정 ===\n");
            if (emotions.isEmpty()) {
                context.append("아직 기록된 감정이 없습니다.\n");
            } else {
                for (var emotion : emotions) {
                    context.append("- ").append(emotion.getMood())
                           .append(": ").append(emotion.getNote()).append("\n");
                }
            }
            
            context.append("\n사용자 질문: ").append(question).append("\n\n");
            context.append("위 정보를 바탕으로 ");
            context.append(user != null ? user.getEmotionMode() : "다정한");
            context.append(" 톤으로 도움이 되는 조언을 150자 이내로 해주세요.");
            
            String answer = claudeApiClient.askClaude(context.toString());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "question", question,
                "answer", answer,
                "context", Map.of(
                    "meals", meals.size(),
                    "workouts", workouts.size(),
                    "emotions", emotions.size()
                )
            ));
            
        } catch (Exception e) {
            log.error("AI 질문 처리 실패", e);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "question", request.get("question"),
                "answer", "죄송합니다. 현재 응답을 생성할 수 없습니다. 잠시 후 다시 시도해주세요."
            ));
        }
    }
}