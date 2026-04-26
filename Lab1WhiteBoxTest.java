package com.example.lab1;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class Lab1WhiteBoxTest {

    @Before
    public void setUp() throws Exception {
        setRandomSeed(0L);
        injectInput("");
    }

    private void injectGraph(Graph graph) throws Exception {
        Field graphField = Lab1.class.getDeclaredField("graph");
        graphField.setAccessible(true);
        graphField.set(null, graph);
    }

    private void injectInput(String input) throws Exception {
        Field inputField = Lab1.class.getDeclaredField("INPUT");
        inputField.setAccessible(true);
        removeFinalModifier(inputField);
        inputField.set(null, new Scanner(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8.name()));
    }

    private void removeFinalModifier(Field field) throws Exception {
        if (!Modifier.isFinal(field.getModifiers())) {
            return;
        }
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }

    private void setRandomSeed(long seed) throws Exception {
        Field randomField = Lab1.class.getDeclaredField("RANDOM");
        randomField.setAccessible(true);
        Random random = (Random) randomField.get(null);
        random.setSeed(seed);
    }

    private Graph fixedGraph(String startNode, String... edges) {
        FixedGraph graph = new FixedGraph(startNode);
        for (String edge : edges) {
            String[] parts = edge.split("->");
            graph.addFixedEdge(parts[0], parts[1]);
        }
        return graph;
    }

    private void assertRandomWalk(String caseName, Graph graph, String input, String expected)
            throws Exception {
        injectGraph(graph);
        injectInput(input);

        String actual = Lab1.randomWalk();
        System.out.println("[" + caseName + "]");
        System.out.println("input: " + input.replace("\n", "\\n"));
        System.out.println("expected: " + expected);
        System.out.println("actual:   " + actual);
        assertEquals(expected, actual);
    }

    @Test
    public void testPath01RandomWalkEmptyGraph() throws Exception {
        assertRandomWalk(
                "Path 1: nodes.isEmpty is true",
                new FixedGraph(),
                "",
                "The graph is empty. Random walk is unavailable.");
    }

    @Test
    public void testPath02RandomWalkStopsWhenStartNodeHasNoOutEdges() throws Exception {
        assertRandomWalk(
                "Path 2: outEdges.isEmpty is true",
                fixedGraph("a"),
                "",
                "a");
    }

    @Test
    public void testPath03RandomWalkStopsWhenUserInputsQ() throws Exception {
        assertRandomWalk(
                "Path 3: user enters q",
                fixedGraph("a", "a->b"),
                "q\n",
                "a -> b");
    }

    @Test
    public void testPath04RandomWalkContinuesThenStopsAtSinkNode() throws Exception {
        assertRandomWalk(
                "Path 4: user continues and next node has no out edges",
                fixedGraph("a", "a->b"),
                "\n",
                "a -> b");
    }

    @Test
    public void testPath05RandomWalkStopsWhenEdgeRepeats() throws Exception {
        assertRandomWalk(
                "Path 5: visitedEdges.contains is true",
                fixedGraph("a", "a->a"),
                "\n",
                "a -> a");
    }

    @Test
    public void testPath06RandomWalkContinuesThenStopsWhenUserInputsQLater() throws Exception {
        assertRandomWalk(
                "Path 6: loop executes more than once and user later enters q",
                fixedGraph("a", "a->b", "b->c"),
                "\nq\n",
                "a -> b -> c");
    }

    private static final class FixedGraph extends Graph {
        private final Set<String> nodes = new LinkedHashSet<>();
        private final Map<String, Map<String, Integer>> outEdges = new LinkedHashMap<>();

        FixedGraph(String... nodeNames) {
            Collections.addAll(nodes, nodeNames);
        }

        void addFixedEdge(String from, String to) {
            outEdges.putIfAbsent(from, new LinkedHashMap<>());
            outEdges.get(from).put(to, 1);
        }

        @Override
        public Set<String> getAllNodes() {
            return Collections.unmodifiableSet(nodes);
        }

        @Override
        public Map<String, Integer> getOutEdges(String from) {
            Map<String, Integer> edges = outEdges.get(from);
            if (edges == null) {
                return Collections.emptyMap();
            }
            return Collections.unmodifiableMap(edges);
        }
    }
}
