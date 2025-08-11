package com.mydiet.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class ClaudeApiService {

    @Value("${claude.api.key}")
    private String apiKey;

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    
    private final RestTemplate restTemplate = new RestTemplate();

    public String sendMessage(String userMessage, String emotionMode, Map<String, Object> userContext) {
        try {
            log.info("Claude API 요청 시작: mode={}, message={}", emotionMode, userMessage);

            String systemPrompt = buildSystemPrompt(emotionMode, userContext);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "claude-3-sonnet-20240229");
            requestBody.put("max_tokens", 1000);
            requestBody.put("system", systemPrompt);
            
            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", userMessage);
            messages.add(message);
            
            requestBody.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", "2023-06-01");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                CLAUDE_API_URL, 
                HttpMethod.POST, 
                entity, 
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> content = (List<Map<String, Object>>) responseBody.get("content");
                
                if (content != null && !content.isEmpty()) {
                    String claudeResponse = (String) content.get(0).get("text");
                    log.info("Claude API 응답 성공");
                    return claudeResponse;
                }
            }

            log.warn("Claude API 응답 파싱 실패");
            return getFallbackResponse(emotionMode);

        } catch (Exception e) {
            log.error("Claude API 호출 실패", e);
            return getFallbackResponse(emotionMode);
        }
    }

    private String buildSystemPrompt(String emotionMode, Map<String, Object> userContext) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 MyDiet 앱의 AI 건강 코치입니다. ");
        
        switch (emotionMode) {
            case "다정함":
                prompt.append("따뜻하고 격려적인 말투로 사용자를 응원해주세요. ");
                break;
            case "츤데레":
                prompt.append("츤데레 캐릭터처럼 겉으로는 차가우면서도 속으로는 걱정하는 말투로 대화하세요. ");
                break;
            case "무자비":
                prompt.append("엄격하고 직설적인 말투로 사용자의 건강 관리를 체크해주세요. ");
                break;
            case "격려":
                prompt.append("긍정적이고 활기찬 말투로 사용자를 동기부여해주세요. ");
                break;
            default:
                prompt.append("친근하고 도움이 되는 말투로 대화하세요. ");
        }
        
        prompt.append("사용자의 식단, 운동, 감정 상태를 분석하고 개인 맞춤형 조언을 제공하세요. ");
        prompt.append("답변은 한국어로 하고, 200자 내외로 간결하게 작성해주세요.");
        
        if (userContext != null && !userContext.isEmpty()) {
            prompt.append("\n\n사용자 정보: ");
            if (userContext.get("weightGoal") != null) {
                prompt.append("목표 체중: ").append(userContext.get("weightGoal")).append("kg ");
            }
            if (userContext.get("todayMeals") != null) {
                prompt.append("오늘 식단: ").append(userContext.get("todayMeals")).append("개 ");
            }
            if (userContext.get("todayWorkouts") != null) {
                prompt.append("오늘 운동: ").append(userContext.get("todayWorkouts")).append("개 ");
            }
        }
        
        return prompt.toString();
    }

    private String getFallbackResponse(String emotionMode) {
        Map<String, String[]> fallbackResponses = new HashMap<>();
        
        fallbackResponses.put("다정함", new String[]{
            "건강한 하루 보내고 계시는군요! 오늘도 화이팅이에요! 🌟",
            "작은 변화가 큰 결과를 만들어요. 천천히 꾸준히 해나가세요! 💪",
            "당신의 노력이 정말 자랑스러워요. 계속 이렇게 해나가시면 좋겠어요! ✨"
        });
        
        fallbackResponses.put("츄데레", new String[]{
            "흥... 그 정도로 만족하면 안 돼! 더 열심히 해야 한다구! 💢",
            "별로 나쁘지 않네... 하지만 방심하면 안 돼! 계속 신경 써! 😤",
            "뭐... 그래도 노력은 하는 것 같으니까... 인정해줄게! 🙄"
        });
        
        fallbackResponses.put("무자비", new String[]{
            "아직 부족해. 목표 달성을 위해서는 더 엄격하게 관리해야 한다! 🔥",
            "변명은 그만하고 행동으로 보여줘. 진짜 의지가 있다면 말이야! ⚡",
            "현실을 직시해. 지금 이대로는 목표 달성이 어렵다! 각성해! 💀"
        });
        
        fallbackResponses.put("격려", new String[]{
            "와! 정말 잘하고 있어요! 이 기세로 계속 가봐요! 🎉",
            "대단해요! 당신은 할 수 있어요! 목표까지 파이팅! 🚀",
            "훌륭한 진전이에요! 이렇게 꾸준히 하면 반드시 성공할 거예요! ⭐"
        });
        
        String[] responses = fallbackResponses.getOrDefault(emotionMode, fallbackResponses.get("다정함"));
        return responses[new Random().nextInt(responses.length)];
    }
}