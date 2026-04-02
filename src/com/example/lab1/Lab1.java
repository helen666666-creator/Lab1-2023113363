package com.example.lab1;

import java.io.*;
import java.util.*;

public class Lab1 {
    private static Graph graph;
    private static final double D = 0.85;
    private static final double EPS = 1e-8;
    private static final Random random = new Random();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Lab1: 基于大模型的编程与Git实战 ===");
        System.out.print("请输入文本文件路径: ");
        String filePath = scanner.nextLine().trim();
        try {
            graph = buildGraphFromFile(filePath);
            System.out.println("图构建成功！");
        } catch (IOException e) {
            System.err.println("文件读取失败: " + e.getMessage());
            return;
        }

        while (true) {
            System.out.println("\n请选择功能:");
            System.out.println("1. 展示有向图 (CLI)");
            System.out.println("2. 查询桥接词");
            System.out.println("3. 根据桥接词生成新文本");
            System.out.println("4. 计算两个单词的最短路径 / 单源最短路径");
            System.out.println("5. 计算单个单词的PageRank值");
            System.out.println("6. 随机游走");
            System.out.println("7. 保存有向图为图形文件 (PNG)");
            System.out.println("0. 退出");
            System.out.print("输入选项: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    showDirectedGraph(graph);
                    break;
                case "2":
                    System.out.print("请输入第一个单词: ");
                    String w1 = scanner.nextLine().trim().toLowerCase();
                    System.out.print("请输入第二个单词: ");
                    String w2 = scanner.nextLine().trim().toLowerCase();
                    System.out.println(queryBridgeWords(w1, w2));
                    break;
                case "3":
                    System.out.print("请输入一行新文本: ");
                    String text = scanner.nextLine().trim();
                    System.out.println("生成文本: " + generateNewText(text));
                    break;
                case "4":
                    System.out.print("请输入第一个单词: ");
                    String sw1 = scanner.nextLine().trim().toLowerCase();
                    System.out.print("请输入第二个单词 (若只查询一个单词的最短路径，留空直接回车): ");
                    String sw2 = scanner.nextLine().trim().toLowerCase();
                    if (sw2.isEmpty()) {
                        // 单源最短路径
                        calcSingleSourceShortestPaths(sw1);
                    } else {
                        // 两个单词的最短路径：使用 calcShortestPath 并高亮展示
                        String result = calcShortestPath(sw1, sw2);
                        System.out.println(result);
                        // 如果路径存在（不是错误信息），则高亮展示图
                        if (!result.startsWith("No ") && !result.startsWith("No path")) {
                            Graph.ShortestPathResult pathResult = graph.getShortestPath(sw1, sw2);
                            if (pathResult != null) {
                                System.out.println("\nGraph with highlighted path (nodes in brackets, edges with *):");
                                graph.printGraphWithHighlight(pathResult.nodes);
                            }
                        }
                    }
                    break;
                case "5":
                    System.out.print("请输入单词: ");
                    String word = scanner.nextLine().trim().toLowerCase();
                    Double pr = calPageRank(word);
                    if (pr != null) {
                        System.out.printf("PageRank(%.8f) = %.8f\n", D, pr);
                    } else {
                        System.out.println("图中不存在该单词！");
                    }
                    break;
                case "6":
                    String walkResult = randomWalk();
                    System.out.println("随机游走路径: " + walkResult);
                    try (FileWriter fw = new FileWriter("random_walk.txt")) {
                        fw.write(walkResult);
                        System.out.println("路径已保存到 random_walk.txt");
                    } catch (IOException e) {
                        System.err.println("写入文件失败: " + e.getMessage());
                    }
                    break;
                case "7":
                    System.out.print("请输入保存的文件名 (不含扩展名): ");
                    String fileName = scanner.nextLine().trim();
                    saveGraphToImage(fileName);
                    break;
                case "0":
                    System.out.println("程序结束。");
                    return;
                default:
                    System.out.println("无效选项，请重新输入。");
            }
        }
    }

    // ================== 核心功能函数 ==================
    public static void showDirectedGraph(Graph g) {
        g.printGraph();
    }

    public static String queryBridgeWords(String word1, String word2) {
        if (!graph.containsNode(word1) && !graph.containsNode(word2)) {
            return "No \"" + word1 + "\" and \"" + word2 + "\" in the graph!";
        }
        if (!graph.containsNode(word1)) {
            return "No \"" + word1 + "\" in the graph!";
        }
        if (!graph.containsNode(word2)) {
            return "No \"" + word2 + "\" in the graph!";
        }
        List<String> bridges = new ArrayList<>();
        Map<String, Integer> outEdges = graph.getOutEdges(word1);
        for (String mid : outEdges.keySet()) {
            if (graph.getOutEdges(mid).containsKey(word2)) {
                bridges.add(mid);
            }
        }
        if (bridges.isEmpty()) {
            return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";
        } else {
            StringBuilder sb = new StringBuilder("The bridge words from \"" + word1 + "\" to \"" + word2 + "\" ");
            if (bridges.size() == 1) {
                sb.append("is: \"" + bridges.get(0) + "\"");
            } else {
                sb.append("are: ");
                for (int i = 0; i < bridges.size(); i++) {
                    if (i == bridges.size() - 1) {
                        sb.append("and \"" + bridges.get(i) + "\"");
                    } else {
                        sb.append("\"" + bridges.get(i) + "\", ");
                    }
                }
            }
            return sb.toString();
        }
    }

    public static String generateNewText(String inputText) {
        String clean = inputText.replaceAll("[^a-zA-Z]", " ");
        String[] words = clean.split("\\s+");
        List<String> wordList = new ArrayList<>();
        for (String w : words) {
            if (!w.isEmpty()) {
                wordList.add(w.toLowerCase());
            }
        }
        if (wordList.size() <= 1) {
            return inputText;
        }
        StringBuilder result = new StringBuilder();
        result.append(wordList.get(0));
        for (int i = 0; i < wordList.size() - 1; i++) {
            String w1 = wordList.get(i);
            String w2 = wordList.get(i + 1);
            List<String> bridges = new ArrayList<>();
            if (graph.containsNode(w1) && graph.containsNode(w2)) {
                Map<String, Integer> outEdges = graph.getOutEdges(w1);
                for (String mid : outEdges.keySet()) {
                    if (graph.getOutEdges(mid).containsKey(w2)) {
                        bridges.add(mid);
                    }
                }
            }
            if (!bridges.isEmpty()) {
                String chosen = bridges.get(random.nextInt(bridges.size()));
                result.append(" ").append(chosen);
            }
            result.append(" ").append(w2);
        }
        return result.toString();
    }

    /**
     * 公开函数：计算两个单词的最短路径，返回字符串描述
     */
    public static String calcShortestPath(String word1, String word2) {
        Graph.ShortestPathResult result = graph.getShortestPath(word1, word2);
        if (result == null) {
            if (!graph.containsNode(word1) && !graph.containsNode(word2)) {
                return "No \"" + word1 + "\" and \"" + word2 + "\" in the graph!";
            } else if (!graph.containsNode(word1)) {
                return "No \"" + word1 + "\" in the graph!";
            } else if (!graph.containsNode(word2)) {
                return "No \"" + word2 + "\" in the graph!";
            } else {
                return "No path from \"" + word1 + "\" to \"" + word2 + "\"!";
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < result.nodes.size(); i++) {
            if (i > 0) sb.append(" -> ");
            sb.append(result.nodes.get(i));
        }
        sb.append(" (total weight: ").append(result.totalWeight).append(")");
        return sb.toString();
    }

    public static Double calPageRank(String word) {
        if (!graph.containsNode(word)) {
            return null;
        }
        Set<String> nodes = graph.getAllNodes();
        int N = nodes.size();
        Map<String, Double> pr = new HashMap<>();

        // 可选功能：使用词频归一化作为初始PR值
        Map<String, Integer> freq = graph.getAllWordFrequencies();
        double totalFreq = 0.0;
        for (int f : freq.values()) {
            totalFreq += f;
        }
        if (totalFreq > 0) {
            for (String node : nodes) {
                int f = freq.getOrDefault(node, 0);
                pr.put(node, f / totalFreq);
            }
        } else {
            for (String node : nodes) {
                pr.put(node, 1.0 / N);
            }
        }

        while (true) {
            Map<String, Double> newPR = new HashMap<>();
            double sinkSum = 0.0;
            for (String node : nodes) {
                if (graph.getOutDegree(node) == 0) {
                    sinkSum += pr.get(node);
                }
            }
            for (String u : nodes) {
                double sum = 0.0;
                Map<String, Integer> inEdges = graph.getInEdges(u);
                for (Map.Entry<String, Integer> e : inEdges.entrySet()) {
                    String v = e.getKey();
                    int outDeg = graph.getOutDegree(v);
                    sum += pr.get(v) / outDeg;
                }
                sum += sinkSum / N;
                newPR.put(u, (1 - D) / N + D * sum);
            }
            double diff = 0.0;
            for (String node : nodes) {
                diff += Math.abs(newPR.get(node) - pr.get(node));
            }
            pr = newPR;
            if (diff < EPS) break;
        }
        return pr.get(word);
    }

    public static String randomWalk() {
        List<String> nodes = new ArrayList<>(graph.getAllNodes());
        if (nodes.isEmpty()) {
            return "图为空，无法游走";
        }
        String current = nodes.get(random.nextInt(nodes.size()));
        StringBuilder path = new StringBuilder(current);
        Set<String> visitedEdges = new HashSet<>();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            Map<String, Integer> outEdges = graph.getOutEdges(current);
            if (outEdges.isEmpty()) {
                break;
            }
            List<String> targets = new ArrayList<>(outEdges.keySet());
            String next = targets.get(random.nextInt(targets.size()));
            String edge = current + "->" + next;
            if (visitedEdges.contains(edge)) {
                break;
            }
            visitedEdges.add(edge);
            path.append(" -> ").append(next);
            current = next;

            System.out.print("当前路径: " + path + "\n是否继续？(输入 q 停止，其他继续): ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("q")) {
                break;
            }
        }
        return path.toString();
    }

    // ================== 辅助函数 ==================
    private static Graph buildGraphFromFile(String filePath) throws IOException {
        Graph g = new Graph();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String clean = line.replaceAll("[^a-zA-Z]", " ");
                String[] words = clean.split("\\s+");
                List<String> wordList = new ArrayList<>();
                for (String w : words) {
                    if (!w.isEmpty()) {
                        String lower = w.toLowerCase();
                        wordList.add(lower);
                        g.addWordFrequency(lower);
                    }
                }
                for (int i = 0; i < wordList.size() - 1; i++) {
                    String from = wordList.get(i);
                    String to = wordList.get(i + 1);
                    g.addEdge(from, to);
                }
            }
        }
        return g;
    }

    private static void calcSingleSourceShortestPaths(String source) {
        if (!graph.containsNode(source)) {
            System.out.println("No \"" + source + "\" in the graph!");
            return;
        }
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparing(dist::get));

        for (String node : graph.getAllNodes()) {
            dist.put(node, Double.POSITIVE_INFINITY);
        }
        dist.put(source, 0.0);
        pq.offer(source);

        while (!pq.isEmpty()) {
            String u = pq.poll();
            if (dist.get(u) == Double.POSITIVE_INFINITY) continue;
            Map<String, Integer> edges = graph.getOutEdges(u);
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

        System.out.println("Shortest paths from \"" + source + "\":");
        boolean hasPath = false;
        for (String target : graph.getAllNodes()) {
            if (target.equals(source)) continue;
            if (dist.get(target) == Double.POSITIVE_INFINITY) {
                System.out.println("  to " + target + ": unreachable");
            } else {
                hasPath = true;
                LinkedList<String> path = new LinkedList<>();
                String cur = target;
                while (cur != null) {
                    path.addFirst(cur);
                    cur = prev.get(cur);
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < path.size(); i++) {
                    if (i > 0) sb.append(" -> ");
                    sb.append(path.get(i));
                }
                sb.append(" (total weight: ").append(dist.get(target)).append(")");
                System.out.println("  to " + target + ": " + sb);
            }
        }
        if (!hasPath) {
            System.out.println("  该单词无法到达任何其他节点。");
        }
    }

    // ================== 保存图为图形文件 ==================
    private static boolean isGraphvizAvailable() {
        try {
            Process process = Runtime.getRuntime().exec("dot -V");
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static void saveGraphToImage(String filename) {
        if (!isGraphvizAvailable()) {
            System.err.println("未检测到 Graphviz，请先安装 Graphviz 并确保 dot 命令在 PATH 中。");
            System.err.println("下载地址：https://graphviz.org/download/");
            return;
        }

        StringBuilder dot = new StringBuilder();
        dot.append("digraph G {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  node [shape=ellipse, style=filled, fillcolor=lightyellow];\n");
        dot.append("  edge [fontsize=10];\n");

        for (String node : graph.getAllNodes()) {
            dot.append("  \"").append(node).append("\";\n");
        }

        Map<String, Map<String, Integer>> adjMap = graph.getAdjMap();
        for (Map.Entry<String, Map<String, Integer>> fromEntry : adjMap.entrySet()) {
            String from = fromEntry.getKey();
            for (Map.Entry<String, Integer> toEntry : fromEntry.getValue().entrySet()) {
                String to = toEntry.getKey();
                int weight = toEntry.getValue();
                dot.append("  \"").append(from).append("\" -> \"").append(to)
                        .append("\" [label=\"").append(weight).append("\"];\n");
            }
        }

        dot.append("}\n");

        String dotFileName = filename + ".dot";
        try (FileWriter fw = new FileWriter(dotFileName)) {
            fw.write(dot.toString());
            System.out.println("DOT 文件已生成: " + dotFileName);
        } catch (IOException e) {
            System.err.println("写入 DOT 文件失败: " + e.getMessage());
            return;
        }

        String pngFileName = filename + ".png";
        try {
            ProcessBuilder pb = new ProcessBuilder("dot", "-Tpng", dotFileName, "-o", pngFileName);
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("图形文件已保存: " + pngFileName);
            } else {
                System.err.println("调用 dot 命令失败，请检查 Graphviz 安装。");
                try (BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = err.readLine()) != null) {
                        System.err.println(line);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("执行 dot 命令时出错: " + e.getMessage());
        }
    }
}
