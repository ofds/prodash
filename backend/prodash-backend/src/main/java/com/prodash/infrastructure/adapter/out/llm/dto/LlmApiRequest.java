package com.prodash.infrastructure.adapter.out.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@ToString
public class LlmApiRequest {

    private String model;
    private List<Message> messages;

    @Getter
    @AllArgsConstructor
    @ToString
    public static class Message {
        private String role;
        private String content;
    }
}
