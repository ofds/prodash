package com.prodash.infrastructure.adapter.out.llm.dto;

import java.util.List;

public class CitationMetadata {
    private List<Object> citations;

    public List<Object> getCitations() {
        return citations;
    }

    public void setCitations(List<Object> citations) {
        this.citations = citations;
    }
}