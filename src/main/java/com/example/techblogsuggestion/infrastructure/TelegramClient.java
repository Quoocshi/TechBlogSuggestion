package com.example.techblogsuggestion.infrastructure;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TelegramClient {

    private static final Logger log = LoggerFactory.getLogger(TelegramClient.class);
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient
            ;
    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.chat-id}")
    private String chatId;

    public void sendMessage(String htmlText){
        if (botToken == null || botToken.isBlank() || chatId == null || chatId.isBlank()) {
            return;
        }

        try{
            String url =  "https://api.telegram.org/bot" + botToken + "/sendMessage";
            Map<String, Object> payload = Map.of(
                    "chat_id", chatId,
                    "text", htmlText,
                    "parse_mode", "HTML",
                    "link_preview_options", Map.of("is_disabled", true)
            );
            String jsonBody = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200){
                log.error("Telegram API error [{}]: {}", response.statusCode(), response.body());
            }
        } catch (Exception e){
            log.error("Failed to send telegram message", e.getMessage());
        }
    }
}
