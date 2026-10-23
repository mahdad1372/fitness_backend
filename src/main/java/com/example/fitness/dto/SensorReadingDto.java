package com.example.fitness.dto;

import java.util.List;

public class SensorReadingDto {
    private final Long liveSessionId;
    private final String phase;
    private final double elapsedMinutes;
    private final int durationMinutes;
    private final List<DataPointDto> readings;

    public SensorReadingDto(Long liveSessionId, String phase, double elapsedMinutes, int durationMinutes, List<DataPointDto> readings) {
        this.liveSessionId = liveSessionId;
        this.phase = phase;
        this.elapsedMinutes = elapsedMinutes;
        this.durationMinutes = durationMinutes;
        this.readings = readings;
    }

    public Long getLiveSessionId() { return liveSessionId; }
    public String getPhase() { return phase; }
    public double getElapsedMinutes() { return elapsedMinutes; }
    public int getDurationMinutes() { return durationMinutes; }
    public List<DataPointDto> getReadings() { return readings; }
}