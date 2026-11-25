package com.example.techblogsuggestion.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GeminiClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String baseUrl;

    @Value("${gemini.api.chat-model:gemini-3.5-flash-lite}")
    private String chatModel;

    @Value("${gemini.api.embedding-model:gemini-embedding-001}")
    private String embeddingModel;

    public float[] embedText(String text) throws Exception {
        String endpoint = baseUrl + "/models/" + embeddingModel + ":embedContent?key=" + apiKey;

        Map<String, Object> payload = Map.of(
                "model", "models/" + embeddingModel,
                "content", Map.of("parts", List.of(Map.of("text", text))),
                "outputDimensionality", 768
        );

        String responseBody = sendPost(endpoint, payload);
        JsonNode valuesNode = objectMapper.readTree(responseBody)
                .path("embedding")
                .path("values");

        if (!valuesNode.isArray() || valuesNode.isEmpty()) {
            throw new RuntimeException("Invalid embedding response from Gemini: " + responseBody);
        }

        float[] vector = new float[valuesNode.size()];
        for (int i = 0; i < valuesNode.size(); i++) {
            vector[i] = (float) valuesNode.get(i).asDouble();
        }
        return vector;
    }

    public String generateJson(String systemInstruction, String userContent) throws Exception {
        String endpoint = baseUrl + "/models/" + chatModel + ":generateContent?key=" + apiKey;

        Map<String, Object> payload = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", systemInstruction))),
                "contents", List.of(Map.of("parts", List.of(Map.of("text", userContent)))),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "temperature", 0.2
                )
        );

        String responseBody = sendPost(endpoint, payload);
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            throw new RuntimeException("No candidates returned from Gemini: " + responseBody);
        }

        String rawContent = candidates.get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();

        return cleanJsonOutput(rawContent);
    }

    private String cleanJsonOutput(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return "";
        }
        String text = rawText.trim();

        if (text.contains("```json")) {
            int start = text.indexOf("```json") + 7;
            int end = text.lastIndexOf("```");
            if (end > start) {
                return text.substring(start, end).trim();
            }
        } else if (text.contains("```")) {
            int start = text.indexOf("```") + 3;
            int end = text.lastIndexOf("```");
            if (end > start) {
                return text.substring(start, end).trim();
            }
        }

        int jsonStartObj = text.indexOf('{');
        int jsonStartArr = text.indexOf('[');
        int start = -1;
        if (jsonStartObj != -1 && jsonStartArr != -1) {
            start = Math.min(jsonStartObj, jsonStartArr);
        } else if (jsonStartObj != -1) {
            start = jsonStartObj;
        } else {
            start = jsonStartArr;
        }

        int jsonEndObj = text.lastIndexOf('}');
        int jsonEndArr = text.lastIndexOf(']');
        int end = Math.max(jsonEndObj, jsonEndArr);

        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1).trim();
        }

        return text;
    }

    private String sendPost(String url, Object payload) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(payload);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        int maxRetries = 3;
        long backoffMillis = 3000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();

            if (status == 200) {
                return response.body();
            }
            boolean isTransientError = (status == 429 || status >= 500);
            if (isTransientError && attempt < maxRetries) {
                Thread.sleep(backoffMillis);
                backoffMillis *= 2;
                continue;
            }
            throw new RuntimeException("Gemini API error [" + status + "]: " + response.body());
        }
        throw new RuntimeException("Gemini API failed after " + maxRetries + " retries");
    }
}

