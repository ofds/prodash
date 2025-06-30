package com.prodash.dto.llm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Represents a single message in the chat request
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String role;
    private String content;
}