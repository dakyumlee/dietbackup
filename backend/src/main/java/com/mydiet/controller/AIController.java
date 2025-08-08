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
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final ClaudeApiClient claudeApiClient;
    private final UserRepository userRepository;
    private final MealLogRepository mealLogRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final EmotionLogRepository emotionLogRepository;

    @PostMapping("/question")
    public ResponseEntity<Map<String, Object>> askQuestion(@RequestBody Map<String, Object> request) {
        log.info("=== AI 질문 요청 ===");
        
        try {
            String question = (String) request.get("question");
            Long userId = 1L;
            
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
                context.append("총 섭취 칼로리: ").append(totalCalories).append("kcal\n");
            }
            
            context.append("\n=== 오늘의 운동 ===\n");
            if (workouts.isEmpty()) {
                context.append("아직 기록된 운동이 없습니다.\n");
            } else {
                int totalBurned = 0;
                for (var workout : workouts) {
                    context.append("- ").append(workout.getType())
                           .append(" ").append(workout.getDuration()).append("분")
                           .append(" (").append(workout.getCaloriesBurned()).append("kcal 소모)\n");
                    totalBurned += workout.getCaloriesBurned() != null ? workout.getCaloriesBurned() : 0;
                }
                context.append("총 소모 칼로리: ").append(totalBurned).append("kcal\n");
            }
            
            context.append("\n=== 오늘의 감정 ===\n");
            if (emotions.isEmpty()) {
                context.append("아직 기록된 감정이 없습니다.\n");
            } else {
                for (var emotion : emotions) {
                    context.append("- 기분: ").append(emotion.getMood())
                           .append(" | 메모: ").append(emotion.getNote()).append("\n");
                }
            }
            
            context.append("\n=== 사용자 질문 ===\n");
            context.append(question).append("\n\n");
            
            context.append("위 정보를 바탕으로 친근하고 도움이 되는 조언을 200자 이내로 해주세요. ");
            context.append("사용자의 감정 모드(").append(user != null ? user.getEmotionMode() : "다정함")
                   .append(")에 맞는 톤으로 답변해주세요.");
            
            String response = claudeApiClient.askClaude(context.toString());
            
            log.info("AI 응답 생성 완료 (사용자: {}, 질문 길이: {})", 
                    user != null ? user.getNickname() : "Unknown", question.length());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "question", question,
                "answer", response,
                "context", Map.of(
                    "mealCount", meals.size(),
                    "workoutCount", workouts.size(),
                    "emotionCount", emotions.size()
                )
            ));
            
        } catch (Exception e) {
            log.error("AI 질문 처리 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "AI 서비스가 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해주세요.",
                "details", e.getMessage()
            ));
        }
    }

    @GetMapping("/daily-advice")
    public ResponseEntity<Map<String, Object>> getDailyAdvice() {
        log.info("=== 일일 조언 요청 ===");
        
        try {
            Map<String, Object> questionRequest = Map.of(
                "question", "오늘 하루 어떻게 보냈는지 보고 조언해주세요."
            );
            
            return askQuestion(questionRequest);
            
        } catch (Exception e) {
            log.error("일일 조언 생성 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "일일 조언을 생성할 수 없습니다."
            ));
        }
    }
}