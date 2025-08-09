package com.mydiet.service;
import com.mydiet.model.Role;
import com.mydiet.config.ClaudeApiClient;
import com.mydiet.model.*;
import com.mydiet.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaudeService {

    private final UserRepository userRepository;
    private final MealLogRepository mealLogRepository;
    private final EmotionLogRepository emotionLogRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final ClaudeResponseRepository claudeResponseRepository;
    private final ClaudeApiClient claudeApiClient;

    public String generateDailyResponse(Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            
            LocalDate today = LocalDate.now();
            List<MealLog> meals = mealLogRepository.findByUserIdAndDate(userId, today);
            List<EmotionLog> emotions = emotionLogRepository.findByUserIdAndDate(userId, today);
            List<WorkoutLog> workouts = workoutLogRepository.findByUserIdAndDate(userId, today);

            String prompt = buildPrompt(user, meals, emotions, workouts);
            String response = claudeApiClient.askClaude(prompt);

            ClaudeResponse log = ClaudeResponse.builder()
                .user(user)
                .type("daily")
                .content(response)
                .createdAt(LocalDateTime.now())
                .build();
            claudeResponseRepository.save(log);

            return response;
        } catch (Exception e) {
            log.error("Claude 응답 생성 실패", e);
            return "Claude 응답 생성 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    public String askQuestion(Long userId, String question) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            
            LocalDate today = LocalDate.now();
            List<MealLog> meals = mealLogRepository.findByUserIdAndDate(userId, today);
            List<EmotionLog> emotions = emotionLogRepository.findByUserIdAndDate(userId, today);
            List<WorkoutLog> workouts = workoutLogRepository.findByUserIdAndDate(userId, today);
            
            String contextualPrompt = buildContextualPrompt(user, meals, workouts, emotions, question);
            String response = claudeApiClient.askClaude(contextualPrompt);
            
            ClaudeResponse log = ClaudeResponse.builder()
                .user(user)
                .type("question")
                .prompt(question)
                .content(response)
                .createdAt(LocalDateTime.now())
                .build();
            claudeResponseRepository.save(log);
            
            return response;
        } catch (Exception e) {
            log.error("Claude 질문 처리 실패", e);
            return "죄송합니다. 현재 답변을 생성할 수 없습니다. 잠시 후 다시 시도해주세요.";
        }
    }

    private String buildPrompt(User user, List<MealLog> meals, List<EmotionLog> emotions, List<WorkoutLog> workouts) {
        StringBuilder prompt = new StringBuilder();
    
        prompt.append("유저 닉네임: ").append(user.getNickname()).append("\n");
        prompt.append("목표 체중: ").append(user.getWeightGoal()).append("kg\n");
        prompt.append("감정 모드: ").append(user.getEmotionMode()).append("\n\n");
    
        prompt.append("🥗 오늘 먹은 음식:\n");
        if (meals.isEmpty()) {
            prompt.append("- 없음\n");
        } else {
            for (MealLog meal : meals) {
                prompt.append("- ").append(meal.getDescription());
                if (meal.getCaloriesEstimate() != null) {
                    prompt.append(" (").append(meal.getCaloriesEstimate()).append(" kcal)");
                }
                prompt.append("\n");
            }
        }
    
        prompt.append("\n😊 오늘 감정:\n");
        if (emotions.isEmpty()) {
            prompt.append("- 없음\n");
        } else {
            for (EmotionLog emotion : emotions) {
                prompt.append("- ").append(emotion.getMood());
                if (emotion.getNote() != null && !emotion.getNote().trim().isEmpty()) {
                    prompt.append(": ").append(emotion.getNote());
                }
                prompt.append("\n");
            }
        }
    
        prompt.append("\n🏃 운동 기록:\n");
        if (workouts.isEmpty()) {
            prompt.append("- 없음\n");
        } else {
            for (WorkoutLog workout : workouts) {
                prompt.append("- ").append(workout.getType()).append(" ").append(workout.getDuration()).append("분");
                if (workout.getCaloriesBurned() != null) {
                    prompt.append(" (").append(workout.getCaloriesBurned()).append(" kcal)");
                }
                prompt.append("\n");
            }
        }
    
        prompt.append("\n\n이 유저에게 감정 모드('").append(user.getEmotionMode()).append("')에 맞춰 한국어로 한 마디 해줘.\n");
        prompt.append("응답 형식: 단 한 문장, 감정 모드에 맞는 스타일로. 친근하고 격려하는 톤으로.\n");
    
        return prompt.toString();
    }
    
    private String buildContextualPrompt(User user, List<MealLog> meals, List<WorkoutLog> workouts, 
                                       List<EmotionLog> emotions, String question) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("당신은 MyDiet 앱의 전문 건강 컨설턴트 AI입니다. ");
        prompt.append("사용자의 건강과 다이어트에 대해 친근하고 도움이 되는 조언을 제공해주세요.\n\n");
        
        prompt.append("=== 사용자 정보 ===\n");
        prompt.append("닉네임: ").append(user.getNickname() != null ? user.getNickname() : "사용자").append("\n");
        prompt.append("목표 체중: ").append(user.getWeightGoal() != null ? user.getWeightGoal() + "kg" : "설정 안됨").append("\n");
        prompt.append("감정 모드: ").append(user.getEmotionMode() != null ? user.getEmotionMode() : "보통").append("\n");
        
        prompt.append("\n=== 오늘의 식단 ===\n");
        if (meals.isEmpty()) {
            prompt.append("아직 기록된 식단이 없습니다.\n");
        } else {
            int totalCalories = 0;
            for (MealLog meal : meals) {
                prompt.append("- ").append(meal.getDescription()).append(" (").append(meal.getCaloriesEstimate()).append(" kcal)\n");
                totalCalories += meal.getCaloriesEstimate() != null ? meal.getCaloriesEstimate() : 0;
            }
            prompt.append("총 칼로리: ").append(totalCalories).append(" kcal\n");
        }
        
        prompt.append("\n=== 오늘의 운동 ===\n");
        if (workouts.isEmpty()) {
            prompt.append("아직 기록된 운동이 없습니다.\n");
        } else {
            for (WorkoutLog workout : workouts) {
                prompt.append("- ").append(workout.getType()).append(" ").append(workout.getDuration()).append("분\n");
            }
        }
        
        prompt.append("\n=== 오늘의 감정 ===\n");
        if (emotions.isEmpty()) {
            prompt.append("아직 기록된 감정이 없습니다.\n");
        } else {
            for (EmotionLog emotion : emotions) {
                prompt.append("- ").append(emotion.getMood());
                if (emotion.getNote() != null) {
                    prompt.append(": ").append(emotion.getNote());
                }
                prompt.append("\n");
            }
        }
        
        prompt.append("\n=== 사용자 질문 ===\n");
        prompt.append(question);
        prompt.append("\n\n위 정보를 바탕으로 사용자의 질문에 친근하고 도움이 되는 답변을 해주세요. ");
        prompt.append("감정 모드('").append(user.getEmotionMode()).append("')에 맞는 톤으로 답변해주세요.");
        
        return prompt.toString();
    }
}