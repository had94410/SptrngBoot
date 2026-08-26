package com.example.sptrngboot.controller;

import com.example.sptrngboot.model.ChatMessage;
import com.example.sptrngboot.repository.ChatMessageRepository;
import com.example.sptrngboot.service.OpenAiChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class ChatbotController {
    private final OpenAiChatService openAiChatService;
    private final ChatMessageRepository chatMessageRepository;

    public ChatbotController(OpenAiChatService openAiChatService, ChatMessageRepository chatMessageRepository) {
        this.openAiChatService = openAiChatService;
        this.chatMessageRepository = chatMessageRepository;
    }

    @GetMapping("/chatbot")
    public String page() {
        return "chatbot";
    }

    @GetMapping("/api/chatbot/history")
    @ResponseBody
    public List<Map<String, String>> history() {
        List<Map<String, String>> response = new ArrayList<>();
        for (ChatMessage message : chatMessageRepository.findAllByOrderByCreatedAtAsc()) {
            response.add(Map.of(
                    "sender", message.getSender(),
                    "text", message.getContent()
            ));
        }
        return response;
    }

    @PostMapping("/api/chatbot/message")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> message(@RequestBody Map<String, String> payload) {
        String userMessage = payload == null ? null : payload.get("message");

        if (userMessage == null || userMessage.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "メッセージを入力してください。"));
        }

        try {
            ChatMessage userChatMessage = chatMessageRepository.save(new ChatMessage("user", userMessage));
            String reply = openAiChatService.chat(userMessage);
            ChatMessage assistantChatMessage = chatMessageRepository.save(new ChatMessage("bot", reply));

            return ResponseEntity.ok(Map.of(
                    "reply", reply,
                    "history", List.of(
                            Map.of("sender", userChatMessage.getSender(), "text", userChatMessage.getContent()),
                            Map.of("sender", assistantChatMessage.getSender(), "text", assistantChatMessage.getContent())
                    )
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", e.getMessage()));
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "LLM API への接続に失敗しました。"));
        }
    }
}
