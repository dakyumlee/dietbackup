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
        
        try {
            User user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                return ResponseEntity.ok("안녕하세요! 프로필을 설정하고 건강 관리를 시작해보세요! 🍎");
            }
            
            String prompt = String.format("사용자 '%s'님에게 %s 톤으로 건강 관리 격려 메시지를 한 문장으로 해주세요.", 
                    user.getNickname(), user.getEmotionMode());
            
            log.info("Claude API 호출 시작: {}", prompt);
            String response = claudeApiClient.askClaude(prompt);
            log.info("Claude API 응답: {}", response);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Claude 메시지 생성 실패: userId={}", userId, e);
            return ResponseEntity.ok("오늘도 건강한 하루 보내세요! 💪 (Claude AI 일시 오류)");
        }
    }

    @PostMapping("/ai/ask")
    public ResponseEntity<Map<String, Object>> askClaude(@RequestBody Map<String, Object> request) {
        log.info("=== AI 질문 요청 ===");
        
        try {
            String question = (String) request.get("question");
            Long userId = request.get("userId") != null ? 
                Long.valueOf(request.get("userId").toString()) : 1L;
            
            if (question == null || question.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "question", "",
                    "answer", "질문을 입력해주세요!"
                ));
            }
            
            User user = userRepository.findById(userId).orElse(null);
            
            StringBuilder context = new StringBuilder();
            context.append("사용자 질문: ").append(question).append("\n\n");
            
            if (user != null) {
                context.append("사용자 정보: ").append(user.getNickname()).append("님, ");
                context.append("감정 모드: ").append(user.getEmotionMode()).append("\n\n");
                context.append(user.getEmotionMode()).append(" 톤으로 ");
            }
            
            context.append("건강 관리에 도움이 되는 조언을 간단하게 해주세요.");
            
            log.info("Claude API 질문 호출: {}", question);
            String answer = claudeApiClient.askClaude(context.toString());
            log.info("Claude API 질문 응답: {}", answer);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "question", question,
                "answer", answer
            ));
            
        } catch (Exception e) {
            log.error("AI 질문 처리 실패", e);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "question", request.get("question"),
                "answer", "죄송합니다. 현재 AI 서비스에 일시적인 문제가 있습니다. 잠시 후 다시 시도해주세요."
            ));
        }
    }
}