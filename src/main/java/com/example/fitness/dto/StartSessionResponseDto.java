package com.example.fitness.dto;

public class StartSessionResponseDto {
    private final Long liveSessionId;
    private final PreSessionCheckDto preSessionCheck;

    public StartSessionResponseDto(Long liveSessionId, PreSessionCheckDto preSessionCheck) {
        this.liveSessionId = liveSessionId;
        this.preSessionCheck = preSessionCheck;
    }

    public Long getLiveSessionId() { return liveSessionId; }
    public PreSessionCheckDto getPreSessionCheck() { return preSessionCheck; }
}