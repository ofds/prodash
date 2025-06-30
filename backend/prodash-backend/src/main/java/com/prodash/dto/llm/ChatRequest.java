// Create this file in a new package: com.prodash.dto.llm
package com.prodash.dto.llm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Represents the request body for the OpenRouter API
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private String model;
    private List<Message> messages;
}

