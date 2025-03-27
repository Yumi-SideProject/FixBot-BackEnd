package dev.YumiPark996.FixBot.service;

import dev.YumiPark996.FixBot.config.FixBotPromptLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VisionService {

    @Value("#{'${ai.api.gemini.endpoint}'.split(',')}")
    private List<String> geminiUrls;

    private final RestTemplate restTemplate;

    public VisionService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public String analyzeImage(String category, String userInput, String imageUrl) {
        try {
            String prompt = FixBotPromptLoader.getFormattedPrompt(
                    "gemini_prompt.txt",
                    category,
                    userInput,
                    imageUrl
            );
            Map<String, Object> geminiPayload = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "role", "user",
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    ),
                    "generationConfig", Map.of(
                            "temperature", 1,
                            "topK", 40,
                            "topP", 0.95,
                            "maxOutputTokens", 8192,
                            "responseMimeType", "text/plain"
                    )
            );

            for (String urlWithKey : geminiUrls) {
                System.out.println("🚀 Gemini 호출 시도: " + urlWithKey);
                Map<String, String> result = callWithKey(urlWithKey, geminiPayload, "gemini");

                String content = result.getOrDefault("result", "");
                if (!content.isBlank()) {
                    System.out.println("✅ Gemini 응답 성공 (키 성공)");
                    return content;
                } else {
                    System.out.println("⚠️ Gemini 응답 실패, 다음 키로 시도");
                }
            }

            // 세 개 다 실패했을 경우
            return "⚠️ Gemini Vision API 호출이 모두 실패했습니다.";

        } catch (Exception e) {
            System.err.println("🔥 analyzeImage 내부 오류 발생:");
            e.printStackTrace();
            return "⚠️ 이미지 분석 중 오류 발생";
        }
    }

    private Map<String, String> callWithKey(String urlWithKey, Map<String, Object> body, String type) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        System.out.println("✅ [DEBUG] 요청 URL = " + urlWithKey);
        System.out.println("✅ [DEBUG] Payload = " + body);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(urlWithKey, HttpMethod.POST, entity, Map.class);
            System.out.println("📡 [" + type + "] 응답 수신 완료");
            System.out.println("📦 Raw Body: " + response.getBody());

            String content = extractContent(response.getBody(), type);
            return Map.of("result", content);
        } catch (Exception e) {
            System.err.println("❌ [" + type + "] Vision API 호출 실패");
            System.err.println("  ↪ URL: " + urlWithKey);
            System.err.println("  ↪ Payload: " + body);
            System.err.println("  ↪ 에러: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();

            return Map.of("result", "");
        }
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> body, String type) {
        try {
            if (body == null) {
                System.err.println("⚠️ extractContent: body가 null입니다 (" + type + ")");
                return "";
            }
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                if (content != null) {
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        String text = (String) parts.get(0).get("text");
                        System.out.println("✅ Gemini 응답 파싱 완료");
                        return text;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("🔴 extractContent 오류 (" + type + "): " + e.getMessage());
        }
        return "";
    }

    private String computeConsensus(List<Map<String, String>> results) {
        return results.stream()
                .map(r -> r.getOrDefault("result", ""))
                .filter(r -> !r.isBlank())
                .collect(Collectors.joining("\n\n---\n\n"));
    }
}