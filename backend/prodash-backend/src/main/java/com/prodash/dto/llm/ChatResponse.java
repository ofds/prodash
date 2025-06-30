package com.prodash.dto.llm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
// Represents the response from the OpenRouter API
@Data
@NoArgsConstructor
public class ChatResponse {
    private List<Choice> choices;
}
