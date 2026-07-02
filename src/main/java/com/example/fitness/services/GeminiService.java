package com.example.fitness.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.Map;
import java.util.List;

@Service
public class GeminiService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    private final WebClient webClient;

    public GeminiService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    public String getRecommendation(String prompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    )
            );

            Map response = webClient.post()
                    .uri(apiUrl)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List choices = (List) response.get("choices");
            Map firstChoice = (Map) choices.get(0);
            Map message = (Map) firstChoice.get("message");
            return (String) message.get("content");

        } catch (WebClientResponseException e) {
            System.out.println("AI error status: " + e.getStatusCode().value());
            System.out.println("AI error body: " + e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 429) {
                return "AI recommendations are temporarily unavailable due to rate limiting. Please try again in a minute.";
            }
            throw new RuntimeException("AI API error: " + e.getMessage());
        }
    }
}