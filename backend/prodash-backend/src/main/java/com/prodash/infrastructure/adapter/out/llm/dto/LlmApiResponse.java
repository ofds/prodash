// src/main/java/com/prodash/infrastructure/adapter/out/llm/dto/LlmApiResponse.java
package com.prodash.infrastructure.adapter.out.llm.dto;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class LlmApiResponse {
    private List<Choice> choices;

    @Getter
    @ToString
    public static class Choice {
        private Message message;
    }

    @Getter
    @ToString
    public static class Message {
        private String content;
    }
}
