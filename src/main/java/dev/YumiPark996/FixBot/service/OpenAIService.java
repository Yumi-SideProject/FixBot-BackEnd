package dev.YumiPark996.FixBot.service;

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

    @Value("${openai.api.key}")  // ✅ application.properties에서 API 키 불러오기
    private String apiKey;

    public String getChatbotResponse(JSONArray conversationHistory) {
        try {
            // ✅ 요청 사이 간격 두기 (예: 200ms)
            Thread.sleep(200);

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

            conversationHistory.put(new JSONObject().put("role", "assistant").put("content", botResponse));

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
