package com.example.fitness.services;

import com.example.fitness.dto.SensorReadingDto;

import java.util.Optional;

public interface SensorDataProvider {
    Optional<SensorReadingDto> currentReading(Long liveSessionId);
}