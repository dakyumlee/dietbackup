package com.mydiet.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Service
public class ClaudeAIService {

    @Value("${claude.api.key}")
    private String apiKey;

    private final HttpClient httpClient;
    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";

    public ClaudeAIService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    }

    public String generateAdvice(String prompt) {
        try {
            return callClaudeAPI(prompt, "일일 건강 조언을 생성해주세요.");
        } catch (Exception e) {
            log.error("Claude AI 조언 생성 실패", e);
            return getFallbackAdvice();
        }
    }

    public String generateAnswer(String prompt) {
        try {
            return callClaudeAPI(prompt, "사용자의 질문에 답변해주세요.");
        } catch (Exception e) {
            log.error("Claude AI 답변 생성 실패", e);
            return "죄송합니다. 현재 AI 서비스에 일시적인 문제가 있습니다. 잠시 후 다시 시도해주세요.";
        }
    }

    public String generateWeeklyReport(String prompt) {
        try {
            return callClaudeAPI(prompt, "주간 건강 관리 보고서를 작성해주세요.");
        } catch (Exception e) {
            log.error("Claude AI 주간 보고서 생성 실패", e);
            return "주간 보고서를 생성할 수 없습니다. 데이터를 수집하고 있으니 잠시 후 다시 확인해주세요.";
        }
    }

    private String callClaudeAPI(String prompt, String systemMessage) throws IOException, InterruptedException {
        String escapedSystemMessage = systemMessage.replace("\"", "\\\"");
        String escapedPrompt = prompt.replace("\"", "\\\"").replace("\n", "\\n");
        
        String requestBody = String.format(
            "{\n" +
            "    \"model\": \"claude-3-haiku-20240307\",\n" +
            "    \"max_tokens\": 1000,\n" +
            "    \"system\": \"%s\",\n" +
            "    \"messages\": [\n" +
            "        {\n" +
            "            \"role\": \"user\",\n" +
            "            \"content\": \"%s\"\n" +
            "        }\n" +
            "    ]\n" +
            "}",
            escapedSystemMessage,
            escapedPrompt
        );

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(CLAUDE_API_URL))
            .header("Content-Type", "application/json")
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .timeout(Duration.ofSeconds(60))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parseClaudeResponse(response.body());
        } else {
            log.error("Claude API 오류: {} - {}", response.statusCode(), response.body());
            throw new RuntimeException("Claude API 호출 실패: " + response.statusCode());
        }
    }

    private String parseClaudeResponse(String responseBody) {
        try {
            int contentStart = responseBody.indexOf("\"text\":\"") + 8;
            int contentEnd = responseBody.indexOf("\"", contentStart);
            
            if (contentStart > 7 && contentEnd > contentStart) {
                String content = responseBody.substring(contentStart, contentEnd);
                return content.replace("\\n", "\n").replace("\\\"", "\"");
            } else {
                log.warn("Claude 응답 파싱 실패: {}", responseBody);
                return getFallbackAdvice();
            }
        } catch (Exception e) {
            log.error("Claude 응답 파싱 오류", e);
            return getFallbackAdvice();
        }
    }

    private String getFallbackAdvice() {
        String[] fallbackMessages = {
            "오늘도 건강한 하루 보내세요! 꾸준한 기록이 건강한 습관을 만듭니다. 💪",
            "균형 잡힌 식단과 적절한 운동으로 목표를 향해 나아가세요! 🌟",
            "작은 변화가 큰 결과를 만듭니다. 오늘 하루도 화이팅! ✨",
            "건강한 생활습관은 하루아침에 만들어지지 않아요. 꾸준히 함께해요! 🎯",
            "오늘의 노력이 내일의 건강을 만듭니다. 차근차근 해나가요! 🌱"
        };
        
        int randomIndex = (int) (Math.random() * fallbackMessages.length);
        return fallbackMessages[randomIndex];
    }

    public boolean isAPIKeyValid() {
        return apiKey != null && !apiKey.trim().isEmpty() && apiKey.startsWith("sk-ant-");
    }

    public boolean isServiceAvailable() {
        if (!isAPIKeyValid()) {
            return false;
        }

        try {
            String testPrompt = "테스트";
            callClaudeAPI(testPrompt, "간단히 '안녕하세요'라고 답변해주세요.");
            return true;
        } catch (Exception e) {
            log.warn("Claude AI 서비스 상태 확인 실패", e);
            return false;
        }
    }
}