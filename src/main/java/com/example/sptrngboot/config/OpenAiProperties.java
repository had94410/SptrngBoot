package com.example.sptrngboot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.openai")
public class OpenAiProperties {
    private String apiKey;
    private String model = "groq/compound-mini";
    private String baseUrl = "https://api.openai.com/v1";
    private String systemPrompt = "あなたは親切で役立つアシスタントです。簡潔かつ丁寧に回答してください。";

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
