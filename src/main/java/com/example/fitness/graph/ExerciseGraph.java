package com.example.fitness.graph;

import java.util.*;

/**
 * Plain, framework-free directed graph of exercises. Nodes are exercise IDs
 * (Integer); edges carry a relationship type and a non-negative weight.
 * This class has no dependency on Spring or JPA, so it can be unit tested
 * with a handful of in-memory edges and no database.
 */
public class ExerciseGraph {

    public enum EdgeType { PROGRESSION, SUBSTITUTE }

    public record Edge(int to, EdgeType type, int weight) {}

    private final Map<Integer, List<Edge>> adjacency = new HashMap<>();

    public void addNode(int id) {
        adjacency.putIfAbsent(id, new ArrayList<>());
    }

    public void addEdge(int from, int to, EdgeType type, int weight) {
        addNode(from);
        addNode(to);
        adjacency.get(from).add(new Edge(to, type, weight));
    }

    public Set<Integer> nodes() {
        return adjacency.keySet();
    }

    // 1) BFS - find valid substitutes when nodes are excluded (no equipment / injury).
    //    Unweighted reachability: every SUBSTITUTE edge is equally "costly" to take,
    //    so we only care about hop count, not difficulty delta.
    //    Time: O(V + E)   Space: O(V)
    public List<Integer> findSubstitutes(int start, Set<Integer> excluded, int maxResults) {
        List<Integer> result = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new ArrayDeque<>();
        visited.add(start);
        queue.add(start);

        while (!queue.isEmpty() && result.size() < maxResults) {
            int current = queue.poll();
            for (Edge edge : adjacency.getOrDefault(current, List.of())) {
                if (edge.type() != EdgeType.SUBSTITUTE) continue;
                if (visited.contains(edge.to())) continue;
                visited.add(edge.to());
                if (!excluded.contains(edge.to())) {
                    result.add(edge.to());
                }
                queue.add(edge.to());
            }
        }
        return result;
    }

    // 2) Kahn's algorithm - topological sort of the PROGRESSION subgraph.
    //    Also doubles as cycle detection: if the returned list is shorter than the
    //    number of nodes in the subgraph, a cycle exists.
    //    Time: O(V + E)   Space: O(V)
    public List<Integer> progressionOrder(Set<Integer> subgraphNodes) {
        Map<Integer, Integer> inDegree = new HashMap<>();
        for (int n : subgraphNodes) inDegree.put(n, 0);

        for (int n : subgraphNodes) {
            for (Edge e : adjacency.getOrDefault(n, List.of())) {
                if (e.type() == EdgeType.PROGRESSION && subgraphNodes.contains(e.to())) {
                    inDegree.merge(e.to(), 1, Integer::sum);
                }
            }
        }

        Queue<Integer> ready = new ArrayDeque<>();
        for (var entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) ready.add(entry.getKey());
        }

        List<Integer> order = new ArrayList<>();
        while (!ready.isEmpty()) {
            int current = ready.poll();
            order.add(current);
            for (Edge e : adjacency.getOrDefault(current, List.of())) {
                if (e.type() != EdgeType.PROGRESSION || !subgraphNodes.contains(e.to())) continue;
                int updated = inDegree.merge(e.to(), -1, Integer::sum);
                if (updated == 0) ready.add(e.to());
            }
        }

        if (order.size() != subgraphNodes.size()) {
            throw new IllegalStateException(
                    "Cycle detected in PROGRESSION edges - exercise data is inconsistent: "
                            + (subgraphNodes.size() - order.size()) + " node(s) unreachable in a valid order.");
        }
        return order;
    }

    // 3) Dijkstra - smoothest (lowest total difficulty-jump) path between two exercises,
    //    considering BOTH progression and substitute edges. Valid because every weight
    //    is non-negative by construction.
    //    Time: O((V + E) log V)   Space: O(V)
    public record PathResult(List<Integer> path, int totalCost) {}

    public Optional<PathResult> smoothestPath(int start, int goal) {
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));

        dist.put(start, 0);
        pq.add(new int[]{start, 0});

        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int node = curr[0], d = curr[1];
            if (d > dist.getOrDefault(node, Integer.MAX_VALUE)) continue;
            if (node == goal) break;

            for (Edge e : adjacency.getOrDefault(node, List.of())) {
                int newDist = d + e.weight();
                if (newDist < dist.getOrDefault(e.to(), Integer.MAX_VALUE)) {
                    dist.put(e.to(), newDist);
                    prev.put(e.to(), node);
                    pq.add(new int[]{e.to(), newDist});
                }
            }
        }

        if (!dist.containsKey(goal)) return Optional.empty();

        LinkedList<Integer> path = new LinkedList<>();
        for (Integer at = goal; at != null; at = prev.get(at)) path.addFirst(at);
        return Optional.of(new PathResult(path, dist.get(goal)));
    }
}