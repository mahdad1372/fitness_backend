package com.example.fitness.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/google-fit")
public class GoogleFitController {

    @Value("${BLOOD_PRESSURE_TOKEN}")
    private String BLOOD_PRESSURE_TOKEN;
    @Value("${HEART_RATE_TOKEN}")
    private String HEART_RATE_TOKEN;
    private float calculateAverageHeartRate(JsonNode root) {
        JsonNode points = root.path("point");

        if (!points.isArray() || points.isEmpty()) {
            return 0;
        }

        float sum = 0;
        int count = 0;

        for (JsonNode point : points) {
            JsonNode valueArray = point.path("value");

            if (valueArray.isArray() && !valueArray.isEmpty()) {
                float bpm = (float) valueArray.get(0).path("fpVal").asDouble();
                sum += bpm;
                count++;
            }
        }

        return (count == 0) ? 0 : sum / count;
    }

    // ---------------- Endpoint ----------------
    @GetMapping("/heart-rate")
    public ResponseEntity<Map<String, Object>> getHeartRateAverage() {
        RestTemplate restTemplate = new RestTemplate();
        String dataSourceId = "derived:com.google.heart_rate.bpm:com.google.android.gms:merge_heart_rate_bpm";
        String datasetId = "1731110400000000000-1731196800000000000";
        try {
            // 2. Build URI (Handles encoding for the colons in dataSourceId)
            URI uri = UriComponentsBuilder
                    .fromHttpUrl("https://www.googleapis.com/fitness/v1/users/me/dataSources/{dataSourceId}/datasets/{datasetId}")
                    .buildAndExpand(dataSourceId, datasetId)
                    .toUri();

            // 3. Prepare Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(HEART_RATE_TOKEN);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 4. Request data from Google
            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = response.getBody();

            // 5. Logic to calculate average
            if (body != null && body.containsKey("point")) {
                List<Map<String, Object>> points = (List<Map<String, Object>>) body.get("point");

                double average = points.stream()
                        // Get the 'value' array inside each point
                        .flatMap(p -> ((List<Map<String, Object>>) p.get("value")).stream())
                        // Extract the 'fpVal'
                        .mapToDouble(v -> {
                            Object val = v.get("fpVal");
                            // Jackson may parse 72 as Integer and 72.5 as Double
                            return (val instanceof Integer) ? ((Integer) val).doubleValue() : (Double) val;
                        })
                        .average()
                        .orElse(0.0);

                // 6. Return the result
                return ResponseEntity.ok(Map.of(
                        "average_bpm", Math.round(average * 100.0) / 100.0,
                        "data_points_analyzed", points.size(),
                        "status", "success"
                ));
            }

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get nutrition like the calories,fat,protein,carbs
    // --- Nutrition endpoint ---
    @GetMapping("/nutrition")
    public ResponseEntity<?> getNutrition(@RequestHeader("Authorization") String authorizationHeader) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authorization header missing or invalid. Use 'Bearer <access_token>'.");
        }

        String accessToken = authorizationHeader.substring("Bearer ".length());

        String googleFitUrl =
                "https://www.googleapis.com/fitness/v1/users/me/dataSources/" +
                        "raw:com.google.nutrition:428801282059:my-nutrition-source/" +
                        "datasets/1731110400000000000-1731196800000000000";

        URI uri = UriComponentsBuilder.fromHttpUrl(googleFitUrl)
                .build(true)
                .toUri();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

        // ---- Extract calories from JSON ----
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            JsonNode points = root.path("point");
            if (points.isMissingNode() || !points.isArray() || points.isEmpty()) {
                return ResponseEntity.ok(Map.of("calories", 0));
            }

            JsonNode valueArray = points.get(0).path("value");
            float calories = 0;

            // value[0] contains mapVal (nutrients)
            JsonNode nutrients = valueArray.get(0).path("mapVal");

            for (JsonNode item : nutrients) {
                if (item.path("key").asText().equals("calories")) {
                    calories = (float) item.path("value").path("fpVal").asDouble();
                    break;
                }
            }

            return ResponseEntity.ok(calories);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error parsing Google Fit response: " + e.getMessage());
        }
    }

    @GetMapping("/blood-pressure")
    public ResponseEntity<Object> getBloodPressureAverage() {
        RestTemplate restTemplate = new RestTemplate();
        String dataSourceId = "raw:com.google.blood_pressure:407408718192:MyCompany:BP_Device_001:bp-device-001-unique:My BP Device";
        String datasetId = "1574159699023000000-1574159699023000000";

        try {
            URI uri = UriComponentsBuilder
                    .fromHttpUrl("https://www.googleapis.com/fitness/v1/users/me/dataSources/{dataSourceId}/datasets/{datasetId}")
                    .buildAndExpand(dataSourceId, datasetId)
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(BLOOD_PRESSURE_TOKEN);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null || !body.containsKey("point")) {
                return ResponseEntity.ok("No data found.");
            }

            List<Map<String, Object>> points = (List<Map<String, Object>>) body.get("point");

            // Extract Systolic (Index 0) and Diastolic (Index 1) from each point
            double avgSystolic = points.stream()
                    .map(p -> (List<Map<String, Object>>) p.get("value"))
                    .mapToDouble(v -> Double.parseDouble(v.get(0).get("fpVal").toString()))
                    .average().orElse(0.0);

            double avgDiastolic = points.stream()
                    .map(p -> (List<Map<String, Object>>) p.get("value"))
                    .mapToDouble(v -> Double.parseDouble(v.get(1).get("fpVal").toString()))
                    .average().orElse(0.0);

            return ResponseEntity.ok(Map.of(
                    "averageSystolic", Math.round(avgSystolic * 10.0) / 10.0,
                    "averageDiastolic", Math.round(avgDiastolic * 10.0) / 10.0,
                    "unit", "mmHg",
                    "pointsCount", points.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}