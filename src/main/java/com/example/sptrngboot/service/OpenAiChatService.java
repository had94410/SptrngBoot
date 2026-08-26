package com.example.sptrngboot.service;

import com.example.sptrngboot.config.OpenAiProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiChatService {
    private final OpenAiProperties properties;
    private final RestTemplate restTemplate;

    public OpenAiChatService(OpenAiProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    public String chat(String message) {
        if (!properties.isConfigured()) {
            return buildDemoReply(message);
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", properties.getModel());
        requestBody.put("temperature", 0.7);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", properties.getSystemPrompt()));
        messages.add(Map.of("role", "user", "content", message));
        requestBody.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(properties.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        String url = properties.getBaseUrl() + "/chat/completions";

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody == null || responseBody.get("choices") == null) {
                throw new IllegalStateException("LLM API からの応答が空でした。");
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new IllegalStateException("LLM API からの応答が空でした。");
            }

            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> messageDetails = (Map<String, Object>) firstChoice.get("message");
            if (messageDetails == null || messageDetails.get("content") == null) {
                throw new IllegalStateException("LLM API からの応答内容を取得できませんでした。");
            }

            return String.valueOf(messageDetails.get("content")).trim();
        } catch (HttpStatusCodeException e) {
            String responseText = e.getResponseBodyAsString();
            String detail = responseText == null || responseText.isBlank() ? e.getMessage() : responseText;
            throw new IllegalStateException("LLM API エラー (status=" + e.getStatusCode() + "): " + detail, e);
        } catch (ResourceAccessException e) {
            throw new IllegalStateException("LLM API への接続がタイムアウトまたは拒否されました。ネットワークと API キーを確認してください。", e);
        }
    }

    private String buildDemoReply(String message) {
        return "LLM APIキーが未設定のため、デモ応答です。\n"
                + "本番のAI応答を使うには OPENAI_API_KEY を設定してください。\n\n"
                + "あなたのメッセージ: " + message;
    }
}
