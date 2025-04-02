package com.example.onlinellmjlama.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class CompletionRequest {
    @SerializedName("model")
    private String model = "meta-llama/llama-3.3-70b-instruct:free"; // Default model

    @SerializedName("messages")
    private List<Message> messages;

    @SerializedName("temperature")
    private float temperature = 0.3f; // Default temperature for deterministic responses

    @SerializedName("max_tokens")
    private int maxTokens = 500; // Default max tokens

    @SerializedName("response_format")
    private Map<String, String> responseFormat; // Optional response format

    // Getters and Setters for each field

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Map<String, String> getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(Map<String, String> responseFormat) {
        this.responseFormat = responseFormat;
    }
}
