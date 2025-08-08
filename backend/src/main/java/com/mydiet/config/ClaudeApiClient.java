package com.mydiet.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClaudeApiClient {

    @Value("${claude.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String askClaude(String prompt) {
        log.info("=== Claude API 호출 시작 ===");
        log.info("API 키 길이: {}", apiKey != null ? apiKey.length() : "null");
        log.info("프롬프트: {}", prompt);
        
        try {
            if (apiKey == null || apiKey.trim().isEmpty() || apiKey.startsWith("${")) {
                log.error("Claude API 키가 설정되지 않았습니다: {}", apiKey);
                return "Claude API 키가 설정되지 않았습니다. 관리자에게 문의하세요.";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", "2023-06-01");

            Map<String, Object> requestBody = Map.of(
                "model", "claude-3-sonnet-20240229",
                "max_tokens", 200,
                "messages", List.of(Map.of("role", "user", "content", prompt))
            );

            log.info("요청 본문: {}", requestBody);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("Claude API 호출 중...");
            ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.anthropic.com/v1/messages",
                entity,
                Map.class
            );

            log.info("응답 상태: {}", response.getStatusCode());
            log.info("응답 본문: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
                if (content != null && !content.isEmpty()) {
                    String result = (String) content.get(0).get("text");
                    log.info("Claude 응답: {}", result);
                    return result;
                }
            }
            
            log.warn("Claude API 응답이 비어있습니다");
            return "Claude 응답을 받을 수 없습니다.";
            
        } catch (Exception e) {
            log.error("Claude API 호출 실패", e);
            return "현재 Claude 서비스를 이용할 수 없습니다: " + e.getMessage();
        }
    }
}