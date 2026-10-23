package com.example.fitness.dto;

public class PreSessionCheckDto {
    private final int systolic;
    private final int diastolic;
    private final boolean clearedToStart;

    public PreSessionCheckDto(int systolic, int diastolic, boolean clearedToStart) {
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.clearedToStart = clearedToStart;
    }

    public int getSystolic() { return systolic; }
    public int getDiastolic() { return diastolic; }
    public boolean isClearedToStart() { return clearedToStart; }
}