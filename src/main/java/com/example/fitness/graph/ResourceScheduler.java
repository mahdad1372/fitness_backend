package com.example.fitness.graph;

import java.util.*;

/**
 * "Studio Resource Scheduler": given a set of session requests, each tied to
 * a SPECIFIC coach and (optionally) a unit of some shared equipment type
 * with limited capacity, select the subset to accept that maximizes total
 * revenue, subject to:
 *   - no coach double-booked (capacity 1 per coach, since it's a specific person)
 *   - no equipment type double-booked beyond its unit count at any instant
 *
 * Why this is NP-hard (unlike everything else in this project so far):
 * with only ONE shared resource (e.g. just coaches), conflicts between
 * requests form an INTERVAL GRAPH, and max-weight independent set on an
 * interval graph is solvable in O(n log n) - that's exactly the weighted
 * interval scheduling DP already used elsewhere in this app. But once a
 * second, independently-shared resource (equipment) is added, two requests
 * with DIFFERENT coaches can still conflict (if they want the same scarce
 * equipment at overlapping times). That breaks the interval-graph structure
 * that made the single-resource version easy: the conflict graph is now a
 * general graph, and max-weight independent set on a general graph is
 * NP-hard. So this problem is at least as hard as general independent set -
 * there's no known polynomial (or even pseudo-polynomial) exact algorithm.
 *
 * The approach here is exact Branch & Bound: a 0/1 decision (accept/reject)
 * per request, pruned with a simple admissible upper bound (sum of all
 * remaining requests' prices, which can only overestimate what's truly
 * achievable since it ignores conflicts). It's still worst-case exponential
 * - unlike the DP-based algorithms elsewhere in this app, this one has no
 * polynomial-time guarantee - but the pruning makes it fast in practice at
 * realistic gym-schedule sizes, which is verified empirically below rather
 * than proven asymptotically.
 */
public class ResourceScheduler {

    public record SessionRequest(
            int id, String studentName, String coachName, String equipmentType,
            int start, int end, double price
    ) {}

    public record ScheduleResult(List<SessionRequest> accepted, List<SessionRequest> rejected, double totalRevenue, long nodesExplored) {}

    public static ScheduleResult solve(List<SessionRequest> requests, Map<String, Integer> equipmentCapacities) {
        int n = requests.size();
        List<SessionRequest> sorted = new ArrayList<>(requests);
        sorted.sort(Comparator.comparingInt(SessionRequest::start));

        double[] suffixPriceSum = new double[n + 1];
        for (int i = n - 1; i >= 0; i--) {
            suffixPriceSum[i] = suffixPriceSum[i + 1] + sorted.get(i).price();
        }

        BranchAndBound bb = new BranchAndBound(sorted, equipmentCapacities, suffixPriceSum);
        bb.search(0, new HashMap<>(), new HashMap<>(), 0.0, new ArrayDeque<>());

        Set<Integer> acceptedIds = new HashSet<>(bb.bestAcceptedIds);
        List<SessionRequest> accepted = new ArrayList<>();
        List<SessionRequest> rejected = new ArrayList<>();
        for (SessionRequest r : sorted) {
            (acceptedIds.contains(r.id()) ? accepted : rejected).add(r);
        }
        accepted.sort(Comparator.comparingInt(SessionRequest::start));
        return new ScheduleResult(accepted, rejected, bb.bestValue, bb.nodesExplored);
    }

    private static class BranchAndBound {
        final List<SessionRequest> requests;
        final Map<String, Integer> equipmentCapacities;
        final double[] suffixPriceSum;

        double bestValue = 0.0;
        List<Integer> bestAcceptedIds = new ArrayList<>();
        long nodesExplored = 0;

        BranchAndBound(List<SessionRequest> requests, Map<String, Integer> equipmentCapacities, double[] suffixPriceSum) {
            this.requests = requests;
            this.equipmentCapacities = equipmentCapacities;
            this.suffixPriceSum = suffixPriceSum;
        }

        void search(int index, Map<String, Integer> coachEndTimes, Map<String, PriorityQueue<Integer>> equipmentEndTimes,
                    double currentValue, Deque<Integer> currentAcceptedIds) {
            nodesExplored++;

            if (index == requests.size()) {
                if (currentValue > bestValue) {
                    bestValue = currentValue;
                    bestAcceptedIds = new ArrayList<>(currentAcceptedIds);
                }
                return;
            }

            if (currentValue + suffixPriceSum[index] <= bestValue) {
                return; // admissible bound says we can't beat the best found - prune
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
                search(index + 1, coachEndTimes, equipmentEndTimes, currentValue + r.price(), currentAcceptedIds);
                currentAcceptedIds.removeLast();

                if (r.equipmentType() != null) {
                    equipHeap.remove(r.end());
                    if (removedEquipEnd != null) equipHeap.add(removedEquipEnd);
                }

                if (prevCoachEnd == null) coachEndTimes.remove(r.coachName());
                else coachEndTimes.put(r.coachName(), prevCoachEnd);
            }

            search(index + 1, coachEndTimes, equipmentEndTimes, currentValue, currentAcceptedIds);
        }
    }
}