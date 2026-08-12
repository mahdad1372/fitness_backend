package com.example.fitness.controllers;

import com.example.fitness.repositories.UserRepository;
import com.example.fitness.services.GoogleFitAuthService;
import com.example.fitness.services.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.fitness.entitties.User;

@RestController
@RequestMapping("/google-fit")
public class GoogleFitController {
    private final UserService userService;
    // These are now REFRESH tokens (long-lived), not access tokens.
    @Value("${BLOOD_PRESSURE_REFRESH_TOKEN}")
    private String bloodPressureRefreshToken;
    @Value("${BLOOD_PRESSURE_REFRESH_TOKEN_WRITE}")
    private String bloodPressureRefreshTokenWrite;
    @Value("${HEART_RATE_REFRESH_TOKEN}")
    private String heartRateRefreshToken;
    @Value("${HEART_RATE_REFRESH_TOKEN_WRITE}")
    private String heartRateRefreshTokenWrite;

    private final GoogleFitAuthService googleFitAuthService;

    public GoogleFitController(GoogleFitAuthService googleFitAuthService,UserService userService) {
        this.googleFitAuthService = googleFitAuthService;
        this.userService= userService;
    }

    // ---------------- Heart rate ----------------
    @GetMapping("/heart-rate/{userId}")
    public ResponseEntity<Object> getHeartRateAverage(@PathVariable("userId") Integer userId) {

        List<User> users = userService.finduserbyid(userId);
        if (users == null || users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No user with id " + userId));
        }
        User user = users.get(0);

        String dataSourceId = user.getHeartRateDataSource();
        if (dataSourceId == null) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED)
                    .body(Map.of("error", "This user has no heart rate data source yet.",
                            "createUrl", "/google-fit/heart-rate/datasource/" + userId));
        }

        // Same fixed window the PATCH endpoint writes into, so reads and writes line up.
        String startNanos = "1731110400000000000";
        String endNanos = "1731196800000000000";
        String datasetId = startNanos + "-" + endNanos;

        RestTemplate restTemplate = new RestTemplate();

        try {
            String accessToken = googleFitAuthService.getValidAccessToken(heartRateRefreshToken);

            URI uri = UriComponentsBuilder
                    .fromHttpUrl("https://www.googleapis.com/fitness/v1/users/me/dataSources/{dataSourceId}/datasets/{datasetId}")
                    .buildAndExpand(dataSourceId, datasetId)
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null || !body.containsKey("point")) {
                return ResponseEntity.ok(Map.of("userId", userId, "message", "No data found."));
            }

            List<Map<String, Object>> points = (List<Map<String, Object>>) body.get("point");

            double averageBpm = points.stream()
                    .map(p -> (List<Map<String, Object>>) p.get("value"))
                    .mapToDouble(v -> Double.parseDouble(v.get(0).get("fpVal").toString()))
                    .average().orElse(0.0);

            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "averageBpm", Math.round(averageBpm * 10.0) / 10.0,
                    "unit", "bpm",
                    "pointsCount", points.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

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

        URI uri = UriComponentsBuilder.fromHttpUrl(googleFitUrl).build(true).toUri();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            JsonNode points = root.path("point");
            if (points.isMissingNode() || !points.isArray() || points.isEmpty()) {
                return ResponseEntity.ok(Map.of("calories", 0));
            }

            JsonNode valueArray = points.get(0).path("value");
            float calories = 0;
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
    // ---------------- Create a blood pressure data source for one user ----------------
    @PatchMapping("/heart-rate/{userId}")
    public ResponseEntity<Object> writeHeartRate(@PathVariable("userId") Integer userId,
                                                 @RequestBody Map<String, Object> request) {

        List<User> users = userService.finduserbyid(userId);
        if (users == null || users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No user with id " + userId));
        }
        User user = users.get(0);

        String dataSourceId = user.getHeartRateDataSource();
        if (dataSourceId == null) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED)
                    .body(Map.of("error", "This user has no heart rate data source yet.",
                            "createUrl", "/google-fit/heart-rate/datasource/" + userId));
        }

        Object bpmRaw = request.get("bpm");
        if (bpmRaw == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "'bpm' is required in the request body."));
        }
        double bpm = Double.parseDouble(bpmRaw.toString());

        String accessToken;
        try {
            accessToken = googleFitAuthService.getValidAccessToken(heartRateRefreshTokenWrite);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not get a Google Fit access token: " + e.getMessage()));
        }

        // Same fixed window as blood pressure, so everything stays consistent.
        String startNanos = "1731110400000000000";
        String endNanos = "1731196800000000000";
        String datasetId = startNanos + "-" + endNanos;

        Map<String, Object> point = Map.of(
                "startTimeNanos", endNanos,
                "endTimeNanos", endNanos,
                "dataTypeName", "com.google.heart_rate.bpm",
                "value", List.of(
                        Map.of("fpVal", bpm)
                )
        );

        Map<String, Object> body = Map.of(
                "dataSourceId", dataSourceId,
                "minStartTimeNs", startNanos,
                "maxEndTimeNs", endNanos,
                "point", List.of(point)
        );

        RestTemplate restTemplate = new RestTemplate(
                new org.springframework.http.client.HttpComponentsClientHttpRequestFactory());
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            URI uri = UriComponentsBuilder
                    .fromHttpUrl("https://www.googleapis.com/fitness/v1/users/me/dataSources/{dataSourceId}/datasets/{datasetId}")
                    .buildAndExpand(dataSourceId, datasetId)
                    .toUri();

            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.PATCH, entity, Map.class);

            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "bpm", bpm,
                    "status", "written"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Google Fit rejected the write request: " + e.getMessage()));
        }
    }
    @PatchMapping("/blood-pressure/{userId}")
    public ResponseEntity<Object> writeBloodPressure(@PathVariable("userId") Integer userId,
                                                     @RequestBody Map<String, Object> request) {

        List<User> users = userService.finduserbyid(userId);
        if (users == null || users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No user with id " + userId));
        }
        User user = users.get(0);

        String dataSourceId = user.getBloodPressureDataSource();
        if (dataSourceId == null) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED)
                    .body(Map.of("error", "This user has no blood pressure data source yet.",
                            "createUrl", "/google-fit/blood-pressure/datasource/" + userId));
        }

        Object systolicRaw = request.get("systolic");
        Object diastolicRaw = request.get("diastolic");
        if (systolicRaw == null || diastolicRaw == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Both 'systolic' and 'diastolic' are required in the request body."));
        }
        double systolic = Double.parseDouble(systolicRaw.toString());
        double diastolic = Double.parseDouble(diastolicRaw.toString());
        int bodyPosition = request.get("bodyPosition") != null ? Integer.parseInt(request.get("bodyPosition").toString()) : 0;
        int measurementLocation = request.get("measurementLocation") != null ? Integer.parseInt(request.get("measurementLocation").toString()) : 0;

        String accessToken;
        try {
            accessToken = googleFitAuthService.getValidAccessToken(bloodPressureRefreshTokenWrite);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not get a Google Fit access token: " + e.getMessage()));
        }

        // Fixed time window instead of "now".
        String startNanos = "1731110400000000000";
        String endNanos = "1731196800000000000";
        String datasetId = startNanos + "-" + endNanos;

        Map<String, Object> point = Map.of(
                "startTimeNanos", endNanos,
                "endTimeNanos", endNanos,
                "dataTypeName", "com.google.blood_pressure",
                "value", List.of(
                        Map.of("fpVal", systolic),
                        Map.of("fpVal", diastolic),
                        Map.of("intVal", bodyPosition),
                        Map.of("intVal", measurementLocation)
                )
        );

        Map<String, Object> body = Map.of(
                "dataSourceId", dataSourceId,
                "minStartTimeNs", startNanos,
                "maxEndTimeNs", endNanos,
                "point", List.of(point)
        );

        RestTemplate restTemplate = new RestTemplate(
                new org.springframework.http.client.HttpComponentsClientHttpRequestFactory());
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            URI uri = UriComponentsBuilder
                    .fromHttpUrl("https://www.googleapis.com/fitness/v1/users/me/dataSources/{dataSourceId}/datasets/{datasetId}")
                    .buildAndExpand(dataSourceId, datasetId)
                    .toUri();

            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.PATCH, entity, Map.class);

            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "systolic", systolic,
                    "diastolic", diastolic,
                    "status", "written"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Google Fit rejected the write request: " + e.getMessage()));
        }
    }
    @PostMapping("/heart-rate/datasource/{userId}")
    public ResponseEntity<Object> createHeartRateDataSource(@PathVariable("userId") Integer userId) {

        String accessToken;
        try {
            accessToken = googleFitAuthService.getValidAccessToken(heartRateRefreshTokenWrite);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not get a Google Fit access token: " + e.getMessage()));
        }

        String uniqueDeviceUid = "hr-device-user-" + userId;
        String streamName = "HR_User_" + userId;
        String expectedDataStreamId = "raw:com.google.heart_rate.bpm:220955582509:MyCompany:HR_Device:"
                + uniqueDeviceUid + ":" + streamName;

        Map<String, Object> body = Map.of(
                "dataStreamName", streamName,
                "type", "raw",
                "application", Map.of("name", "Fitness App", "version", "1"),
                "dataType", Map.of("name", "com.google.heart_rate.bpm"),
                "device", Map.of(
                        "manufacturer", "MyCompany",
                        "model", "HR_Device",
                        "type", "watch",
                        "uid", uniqueDeviceUid,
                        "version", "1.0"
                )
        );

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        String dataStreamId;
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://www.googleapis.com/fitness/v1/users/me/dataSources",
                    HttpMethod.POST, entity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            dataStreamId = responseBody != null ? (String) responseBody.get("dataStreamId") : null;

            if (dataStreamId == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Google did not return a dataStreamId", "response", responseBody));
            }

        } catch (org.springframework.web.client.HttpClientErrorException.Conflict e) {
            dataStreamId = expectedDataStreamId;

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Google Fit rejected the create-dataSource request: " + e.getMessage()));
        }
        userService.update_datastreamheartrate(userId, dataStreamId);
        return ResponseEntity.ok(Map.of("userId", userId, "dataSourceId", dataStreamId, "status", "created"));
    }
    @PostMapping("/blood-pressure/datasource/{userId}")
    public ResponseEntity<Object> createBloodPressureDataSource(@PathVariable("userId") Integer userId) {

        String accessToken;
        try {
            accessToken = googleFitAuthService.getValidAccessToken(bloodPressureRefreshTokenWrite);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not get a Google Fit access token: " + e.getMessage()));
        }

        String uniqueDeviceUid = "bp-device-user-" + userId;
        String streamName = "BP_User_" + userId;
        // Same shape Google builds server-side: type:dataType:projectNumber:manufacturer:model:uid:streamName
        String expectedDataStreamId = "raw:com.google.blood_pressure:220955582509:MyCompany:BP_Device:"
                + uniqueDeviceUid + ":" + streamName;

        Map<String, Object> body = Map.of(
                "dataStreamName", streamName,
                "type", "raw",
                "application", Map.of("name", "Fitness App", "version", "1"),
                "dataType", Map.of("name", "com.google.blood_pressure"),
                "device", Map.of(
                        "manufacturer", "MyCompany",
                        "model", "BP_Device",
                        "type", "scale",
                        "uid", uniqueDeviceUid,
                        "version", "1.0"
                )
        );

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        String dataStreamId;
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://www.googleapis.com/fitness/v1/users/me/dataSources",
                    HttpMethod.POST, entity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            dataStreamId = responseBody != null ? (String) responseBody.get("dataStreamId") : null;

            if (dataStreamId == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Google did not return a dataStreamId", "response", responseBody));
            }

        } catch (org.springframework.web.client.HttpClientErrorException.Conflict e) {
            dataStreamId = expectedDataStreamId;

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Google Fit rejected the create-dataSource request: " + e.getMessage()));
        }

        userService.update_datastream(userId, dataStreamId);

        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "dataSourceId", dataStreamId,
                "status", "created"
        ));
    }
    @GetMapping("/blood-pressure/{userId}")
    public ResponseEntity<Object> getBloodPressureAverage(@PathVariable("userId") Integer userId) {

        List<User> users = userService.finduserbyid(userId);
        if (users == null || users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No user with id " + userId));
        }
        User user = users.get(0);

        String dataSourceId = user.getBloodPressureDataSource();
        if (dataSourceId == null) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED)
                    .body(Map.of("error", "This user has no blood pressure data source yet.",
                            "createUrl", "/google-fit/blood-pressure/datasource/" + userId));
        }

        // Same fixed window the PATCH endpoint writes into, so reads and writes line up.
        String startNanos = "1731110400000000000";
        String endNanos = "1731196800000000000";
        String datasetId = startNanos + "-" + endNanos;

        RestTemplate restTemplate = new RestTemplate();

        try {
            String accessToken = googleFitAuthService.getValidAccessToken(bloodPressureRefreshToken);

            URI uri = UriComponentsBuilder
                    .fromHttpUrl("https://www.googleapis.com/fitness/v1/users/me/dataSources/{dataSourceId}/datasets/{datasetId}")
                    .buildAndExpand(dataSourceId, datasetId)
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null || !body.containsKey("point")) {
                return ResponseEntity.ok(Map.of("userId", userId, "message", "No data found."));
            }

            List<Map<String, Object>> points = (List<Map<String, Object>>) body.get("point");

            double avgSystolic = points.stream()
                    .map(p -> (List<Map<String, Object>>) p.get("value"))
                    .mapToDouble(v -> Double.parseDouble(v.get(0).get("fpVal").toString()))
                    .average().orElse(0.0);

            double avgDiastolic = points.stream()
                    .map(p -> (List<Map<String, Object>>) p.get("value"))
                    .mapToDouble(v -> Double.parseDouble(v.get(1).get("fpVal").toString()))
                    .average().orElse(0.0);

            return ResponseEntity.ok(Map.of(
                    "userId", userId,
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