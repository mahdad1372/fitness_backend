package com.example.fitness.dto;

public class DataPointDto {
    private final String dataTypeName;
    private final long startTimeMillis;
    private final long endTimeMillis;
    private final double value;

    public DataPointDto(String dataTypeName, long startTimeMillis, long endTimeMillis, double value) {
        this.dataTypeName = dataTypeName;
        this.startTimeMillis = startTimeMillis;
        this.endTimeMillis = endTimeMillis;
        this.value = value;
    }

    public String getDataTypeName() { return dataTypeName; }
    public long getStartTimeMillis() { return startTimeMillis; }
    public long getEndTimeMillis() { return endTimeMillis; }
    public double getValue() { return value; }
}