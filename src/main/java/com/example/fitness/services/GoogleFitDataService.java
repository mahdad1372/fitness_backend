package com.example.fitness.services;

import com.example.fitness.entitties.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Fetches a user's live heart rate / blood pressure averages straight from
 * Google Fit, for internal use by other services (currently: the
 * cardiovascular risk calculation in Health_metricsService).
 *
 * This mirrors the read logic already in GoogleFitController's
 * GET /google-fit/heart-rate/{userId} and GET /google-fit/blood-pressure/{userId}
 * endpoints, but exposes it as a plain method call instead of an HTTP
 * endpoint, so it can be called directly from another service/controller
 * without an extra network hop.
 */
@Service
public class GoogleFitDataService {

    // Same fixed window GoogleFitController reads/writes, so results line up.
    private static final String START_NANOS = "1731110400000000000";
    private static final String END_NANOS = "1731196800000000000";

    @Value("${HEART_RATE_REFRESH_TOKEN}")
    private String heartRateRefreshToken;
    @Value("${BLOOD_PRESSURE_REFRESH_TOKEN}")
    private String bloodPressureRefreshToken;

    private final GoogleFitAuthService googleFitAuthService;

    public GoogleFitDataService(GoogleFitAuthService googleFitAuthService) {
        this.googleFitAuthService = googleFitAuthService;
    }

    /**
     * Average heart rate (bpm) over the fixed data window, for this user.
     * Throws IllegalStateException if the user has no heart rate data
     * source yet, or if Google Fit has no points for the window.
     */
    @SuppressWarnings("unchecked")
    public double getHeartRateAverage(User user) {
        String dataSourceId = user.getHeartRateDataSource();
        if (dataSourceId == null) {
            throw new IllegalStateException("This user has no heart rate data source yet.");
        }

        String accessToken = googleFitAuthService.getValidAccessToken(heartRateRefreshToken);
        Map<String, Object> body = fetchDataset(dataSourceId, accessToken);

        if (body == null || !body.containsKey("point")) {
            throw new IllegalStateException("No heart rate data found for this user.");
        }

        List<Map<String, Object>> points = (List<Map<String, Object>>) body.get("point");
        return points.stream()
                .map(p -> (List<Map<String, Object>>) p.get("value"))
                .mapToDouble(v -> Double.parseDouble(v.get(0).get("fpVal").toString()))
                .average()
                .orElseThrow(() -> new IllegalStateException("No heart rate data found for this user."));
    }

    /**
     * Average systolic reading over the fixed data window, for this user —
     * this is the single number the cardiovascular risk formula expects
     * for "blood pressure".
     * Throws IllegalStateException if the user has no blood pressure data
     * source yet, or if Google Fit has no points for the window.
     */
    @SuppressWarnings("unchecked")
    public double getBloodPressureAverage(User user) {
        String dataSourceId = user.getBloodPressureDataSource();
        if (dataSourceId == null) {
            throw new IllegalStateException("This user has no blood pressure data source yet.");
        }

        String accessToken = googleFitAuthService.getValidAccessToken(bloodPressureRefreshToken);
        Map<String, Object> body = fetchDataset(dataSourceId, accessToken);

        if (body == null || !body.containsKey("point")) {
            throw new IllegalStateException("No blood pressure data found for this user.");
        }

        List<Map<String, Object>> points = (List<Map<String, Object>>) body.get("point");
        return points.stream()
                .map(p -> (List<Map<String, Object>>) p.get("value"))
                .mapToDouble(v -> Double.parseDouble(v.get(0).get("fpVal").toString()))
                .average()
                .orElseThrow(() -> new IllegalStateException("No blood pressure data found for this user."));
    }

    private Map<String, Object> fetchDataset(String dataSourceId, String accessToken) {
        String datasetId = START_NANOS + "-" + END_NANOS;

        URI uri = UriComponentsBuilder
                .fromHttpUrl("https://www.googleapis.com/fitness/v1/users/me/dataSources/{dataSourceId}/datasets/{datasetId}")
                .buildAndExpand(dataSourceId, datasetId)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
        return response.getBody();
    }
}