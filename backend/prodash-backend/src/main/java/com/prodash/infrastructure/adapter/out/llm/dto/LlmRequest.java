package com.prodash.infrastructure.adapter.out.llm.dto;

import java.util.List;

public class LlmRequest {
    private String model;
    private List<Message> messages;

    public LlmRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }

    // Nested Message class
    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}