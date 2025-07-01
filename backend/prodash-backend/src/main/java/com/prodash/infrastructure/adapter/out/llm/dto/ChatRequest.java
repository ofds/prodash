package com.prodash.infrastructure.adapter.out.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class ChatRequest {
    private String model;
    private List<Message> messages;
}