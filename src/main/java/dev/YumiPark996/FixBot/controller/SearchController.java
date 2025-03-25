package dev.YumiPark996.FixBot.controller;

import dev.YumiPark996.FixBot.dto.GoogleDTO;
import dev.YumiPark996.FixBot.dto.VideoDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import dev.YumiPark996.FixBot.service.FixBotService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final FixBotService fixBotService;

    @Autowired
    public SearchController(FixBotService fixBotService) {
        this.fixBotService = fixBotService;
    }

    @GetMapping("/google")
    public ResponseEntity<List<GoogleDTO>> searchGoogle(@RequestParam String query) {
        try {
            return ResponseEntity.ok(fixBotService.getGoogleResults(query));
        } catch (Exception e) {
            e.printStackTrace(); // 예외 로그 찍기
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/youtube")
    public ResponseEntity<List<VideoDTO>> searchYoutube(@RequestParam String query) {
        try {
            return ResponseEntity.ok(fixBotService.getYouTubeResults(query));
        } catch (Exception e) {
            e.printStackTrace(); // 예외 로그 찍기
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
