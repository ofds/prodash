// in ChatResponse.java
package com.prodash.infrastructure.adapter.out.llm.dto;
import java.util.List;
public class ChatResponse {
    private List<Choice> choices;
    public List<Choice> getChoices() { return choices; }
    public static class Choice {
        private Message message;
        public Message getMessage() { return message; }
    }
}