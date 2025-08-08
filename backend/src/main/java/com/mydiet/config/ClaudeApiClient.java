package com.mydiet.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClaudeApiClient {

    @Value("${claude.api.key}")
    private String apiKey;

    @Value("${claude.api.base-url}")
    private String baseUrl;

    @Value("${claude.api.model}")
    private String model;

    private final WebClient webClient;

    public ClaudeApiClient() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.anthropic.com/v1")
                .build();
    }

    public String askClaude(String prompt) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 1000);
            requestBody.put("messages", List.of(message));

            log.debug("Claude API 요청: {}", requestBody);

            Map<String, Object> response = webClient.post()
                    .uri("/messages")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            log.debug("Claude API 응답: {}", response);

            if (response != null && response.containsKey("content")) {
                List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
                if (!content.isEmpty()) {
                    return (String) content.get(0).get("text");
                }
            }

            return "응답을 받을 수 없습니다.";

        } catch (Exception e) {
            log.error("Claude API 호출 실패", e);
            return "AI 응답 생성 중 오류가 발생했습니다.";
        }
    }
}