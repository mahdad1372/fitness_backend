package com.example.fitness.dto;

public class AIChatResponse {
    private String reply;

    public AIChatResponse(String reply) { this.reply = reply; }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
}