package com.example.techblogsuggestion.api;

import com.example.techblogsuggestion.service.TelegramDigestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/digest")
@RequiredArgsConstructor
public class DigestController {

    private final TelegramDigestService telegramDigestService;
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> triggerSendDigest() {
        telegramDigestService.sendDailyDigest();
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Daily digest triggered. Check your Telegram chat."
        ));
    }
}