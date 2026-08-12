package com.example.fitness.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps a short-lived Google Fit access token fresh using a long-lived
 * refresh token, so we never send an expired access token to the API.
 *
 * NOTE: this still uses ONE refresh token per data type (blood pressure /
 * heart rate), shared by the whole app - it fixes the "token expires"
 * half of the original problem, but NOT the "same token for every user"
 * half. For true per-user tokens, see GoogleTokenService instead, which
 * stores a refresh token per row in the `users` table.
 */
@Service
public class GoogleFitAuthService {

    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    private static final long EXPIRY_SAFETY_MARGIN_MS = 60_000; // refresh 60s early

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static class CachedToken {
        String accessToken;
        long expiresAtMillis;
    }

    // Keyed by refresh token, so blood pressure and heart rate each get
    // their own cached access token.
    private final Map<String, CachedToken> cache = new ConcurrentHashMap<>();

    public synchronized String getValidAccessToken(String refreshToken) {
        CachedToken cached = cache.get(refreshToken);

        boolean expired = cached == null
                || cached.accessToken == null
                || cached.expiresAtMillis <= System.currentTimeMillis() + EXPIRY_SAFETY_MARGIN_MS;

        if (!expired) {
            return cached.accessToken;
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(TOKEN_ENDPOINT, HttpMethod.POST, entity, String.class);

        JsonNode body;
        try {
            body = objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse Google token response: " + response.getBody(), e);
        }

        String accessToken = body.path("access_token").asText(null);
        int expiresInSeconds = body.path("expires_in").asInt(3600);

        if (accessToken == null) {
            throw new IllegalStateException("Google did not return an access_token: " + body);
        }

        CachedToken fresh = new CachedToken();
        fresh.accessToken = accessToken;
        fresh.expiresAtMillis = System.currentTimeMillis() + expiresInSeconds * 1000L;
        cache.put(refreshToken, fresh);

        return accessToken;
    }
}