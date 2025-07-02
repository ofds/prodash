package com.prodash.infrastructure.adapter.out.llm.dto;

import java.util.List;

public class PredictionResponse {
    private List<Prediction> predictions;

    public List<Prediction> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<Prediction> predictions) {
        this.predictions = predictions;
    }
}