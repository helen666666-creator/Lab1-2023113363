package com.example.lab1;

import java.util.*;

public class Graph {
    private Map<String, Map<String, Integer>> adjMap;
    private Set<String> nodes;
    private Map<String, Integer> wordFreq;          // 单词出现频率

    public Graph() {
        adjMap = new HashMap<>();
        nodes = new HashSet<>();
        wordFreq = new HashMap<>();
    }

    public void addEdge(String from, String to) {
        from = from.toLowerCase();
        to = to.toLowerCase();
        nodes.add(from);
        nodes.add(to);
        adjMap.putIfAbsent(from, new HashMap<>());
        Map<String, Integer> edges = adjMap.get(from);
        edges.put(to, edges.getOrDefault(to, 0) + 1);
    }

    public void addWordFrequency(String word) {
        word = word.toLowerCase();
        wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
    }

    public int getWordFrequency(String word) {
        return wordFreq.getOrDefault(word.toLowerCase(), 0);
    }

    public Map<String, Integer> getAllWordFrequencies() {
        return wordFreq;
    }

    public Map<String, Integer> getOutEdges(String from) {
        return adjMap.getOrDefault(from.toLowerCase(), Collections.emptyMap());
    }

    public Map<String, Integer> getInEdges(String to) {
        to = to.toLowerCase();
        Map<String, Integer> inEdges = new HashMap<>();
        for (String from : adjMap.keySet()) {
            Map<String, Integer> edges = adjMap.get(from);
            if (edges.containsKey(to)) {
                inEdges.put(from, edges.get(to));
            }
        }
        return inEdges;
    }

    public Set<String> getAllNodes() {
        return nodes;
    }

    public boolean containsNode(String node) {
        return nodes.contains(node.toLowerCase());
    }

    public int getOutDegree(String node) {
        return adjMap.getOrDefault(node.toLowerCase(), Collections.emptyMap()).size();
    }

    public Set<String> getSinkNodes() {
        Set<String> sinks = new HashSet<>();
        for (String node : nodes) {
            if (getOutDegree(node) == 0) {
                sinks.add(node);
            }
        }
        return sinks;
    }

    public Map<String, Map<String, Integer>> getAdjMap() {
        return adjMap;
    }

    public void printGraph() {
        System.out.println("Directed Graph (node -> {target:weight}):");
        for (String from : adjMap.keySet()) {
            System.out.print(from + " -> ");
            Map<String, Integer> edges = adjMap.get(from);
            if (edges.isEmpty()) {
                System.out.println("{}");
            } else {
                System.out.print("{");
                List<String> items = new ArrayList<>();
                for (Map.Entry<String, Integer> e : edges.entrySet()) {
                    items.add(e.getKey() + ":" + e.getValue());
                }
                System.out.println(String.join(", ", items) + "}");
            }
        }
        for (String node : nodes) {
            if (!adjMap.containsKey(node)) {
                System.out.println(node + " -> {}");
            }
        }
    }

    public void printGraphWithHighlight(List<String> pathNodes) {
        System.out.println("Directed Graph with highlighted path (node -> {target:weight}) :");
        Set<String> pathNodeSet = new HashSet<>(pathNodes);
        Set<String> pathEdges = new HashSet<>();
        for (int i = 0; i < pathNodes.size() - 1; i++) {
            String from = pathNodes.get(i);
            String to = pathNodes.get(i + 1);
            pathEdges.add(from + "->" + to);
        }

        for (String from : adjMap.keySet()) {
            String nodeDisplay = pathNodeSet.contains(from) ? "[" + from + "]" : from;
            System.out.print(nodeDisplay + " -> ");
            Map<String, Integer> edges = adjMap.get(from);
            if (edges.isEmpty()) {
                System.out.println("{}");
            } else {
                System.out.print("{");
                List<String> items = new ArrayList<>();
                for (Map.Entry<String, Integer> e : edges.entrySet()) {
                    String to = e.getKey();
                    int weight = e.getValue();
                    String edgeKey = from + "->" + to;
                    String edgeDisplay;
                    if (pathEdges.contains(edgeKey)) {
                        edgeDisplay = "*" + to + ":" + weight + "*";
                    } else {
                        edgeDisplay = to + ":" + weight;
                    }
                    items.add(edgeDisplay);
                }
                System.out.println(String.join(", ", items) + "}");
            }
        }
        for (String node : nodes) {
            if (!adjMap.containsKey(node)) {
                String nodeDisplay = pathNodeSet.contains(node) ? "[" + node + "]" : node;
                System.out.println(nodeDisplay + " -> {}");
            }
        }
    }

    public ShortestPathResult getShortestPath(String word1, String word2) {
        if (!containsNode(word1) || !containsNode(word2)) {
            return null;
        }
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparing(dist::get));

        for (String node : nodes) {
            dist.put(node, Double.POSITIVE_INFINITY);
        }
        dist.put(word1, 0.0);
        pq.offer(word1);

        while (!pq.isEmpty()) {
            String u = pq.poll();
            if (u.equals(word2)) break;
            if (dist.get(u) == Double.POSITIVE_INFINITY) continue;
            Map<String, Integer> edges = getOutEdges(u);
            for (Map.Entry<String, Integer> e : edges.entrySet()) {
                String v = e.getKey();
                double weight = e.getValue();
                double newDist = dist.get(u) + weight;
                if (newDist < dist.get(v)) {
                    dist.put(v, newDist);
                    prev.put(v, u);
                    pq.offer(v);
                }
            }
        }

        if (dist.get(word2) == Double.POSITIVE_INFINITY) {
            return null;
        }

        LinkedList<String> path = new LinkedList<>();
        String cur = word2;
        while (cur != null) {
            path.addFirst(cur);
            cur = prev.get(cur);
        }
        return new ShortestPathResult(path, dist.get(word2));
    }

    public static class ShortestPathResult {
        public List<String> nodes;
        public double totalWeight;

        public ShortestPathResult(List<String> nodes, double totalWeight) {
            this.nodes = nodes;
            this.totalWeight = totalWeight;
        }
    }
}