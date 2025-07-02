// src/main/java/com/prodash/infrastructure/adapter/out/llm/dto/LlmApiResponse.java
package com.prodash.infrastructure.adapter.out.llm.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class LlmApiResponse {

    private List<Choice> choices;

    @Getter
    public static class Choice {
        private Message message;
    }

    @Getter
    public static class Message {
        private String content;
    }
}