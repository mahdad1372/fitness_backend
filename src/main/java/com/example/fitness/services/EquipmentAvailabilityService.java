package com.example.fitness.services;

import com.example.fitness.entitties.Equipment;
import com.example.fitness.entitties.SessionRequest;
import com.example.fitness.repositories.EquipmentRepository;
import com.example.fitness.repositories.SessionRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Turns "how many ACCEPTED bookings does this equipment type have on this
 * date" into "here are the actual free time windows" - a sweep-line over
 * every booking's start/end, counting at each moment how many units are
 * occupied. Same capacity-counting idea ResourceScheduler's Branch & Bound
 * already uses (a min-heap of busy-until times per equipment type) - this
 * just computes it for a whole day up front instead of checked one request
 * at a time during a search.
 *
 * Used by WeeklyPlannerService to give each planned exercise a REAL start
 * and end time instead of just a day-level minute budget - see
 * earliestSlot(), its one real entry point for that.
 */
@Service
@RequiredArgsConstructor
public class EquipmentAvailabilityService {

    // Gym operating hours, flat for every day of the week. If you ever need
    // different hours per day (e.g. shorter on Sundays), swap this pair for
    // a lookup keyed by DayOfWeek inside freeWindows() - nothing else here
    // or in WeeklyPlannerService needs to change, since everything derives
    // its day-start/day-end from these two constants.
    public static final int GYM_OPEN_HOUR = 8;
    public static final int GYM_CLOSE_HOUR = 20;

    private final SessionRequestRepository sessionRequestRepository;
    private final EquipmentRepository equipmentRepository;

    /** One contiguous stretch of `date` where exactly `freeUnits` of this equipment are open. */
    public record FreeWindow(LocalDateTime start, LocalDateTime end, int freeUnits) {}

    /**
     * Every contiguous window during gym hours on `date` for this equipment
     * type, each tagged with how many units are free during it. Windows
     * are always contiguous and gap-free across the whole
     * [GYM_OPEN_HOUR, GYM_CLOSE_HOUR) span - adjacent windows with the same
     * free count are merged into one, purely to keep the list short for
     * display; earliestSlot() below works correctly either way since it
     * scans across window boundaries rather than requiring a fit inside a
     * single entry.
     */
    public List<FreeWindow> freeWindows(Integer equipmentId, LocalDate date) {
        Equipment equipment = equipmentRepository.findById(equipmentId).orElse(null);
        if (equipment == null || equipment.getCapacity() == null) return List.of();
        int capacity = equipment.getCapacity();

        LocalDateTime dayStart = date.atTime(GYM_OPEN_HOUR, 0);
        LocalDateTime dayEnd = date.atTime(GYM_CLOSE_HOUR, 0);

        List<SessionRequest> accepted = sessionRequestRepository
                .findByEquipment_idAndStatus(equipmentId, "ACCEPTED");

        TreeSet<LocalDateTime> breakpoints = new TreeSet<>();
        breakpoints.add(dayStart);
        breakpoints.add(dayEnd);

        List<LocalDateTime[]> clipped = new ArrayList<>();
        for (SessionRequest r : accepted) {
            LocalDateTime start = toLocalDateTime(r.getStart_time());
            LocalDateTime end = toLocalDateTime(r.getEnd_time());
            if (end.isBefore(dayStart) || start.isAfter(dayEnd) || !start.isBefore(end)) continue;
            LocalDateTime clippedStart = start.isBefore(dayStart) ? dayStart : start;
            LocalDateTime clippedEnd = end.isAfter(dayEnd) ? dayEnd : end;
            clipped.add(new LocalDateTime[]{clippedStart, clippedEnd});
            breakpoints.add(clippedStart);
            breakpoints.add(clippedEnd);
        }

        List<LocalDateTime> points = new ArrayList<>(breakpoints);
        List<FreeWindow> windows = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            LocalDateTime sliceStart = points.get(i);
            LocalDateTime sliceEnd = points.get(i + 1);
            if (!sliceStart.isBefore(sliceEnd)) continue;

            int inUse = 0;
            for (LocalDateTime[] b : clipped) {
                if (!b[0].isAfter(sliceStart) && !b[1].isBefore(sliceEnd)) inUse++;
            }
            int free = Math.max(0, capacity - inUse);

            if (!windows.isEmpty()) {
                FreeWindow last = windows.get(windows.size() - 1);
                if (last.freeUnits() == free && last.end().equals(sliceStart)) {
                    windows.set(windows.size() - 1, new FreeWindow(last.start(), sliceEnd, free));
                    continue;
                }
            }
            windows.add(new FreeWindow(sliceStart, sliceEnd, free));
        }
        return windows;
    }

    /**
     * The earliest moment at or after `notBefore`, within gym hours on
     * `date`, where this equipment has a free unit for the ENTIRE
     * requested duration - what WeeklyPlannerService calls to give one
     * planned exercise a real start time. Empty if nothing that long is
     * free before closing.
     *
     * Candidates are only ever tried at `notBefore` itself or at a later
     * window's start - free capacity can only ever increase at a window
     * boundary (someone else's booking ending), so those are the only
     * points where a fit that didn't work a moment ago could start
     * working - a standard interval-scheduling argument.
     */
    public Optional<LocalDateTime> earliestSlot(Integer equipmentId, LocalDate date, LocalDateTime notBefore, int durationMinutes) {
        List<FreeWindow> windows = freeWindows(equipmentId, date);
        for (FreeWindow w : windows) {
            LocalDateTime candidateStart = w.start().isBefore(notBefore) ? notBefore : w.start();
            if (candidateStart.isAfter(w.end())) continue; // this window is entirely before notBefore
            LocalDateTime candidateEnd = candidateStart.plusMinutes(durationMinutes);
            if (fullyFree(windows, candidateStart, candidateEnd)) {
                return Optional.of(candidateStart);
            }
        }
        return Optional.empty();
    }

    // True when every window overlapping [start, end) has at least one free
    // unit, AND end doesn't run past the last window's end (= gym close).
    private boolean fullyFree(List<FreeWindow> windows, LocalDateTime start, LocalDateTime end) {
        if (windows.isEmpty()) return false;
        if (end.isAfter(windows.get(windows.size() - 1).end())) return false;
        for (FreeWindow w : windows) {
            if (w.end().isAfter(start) && w.start().isBefore(end) && w.freeUnits() < 1) {
                return false;
            }
        }
        return true;
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}