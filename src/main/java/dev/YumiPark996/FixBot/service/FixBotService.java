package dev.YumiPark996.FixBot.service;

import dev.YumiPark996.FixBot.config.FixBotPromptLoader;
import dev.YumiPark996.FixBot.dto.*;
import dev.YumiPark996.FixBot.repository.GoogleRepository;
import dev.YumiPark996.FixBot.repository.VideoRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FixBotService {
    private final GoogleRepository googleRepository;
    private final VideoRepository videoRepository;
    private final OpenAIService openAIService;
    private static final Logger logger = LoggerFactory.getLogger(FixBotService.class);
    private final Map<String, JSONArray> sessionHistories = new ConcurrentHashMap<>();


    @Autowired
    public FixBotService(GoogleRepository googleRepository, VideoRepository videoRepository, OpenAIService openAIService) {
        this.googleRepository = googleRepository;
        this.videoRepository = videoRepository;
        this.openAIService = openAIService;
    }

    // ✅ 챗봇 응답을 저장하면서 호출
    public String getChatbotResponse(String sessionId, String userInput, String brand, String category, String subcategory, String question, String visionSummary) {
        try {
            // ✅ system prompt는 매번 새로 넣어줘야 함
            String systemPrompt = FixBotPromptLoader.getFormattedPrompt("fixbot_prompt.txt", null, null, null);
            JSONArray conversationHistory = sessionHistories.computeIfAbsent(sessionId, k -> {
                JSONArray arr = new JSONArray();
                arr.put(new JSONObject().put("role", "system").put("content", systemPrompt));
                return arr;
            });

            // ✅ structuredInput: 사용자의 질문 + 컨텍스트 정리
            String structuredInput = String.format(
                    "브랜드: %s\n카테고리: %s\n세부 카테고리: %s\n질문: %s\n사용자 채팅: %s",
                    brand, category, subcategory, question, userInput
            );

            // ✅ 1. 이미지 분석 결과 먼저
            if (visionSummary != null && !visionSummary.isBlank()) {
                conversationHistory.put(new JSONObject()
                        .put("role", "user")
                        .put("content", "[이미지 분석 결과]\n" + visionSummary));
            }

            // ✅ 2. 사용자 입력 추가
            conversationHistory.put(new JSONObject()
                    .put("role", "user")
                    .put("content", structuredInput));

            // ✅ 3. GPT 호출 (이전 대화 포함해서)
            String response = openAIService.getChatbotResponse(conversationHistory);

            // ✅ 4. GPT 응답 저장
            conversationHistory.put(new JSONObject()
                    .put("role", "assistant")
                    .put("content", response));

            return response;
        } catch (Exception e) {
            logger.error("FixBot API 호출 중 오류 발생", e);
            return "⚠️ AI 응답을 가져오는 중 오류가 발생했습니다. 다시 시도해주세요.";
        }
    }

    // Google 검색 API 호출
    public List<GoogleDTO> getGoogleResults(String query) throws Exception {
        GoogleParam param = new GoogleParam(query);
        return googleRepository.getResults(param);
    }

    // YouTube 검색 API 호출
    public List<VideoDTO> getYouTubeResults(String query) throws Exception {
        VideoParam param = new VideoParam(query);
        return videoRepository.getVideos(param);
    }
}
