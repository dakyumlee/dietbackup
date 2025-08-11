package com.mydiet.controller;

import com.mydiet.model.*;
import com.mydiet.repository.*;
import com.mydiet.service.ClaudeAIService;
import com.mydiet.util.SessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final SessionUtil sessionUtil;
    private final UserRepository userRepository;
    private final MealLogRepository mealLogRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final EmotionLogRepository emotionLogRepository;
    private final ClaudeAIService claudeAIService;

    @GetMapping("/daily-advice")
    public ResponseEntity<String> getDailyMessage(HttpServletRequest request) {
        try {
            Long userId = sessionUtil.getCurrentUserId(request);
            if (userId == null) {
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body("사용자 정보를 찾을 수 없습니다.");
            }

            LocalDate today = LocalDate.now();
            List<MealLog> todayMeals = mealLogRepository.findByUserIdAndDate(userId, today);
            List<WorkoutLog> todayWorkouts = workoutLogRepository.findByUserIdAndDate(userId, today);
            List<EmotionLog> todayEmotions = emotionLogRepository.findByUserIdAndDate(userId, today);

            String prompt = buildDailyAdvicePrompt(user, todayMeals, todayWorkouts, todayEmotions);
            String advice = claudeAIService.generateAdvice(prompt);
            
            return ResponseEntity.ok(advice);

        } catch (Exception e) {
            log.error("일일 AI 메시지 생성 실패", e);
            return ResponseEntity.ok("오늘도 건강한 하루 보내세요! 💪");
        }
    }

    @PostMapping("/question")
    public ResponseEntity<Map<String, String>> askQuestion(@RequestBody Map<String, String> request, 
                                            HttpServletRequest httpRequest) {
        try {
            Long userId = sessionUtil.getCurrentUserId(httpRequest);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
            }

            String question = request.get("question");
            if (question == null || question.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "질문을 입력해주세요."));
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "사용자 정보를 찾을 수 없습니다."));
            }

            LocalDate today = LocalDate.now();
            List<MealLog> todayMeals = mealLogRepository.findByUserIdAndDate(userId, today);
            List<WorkoutLog> todayWorkouts = workoutLogRepository.findByUserIdAndDate(userId, today);
            List<EmotionLog> todayEmotions = emotionLogRepository.findByUserIdAndDate(userId, today);

            String prompt = buildQuestionPrompt(user, todayMeals, todayWorkouts, todayEmotions, question);
            String answer = claudeAIService.generateAnswer(prompt);
            
            return ResponseEntity.ok(Map.of("answer", answer));

        } catch (Exception e) {
            log.error("AI 질문 답변 실패", e);
            return ResponseEntity.ok(Map.of("answer", "죄송합니다. 현재 응답을 생성할 수 없습니다."));
        }
    }

    private String buildDailyAdvicePrompt(User user, List<MealLog> meals, List<WorkoutLog> workouts, List<EmotionLog> emotions) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("=== MyDiet AI 코치 ===\n");
        prompt.append("당신은 ").append(user.getEmotionMode() != null ? user.getEmotionMode() : "다정한").append(" 스타일의 AI 다이어트 코치입니다.\n\n");
        
        prompt.append("=== 사용자 정보 ===\n");
        prompt.append("닉네임: ").append(user.getNickname() != null ? user.getNickname() : "사용자").append("\n");
        if (user.getWeightGoal() != null) {
            prompt.append("목표 체중: ").append(user.getWeightGoal()).append("kg\n");
        }
        prompt.append("코칭 스타일: ").append(user.getEmotionMode() != null ? user.getEmotionMode() : "다정함").append("\n");
        
        prompt.append("\n=== 오늘의 식단 ===\n");
        if (meals.isEmpty()) {
            prompt.append("아직 기록된 식단이 없습니다.\n");
        } else {
            int totalCalories = 0;
            for (MealLog meal : meals) {
                prompt.append("- ").append(meal.getDescription()).append(" (").append(meal.getCaloriesEstimate()).append(" kcal)\n");
                totalCalories += meal.getCaloriesEstimate() != null ? meal.getCaloriesEstimate() : 0;
            }
            prompt.append("총 섭취 칼로리: ").append(totalCalories).append(" kcal\n");
        }
        
        prompt.append("\n=== 오늘의 운동 ===\n");
        if (workouts.isEmpty()) {
            prompt.append("아직 기록된 운동이 없습니다.\n");
        } else {
            int totalBurned = 0;
            for (WorkoutLog workout : workouts) {
                prompt.append("- ").append(workout.getType()).append(" ").append(workout.getDuration()).append("분 (").append(workout.getCaloriesBurned()).append(" kcal 소모)\n");
                totalBurned += workout.getCaloriesBurned() != null ? workout.getCaloriesBurned() : 0;
            }
            prompt.append("총 소모 칼로리: ").append(totalBurned).append(" kcal\n");
        }
        
        prompt.append("\n=== 오늘의 감정 ===\n");
        if (emotions.isEmpty()) {
            prompt.append("아직 기록된 감정이 없습니다.\n");
        } else {
            for (EmotionLog emotion : emotions) {
                prompt.append("- 기분: ").append(emotion.getMood()).append("\n");
                if (emotion.getNote() != null && !emotion.getNote().trim().isEmpty()) {
                    prompt.append("  메모: ").append(emotion.getNote()).append("\n");
                }
            }
        }
        
        prompt.append("\n=== 조언 요청 ===\n");
        prompt.append("위 정보를 바탕으로 사용자에게 도움이 되는 일일 조언을 제공해주세요.\n");
        prompt.append("- 길이: 150자 이내\n");
        prompt.append("- 톤: ").append(user.getEmotionMode() != null ? user.getEmotionMode() : "다정함").append(" 스타일\n");
        prompt.append("- 구체적이고 실행 가능한 조언 포함\n");
        prompt.append("- 건강과 안전을 최우선으로 고려\n");
        
        return prompt.toString();
    }

    private String buildQuestionPrompt(User user, List<MealLog> meals, List<WorkoutLog> workouts, List<EmotionLog> emotions, String question) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("=== MyDiet AI 코치 ===\n");
        prompt.append("당신은 ").append(user.getEmotionMode() != null ? user.getEmotionMode() : "다정한").append(" 스타일의 AI 다이어트 코치입니다.\n\n");
        
        prompt.append("=== 사용자 정보 ===\n");
        prompt.append("닉네임: ").append(user.getNickname() != null ? user.getNickname() : "사용자").append("\n");
        if (user.getWeightGoal() != null) {
            prompt.append("목표 체중: ").append(user.getWeightGoal()).append("kg\n");
        }
        
        prompt.append("\n=== 오늘의 현황 ===\n");
        
        int totalCalories = meals.stream().mapToInt(m -> m.getCaloriesEstimate() != null ? m.getCaloriesEstimate() : 0).sum();
        int totalBurned = workouts.stream().mapToInt(w -> w.getCaloriesBurned() != null ? w.getCaloriesBurned() : 0).sum();
        
        prompt.append("식단: ").append(meals.size()).append("회 (").append(totalCalories).append(" kcal)\n");
        prompt.append("운동: ").append(workouts.size()).append("회 (").append(totalBurned).append(" kcal 소모)\n");
        prompt.append("감정기록: ").append(emotions.size()).append("회\n");
        
        if (!emotions.isEmpty()) {
            EmotionLog latestEmotion = emotions.get(emotions.size() - 1);
            prompt.append("최근 기분: ").append(latestEmotion.getMood()).append("\n");
        }
        
        prompt.append("\n=== 사용자 질문 ===\n");
        prompt.append(question).append("\n\n");
        
        prompt.append("=== 답변 가이드라인 ===\n");
        prompt.append("- 사용자의 현재 상황을 고려한 맞춤형 답변\n");
        prompt.append("- 길이: 200자 이내\n");
        prompt.append("- 톤: ").append(user.getEmotionMode() != null ? user.getEmotionMode() : "다정함").append(" 스타일\n");
        prompt.append("- 구체적이고 실행 가능한 조언\n");
        prompt.append("- 건강과 안전을 최우선으로 고려\n");
        
        return prompt.toString();
    }
}