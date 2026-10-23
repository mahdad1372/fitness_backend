package com.example.fitness.services;

import com.example.fitness.dto.DataPointDto;
import com.example.fitness.dto.SensorReadingDto;
import com.example.fitness.entitties.LiveSession;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VirtualSensorEngine implements SensorDataProvider {

    // One in-memory simulation per live session. Intentionally not
    // persisted tick-by-tick: it's a stand-in for hardware we don't have,
    // so losing it on a restart just means tapping Start again, and it
    // avoids a database write every few seconds for data nobody needs
    // to keep forever.
    private final Map<Long, SensorState> sessions = new ConcurrentHashMap<>();
    private final Random random = new Random();

    private static final double RESTING_HEART_RATE = 68;
    private static final double CALORIES_PER_BPM_ABOVE_RESTING_PER_MINUTE = 0.12;
    private static final long TICK_MILLIS = 3000;

    private enum Phase { WARMUP, ACTIVE, COOLDOWN, DONE }

    private static class SensorState {
        long startedAtMillis;
        int durationMinutes;
        double targetHeartRate;
        boolean trackSteps;

        Phase phase = Phase.WARMUP;
        double heartRate = RESTING_HEART_RATE;
        double heartRateAtCooldownStart = RESTING_HEART_RATE;
        int steps = 0;
        int reps = 0;
        double calories = 0;
    }

    public void startSession(Long liveSessionId, LiveSession session) {
        SensorState state = new SensorState();
        state.startedAtMillis = System.currentTimeMillis();
        state.durationMinutes = session.getDuration_minutes();
        state.targetHeartRate = Math.min(180, RESTING_HEART_RATE + session.getDifficulty_level() * 8.0);
        state.trackSteps = Boolean.TRUE.equals(session.getTrack_steps());
        sessions.put(liveSessionId, state);
    }

    public void finishSession(Long liveSessionId) {
        sessions.remove(liveSessionId);
    }

    /**
     * Runs every few seconds for every session currently live. Which
     * formula applies depends only on which phase the session is in, and
     * each tick nudges forward from the PREVIOUS value rather than
     * picking a fresh random number - that's what keeps the curve looking
     * continuous instead of jagged.
     */
    @Scheduled(fixedRate = TICK_MILLIS)
    public void tick() {
        for (SensorState state : sessions.values()) {
            double elapsedMinutes = (System.currentTimeMillis() - state.startedAtMillis) / 60000.0;
            double warmupEnd = state.durationMinutes * 0.15;
            double activeEnd = state.durationMinutes * 0.85;
            Phase previousPhase = state.phase;

            if (elapsedMinutes >= state.durationMinutes) {
                state.phase = Phase.DONE;
            } else if (elapsedMinutes < warmupEnd) {
                state.phase = Phase.WARMUP;
                double progress = warmupEnd == 0 ? 1 : elapsedMinutes / warmupEnd;
                state.heartRate = RESTING_HEART_RATE
                        + (state.targetHeartRate - RESTING_HEART_RATE) * (1 - Math.exp(-3 * progress));
            } else if (elapsedMinutes < activeEnd) {
                state.phase = Phase.ACTIVE;
                double nudge = (random.nextDouble() - 0.5) * 4;
                state.heartRate = Math.max(state.targetHeartRate - 6,
                        Math.min(state.targetHeartRate + 6, state.heartRate + nudge));
            } else {
                if (previousPhase != Phase.COOLDOWN) {
                    state.heartRateAtCooldownStart = state.heartRate;
                }
                state.phase = Phase.COOLDOWN;
                double progress = (activeEnd >= state.durationMinutes)
                        ? 1 : (elapsedMinutes - activeEnd) / (state.durationMinutes - activeEnd);
                state.heartRate = RESTING_HEART_RATE
                        + (state.heartRateAtCooldownStart - RESTING_HEART_RATE) * Math.exp(-3 * progress);
            }

            boolean accumulating = state.phase == Phase.WARMUP || state.phase == Phase.ACTIVE;
            if (accumulating) {
                if (state.trackSteps) {
                    state.steps += 2 + random.nextInt(5);
                } else if (state.phase == Phase.ACTIVE && random.nextDouble() < 0.4) {
                    state.reps += 1;
                }
                double tickMinutes = TICK_MILLIS / 60000.0;
                state.calories += Math.max(0, state.heartRate - RESTING_HEART_RATE)
                        * CALORIES_PER_BPM_ABOVE_RESTING_PER_MINUTE * tickMinutes;
            }
        }
    }

    @Override
    public Optional<SensorReadingDto> currentReading(Long liveSessionId) {
        SensorState state = sessions.get(liveSessionId);
        if (state == null) return Optional.empty();

        long now = System.currentTimeMillis();
        List<DataPointDto> readings = new ArrayList<>();
        readings.add(new DataPointDto("com.google.heart_rate.bpm", now, now, round1(state.heartRate)));
        if (state.trackSteps) {
            readings.add(new DataPointDto("com.google.step_count.delta", now, now, state.steps));
        } else {
            // Google Fit has no standard rep-count type - real wearables
            // don't track strength reps the way they track steps - so this
            // is a custom type, kept in the same shape anyway.
            readings.add(new DataPointDto("com.example.rep_count.delta", now, now, state.reps));
        }
        readings.add(new DataPointDto("com.google.calories.expended", now, now, round1(state.calories)));

        double elapsedMinutes = (now - state.startedAtMillis) / 60000.0;
        return Optional.of(new SensorReadingDto(
                liveSessionId, state.phase.name(), round1(elapsedMinutes), state.durationMinutes, readings));
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}