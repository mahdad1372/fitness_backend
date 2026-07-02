package com.example.fitness.controllers;

import com.example.fitness.dto.AIChatRequest;
import com.example.fitness.dto.AIChatResponse;
import com.example.fitness.services.AIChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-chat")
@RequiredArgsConstructor
public class AIChatController {

    private final AIChatService aiChatService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AIChatResponse> chat(@RequestBody AIChatRequest request) {
        String reply = aiChatService.chat(request.getUserId(), request.getMessage());
        return ResponseEntity.ok(new AIChatResponse(reply));
    }

    @DeleteMapping("/history/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> clearHistory(@PathVariable Integer userId) {
        aiChatService.clearHistory(userId);
        return ResponseEntity.ok().build();
    }
}