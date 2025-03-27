package dev.YumiPark996.FixBot.service;

import dev.YumiPark996.FixBot.config.FixBotPromptLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;

@Service
@EnableRetry
public class OpenAIService {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final RestTemplate restTemplate = new RestTemplate();
    private final JSONArray conversationHistory = new JSONArray();

    @Value("${openai.api.key}")  // ✅ application.properties에서 API 키 불러오기
    private String apiKey;

    public OpenAIService() {
        // ✅ FixBot 프롬프트 로드하여 system 메시지 설정
        String systemPrompt = FixBotPromptLoader.getFormattedPrompt("fixbot_prompt.txt", null, null, null);
        JSONObject systemMessage = new JSONObject()
                .put("role", "system")
                .put("content", systemPrompt);
        conversationHistory.put(systemMessage);
    }

    @Cacheable(value = "chatCache", key = "#userInput")
    public String getChatbotResponse(String userInput) {
        try {
            // ✅ 요청 사이 간격 두기 (예: 200ms)
            Thread.sleep(200);
            // ✅ 사용자 입력 저장
            JSONObject userMessage = new JSONObject()
                    .put("role", "user")
                    .put("content", userInput);
            conversationHistory.put(userMessage);

            // ✅ OpenAI API 요청 본문 생성
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", conversationHistory);

            // ✅ HTTP 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);  // ✅ application.properties에서 로드한 API 키 사용

            ResponseEntity<String> response;

            try {
                response = callOpenAI(requestBody, headers);
            } catch (HttpClientErrorException.TooManyRequests e) {
                // ✅ 429 발생 시 1초 후 재시도
                Thread.sleep(1000);
                response = callOpenAI(requestBody, headers);
            }

            // ✅ AI 응답 저장 및 반환
            JSONObject jsonResponse = new JSONObject(response.getBody());
            String botResponse = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

            JSONObject botMessage = new JSONObject().put("role", "assistant").put("content", botResponse);
            conversationHistory.put(botMessage);

            return botResponse;

        } catch (Exception e) {
            return "⚠️ OpenAI API 호출 중 오류 발생: " + e.getMessage();
        }
    }

    @Retryable(
            value = { HttpClientErrorException.TooManyRequests.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000) // 1초 대기 후 재시도
    )
    public ResponseEntity<String> callOpenAI(JSONObject requestBody, HttpHeaders headers) {
        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        return restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);
    }

}
