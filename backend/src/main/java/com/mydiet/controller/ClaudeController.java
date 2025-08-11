package com.mydiet.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/claude")
@RequiredArgsConstructor
public class ClaudeController {

    @GetMapping("/message")
    public ResponseEntity<String> getClaudeMessage(
            @RequestParam(required = false) Long userId,
            HttpSession session) {
        try {
            Long sessionUserId = (Long) session.getAttribute("userId");
            Long actualUserId = userId != null ? userId : sessionUserId;
            
            if (actualUserId == null) {
                return ResponseEntity.ok("안녕하세요! 로그인 후 개인 맞춤형 조언을 받아보세요! 😊");
            }

            String[] responses = {
                "오늘도 건강한 하루 보내세요! 꾸준한 운동과 균형잡힌 식단이 중요합니다. 💪",
                "목표를 향해 한 걸음씩 나아가고 있어요! 작은 성취도 큰 의미가 있답니다. 🌟",
                "스트레스 관리도 건강의 중요한 부분이에요. 충분한 휴식을 잊지 마세요! 😌",
                "물을 충분히 마시고 규칙적인 생활 패턴을 유지해보세요! 💧",
                "오늘 하루도 자신을 위해 노력하는 당신이 정말 대단해요! 🎯"
            };
            
            String randomResponse = responses[(int) (Math.random() * responses.length)];
            return ResponseEntity.ok(randomResponse);
            
        } catch (Exception e) {
            log.error("Claude 메시지 생성 실패", e);
            return ResponseEntity.ok("건강한 하루 보내세요! 💪");
        }
    }

    @GetMapping("/daily-message")
    public ResponseEntity<Map<String, Object>> getDailyMessage(HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            
            String message = userId != null ? 
                "오늘도 건강한 식단과 꾸준한 운동으로 목표를 향해 나아가세요! 💪" :
                "로그인 후 개인 맞춤형 건강 조언을 받아보세요! 😊";
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", message);
            response.put("timestamp", java.time.LocalDateTime.now());
            response.put("success", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("일일 메시지 생성 실패", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "건강한 하루 보내세요! 💪");
            errorResponse.put("success", false);
            return ResponseEntity.ok(errorResponse);
        }
    }
}
