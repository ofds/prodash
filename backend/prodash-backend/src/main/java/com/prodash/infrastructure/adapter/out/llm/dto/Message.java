package com.prodash.infrastructure.adapter.out.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Message {
    private String role;
    private String content;
}