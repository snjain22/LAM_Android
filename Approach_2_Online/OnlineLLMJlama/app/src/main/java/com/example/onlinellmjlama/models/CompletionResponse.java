package com.example.onlinellmjlama.models;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class CompletionResponse {

    @SerializedName("choices")
    private List<Choice> choices;

    public List<Choice> getChoices() {
        return choices != null ? choices : new ArrayList<>();
    }

    public String getContent() {
        if (choices != null && !choices.isEmpty()) {
            return choices.get(0).getMessage().getContent();
        }
        return null;
    }

    public static class Choice {
        @SerializedName("message")
        private Message message;

        public Message getMessage() {
            return message;
        }
    }

    public static class Message {
        @SerializedName("content")
        private String content;

        public String getContent() {
            return content;
        }
    }
}
