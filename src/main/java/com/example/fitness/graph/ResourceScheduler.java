package com.example.fitness.graph;

import java.util.*;

/**
 * "Studio Resource Scheduler": given a set of session requests, each tied to
 * a SPECIFIC coach (every student has exactly one active coach - a flat
 * monthly subscription, not a per-session fee) and (optionally) a unit of
 * some shared equipment type with limited capacity, select the subset to
 * accept that maximizes the NUMBER of sessions accepted, subject to:
 *   - no coach double-booked (capacity 1 per coach, since it's a specific person)
 *   - no equipment type double-booked beyond its unit count at any instant
 *
 * Why this is still NP-hard even without per-session pricing: with only ONE
 * shared resource (e.g. just coaches), conflicts between requests form an
 * INTERVAL GRAPH, and max independent set on an interval graph is solvable
 * in O(n log n). But once a second, independently-shared resource
 * (equipment) is added, two requests with DIFFERENT coaches can still
 * conflict (if they want the same scarce equipment at overlapping times).
 * That breaks the interval-graph structure: the conflict graph is now a
 * general graph, and max independent set on a general graph - even the
 * UNWEIGHTED version, which is what this is now - is NP-hard. So dropping
 * per-session pricing didn't make the underlying search any easier.
 *
 * The approach is exact Branch & Bound: a 0/1 decision (accept/reject) per
 * request, pruned with an admissible upper bound (current accepted count +
 * however many requests are still undecided, which can only overestimate
 * what's truly achievable since it ignores conflicts).
 */
public class ResourceScheduler {

    public record SessionRequest(
            int id, String studentName, String coachName, String equipmentType,
            int start, int end
    ) {}

    public record ScheduleResult(List<SessionRequest> accepted, List<SessionRequest> rejected, long nodesExplored) {}

    public static ScheduleResult solve(List<SessionRequest> requests, Map<String, Integer> equipmentCapacities) {
        int n = requests.size();
        List<SessionRequest> sorted = new ArrayList<>(requests);
        sorted.sort(Comparator.comparingInt(SessionRequest::start));

        BranchAndBound bb = new BranchAndBound(sorted, equipmentCapacities);
        bb.search(0, new HashMap<>(), new HashMap<>(), 0, new ArrayDeque<>());

        Set<Integer> acceptedIds = new HashSet<>(bb.bestAcceptedIds);
        List<SessionRequest> accepted = new ArrayList<>();
        List<SessionRequest> rejected = new ArrayList<>();
        for (SessionRequest r : sorted) {
            (acceptedIds.contains(r.id()) ? accepted : rejected).add(r);
        }
        accepted.sort(Comparator.comparingInt(SessionRequest::start));
        return new ScheduleResult(accepted, rejected, bb.nodesExplored);
    }

    private static class BranchAndBound {
        final List<SessionRequest> requests;
        final Map<String, Integer> equipmentCapacities;
        final int n;

        int bestCount = 0;
        List<Integer> bestAcceptedIds = new ArrayList<>();
        long nodesExplored = 0;

        BranchAndBound(List<SessionRequest> requests, Map<String, Integer> equipmentCapacities) {
            this.requests = requests;
            this.equipmentCapacities = equipmentCapacities;
            this.n = requests.size();
        }

        void search(int index, Map<String, Integer> coachEndTimes, Map<String, PriorityQueue<Integer>> equipmentEndTimes,
                    int currentCount, Deque<Integer> currentAcceptedIds) {
            nodesExplored++;

            if (index == n) {
                if (currentCount > bestCount) {
                    bestCount = currentCount;
                    bestAcceptedIds = new ArrayList<>(currentAcceptedIds);
                }
                return;
            }

            if (currentCount + (n - index) <= bestCount) {
                return; // can't possibly beat the best found - prune
            }

            SessionRequest r = requests.get(index);

            Integer coachBusyUntil = coachEndTimes.get(r.coachName());
            boolean coachFree = coachBusyUntil == null || coachBusyUntil <= r.start();

            boolean equipmentFree = true;
            PriorityQueue<Integer> equipHeap = null;
            Integer capacity = null;
            if (r.equipmentType() != null) {
                capacity = equipmentCapacities.getOrDefault(r.equipmentType(), 0);
                equipHeap = equipmentEndTimes.computeIfAbsent(r.equipmentType(), k -> new PriorityQueue<>());
                equipmentFree = equipHeap.size() < capacity || (!equipHeap.isEmpty() && equipHeap.peek() <= r.start());
            }

            if (coachFree && equipmentFree) {
                Integer prevCoachEnd = coachEndTimes.put(r.coachName(), r.end());

                Integer removedEquipEnd = null;
                if (r.equipmentType() != null) {
                    if (!equipHeap.isEmpty() && equipHeap.peek() <= r.start()) {
                        removedEquipEnd = equipHeap.poll();
                    }
                    equipHeap.add(r.end());
                }

                currentAcceptedIds.addLast(r.id());
                search(index + 1, coachEndTimes, equipmentEndTimes, currentCount + 1, currentAcceptedIds);
                currentAcceptedIds.removeLast();

                if (r.equipmentType() != null) {
                    equipHeap.remove(r.end());
                    if (removedEquipEnd != null) equipHeap.add(removedEquipEnd);
                }

                if (prevCoachEnd == null) coachEndTimes.remove(r.coachName());
                else coachEndTimes.put(r.coachName(), prevCoachEnd);
            }

            search(index + 1, coachEndTimes, equipmentEndTimes, currentCount, currentAcceptedIds);
        }
    }
}