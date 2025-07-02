package com.prodash.infrastructure.adapter.out.llm.dto;

import java.util.List;

public class SafetyAttributes {
    private List<Double> scores;
    private boolean blocked;
    private List<String> categories;

    public List<Double> getScores() {
        return scores;
    }

    public void setScores(List<Double> scores) {
        this.scores = scores;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
}