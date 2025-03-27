package dev.YumiPark996.FixBot.controller;

import dev.YumiPark996.FixBot.service.FixBotService;
import dev.YumiPark996.FixBot.service.VisionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/chat")  // ✅ `/chat`이 기본 경로
public class FixBotController {
    private final FixBotService fixBotService;
    private final VisionService visionService;
    private static final Logger logger = LoggerFactory.getLogger(FixBotController.class);

    public FixBotController(FixBotService fixBotService, VisionService visionService) {
        this.fixBotService = fixBotService;
        this.visionService = visionService;
    }

    /**
     * 🔹 `/chat` 엔드포인트 (UI 제공)
     * ✅ 요청 방식: GET
     */
    @GetMapping
    public ResponseEntity<?> getChatPage(@RequestParam String brand,
                                         @RequestParam String category,
                                         @RequestParam String subcategory,
                                         @RequestParam String question) {
        // 기본 데이터 반환 (JSON 형식)
        return ResponseEntity.ok(Map.of(
                "brand", brand,
                "category", category,
                "subcategory", subcategory,
                "question", question,
                "message", "채팅을 시작하세요."
        ));
    }

    /**
     * 🔹 AI 응답을 가져오는 엔드포인트
     * ✅ 요청 방식: POST
     */
    @PostMapping("/ai-response")
    public ResponseEntity<?> aiResponse(@RequestBody Map<String, Object> requestBody) {
        try {
            String sessionId = (String) requestBody.get("sessionId");
            String userInput = (String) requestBody.get("message");
            String brand = (String) requestBody.get("brand");
            String category = (String) requestBody.get("category");
            String subcategory = (String) requestBody.get("subcategory");
            String question = (String) requestBody.get("question");
            String imageUrl = (String) requestBody.get("imageUrl"); // nullable

            String visionSummary = null;

            if (imageUrl != null && !imageUrl.isBlank()) {
                visionSummary = visionService.analyzeImage(category, userInput, imageUrl); // 🔍 Vision 모듈 호출
            }

            String response = fixBotService.getChatbotResponse(sessionId, userInput, brand, category, subcategory, question, visionSummary);
            return ResponseEntity.ok(Map.of("answer", response));
        } catch (Exception e) {
            logger.error("🔴 AI 응답 중 오류 발생", e);
            return ResponseEntity.status(500).body(Map.of("error", "⚠️ AI 응답을 가져오는 중 오류가 발생했습니다. 다시 시도해주세요."));
        }
    }
}