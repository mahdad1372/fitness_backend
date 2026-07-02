package com.example.fitness.dto;

public class CardiovascularResponse {
    private Double cardio;
    private String status;

    public CardiovascularResponse(Double cardio, String status) {
        this.cardio = cardio;
        this.status = status;
    }

    public Double getCardio() {
        return cardio;
    }

    public void setCardio(Double cardio) {
        this.cardio = cardio;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}