package com.prodash.infrastructure.adapter.out.llm.dto;

import java.util.List;

public class LlmResponse {
    private List<Choice> choices;

    // Nested Choice class
    public static class Choice {
        private Message message;
        
        public Message getMessage() {
            return message;
        }
    }
    
    // Nested Message class
    public static class Message {
        private String content;

        public String getContent() {
            return content;
        }
    }
}