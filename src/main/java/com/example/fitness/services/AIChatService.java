package com.example.fitness.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AIChatService {

    private final GeminiService geminiService;

    // Stores conversation history per user so the AI remembers context
    private final Map<Integer, List<String>> conversationHistory = new ConcurrentHashMap<>();

    public String chat(Integer userId, String userMessage) {

        // Get or create conversation history for this user
        conversationHistory.putIfAbsent(userId, new ArrayList<>());
        List<String> history = conversationHistory.get(userId);

        // Add user message to history
        history.add("User: " + userMessage);

        // Build the full prompt with conversation history
        String prompt = buildPrompt(history);

        // Get AI response
        String reply = geminiService.getRecommendation(prompt);

        // Add AI reply to history
        history.add("AI Coach: " + reply);

        // Keep only last 10 messages to avoid token limits
        if (history.size() > 10) {
            history.subList(0, history.size() - 10).clear();
        }

        return reply;
    }

    public void clearHistory(Integer userId) {
        conversationHistory.remove(userId);
    }

    private String buildPrompt(List<String> history) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a professional AI fitness coach. ");
        prompt.append("You are currently chatting with a user who is waiting for their private coach to become available. ");
        prompt.append("Be friendly, motivating, and give helpful fitness advice. ");
        prompt.append("Keep your responses concise and conversational (2-4 sentences max). ");
        prompt.append("Here is the conversation so far:\n\n");

        for (String message : history) {
            prompt.append(message).append("\n");
        }

        prompt.append("\nAI Coach:");
        return prompt.toString();
    }
}