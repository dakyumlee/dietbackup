package com.mydiet.controller;

import com.mydiet.model.*;
import com.mydiet.repository.*;
import com.mydiet.config.ClaudeApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/claude")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ClaudeController {
    
    private final MealLogRepository mealLogRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final EmotionLogRepository emotionLogRepository;
    private final UserRepository userRepository;
    private final ClaudeApiClient claudeApiClient;
    
    @GetMapping("/message")
    public ResponseEntity<String> getClaudeMessage(
            @RequestParam(required = false) Long userId,
            HttpSession session) {
        
        log.info("=== Claude 메시지 요청 ===");
        
        if (userId == null) {
            userId = (Long) session.getAttribute("userId");
            if (userId == null) userId = 1L;
        }
        
        try {
            LocalDate today = LocalDate.now();
            List<MealLog> todayMeals = mealLogRepository.findByUserIdAndDate(userId, today);
            List<WorkoutLog> todayWorkouts = workoutLogRepository.findByUserIdAndDate(userId, today);
            List<EmotionLog> todayEmotions = emotionLogRepository.findByUserIdAndDate(userId, today);
            
            Optional<User> userOpt = userRepository.findById(userId);
            String nickname = userOpt.map(User::getNickname).orElse("사용자");
            String emotionMode = userOpt.map(User::getEmotionMode).orElse("GENTLE");
            
            String prompt = buildClaudePrompt(nickname, emotionMode, todayMeals, todayWorkouts, todayEmotions);
            String message = claudeApiClient.askClaude(prompt);
            
            log.info("Claude 메시지 생성 완료 - length: {}", message.length());
            
            return ResponseEntity.ok(message);
            
        } catch (Exception e) {
            log.error("❌ Claude 메시지 생성 실패", e);
            return ResponseEntity.ok("안녕하세요! 오늘도 건강한 하루 보내세요! 💪✨");
        }
    }
    
    @PostMapping("/api/ai/ask")
    public ResponseEntity<Map<String, Object>> askClaude(
            @RequestBody Map<String, Object> request,
            HttpSession session) {
        
        log.info("=== Claude AI 채팅 요청 ===");
        
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) userId = 1L;
        
        String question = (String) request.get("question");
        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "질문을 입력해주세요."));
        }
        
        try {
            String answer = claudeApiClient.askClaude(generateSimpleAnswer(question, userId));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("question", question);
            response.put("answer", answer);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Claude AI 응답 생성 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "AI 응답 생성에 실패했습니다."));
        }
    }
    
    private String buildClaudePrompt(String nickname, String emotionMode, 
                                   List<MealLog> meals, List<WorkoutLog> workouts, List<EmotionLog> emotions) {
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 MyDiet 앱의 AI 건강 코치입니다. 사용자의 하루를 분석하고 맞춤형 조언을 제공해주세요.\n\n");
        
        prompt.append("=== 사용자 정보 ===\n");
        prompt.append("닉네임: ").append(nickname).append("\n");
        prompt.append("감정 모드: ").append(emotionMode).append("\n\n");
        
        prompt.append("=== 오늘의 기록 ===\n");
        prompt.append("식단 기록: ").append(meals.size()).append("회\n");
        prompt.append("운동 기록: ").append(workouts.size()).append("회\n");
        prompt.append("감정 기록: ").append(emotions.size()).append("회\n\n");
        
        prompt.append("위 데이터를 바탕으로 감정 모드 '").append(emotionMode).append("'에 맞는 스타일로 ");
        prompt.append("격려하고 조언하는 메시지를 한국어로 3-4줄 작성해주세요.\n");
        
        return prompt.toString();
    }
    
    private String generateSimpleAnswer(String question, Long userId) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 MyDiet 앱의 건강 전문가입니다. 다음 질문에 친근하고 도움이 되는 답변을 해주세요:\n\n");
        prompt.append("질문: ").append(question).append("\n\n");
        prompt.append("답변은 200자 이내로 간결하게 작성해주세요.");
        
        return prompt.toString();
    }
}