// src/main/java/com/prodash/infrastructure/adapter/out/llm/dto/LlmApiRequest.java
package com.prodash.infrastructure.adapter.out.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class LlmApiRequest {

    private String model;
    private List<Message> messages;

    @Getter
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}