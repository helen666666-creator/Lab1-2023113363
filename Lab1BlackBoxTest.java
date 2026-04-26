package com.example.lab1;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

public class Lab1BlackBoxTest {

    @Before
    public void setUp() throws Exception {
        injectGraph(buildGraph(
                "a->b",
                "b->c",
                "a->d",
                "d->e",
                "x->y"));
    }

    private Graph buildGraph(String... edges) {
        Graph graph = new Graph();
        for (String edge : edges) {
            String[] parts = edge.split("->");
            graph.addEdge(parts[0], parts[1]);
        }
        return graph;
    }

    private void injectGraph(Graph graph) throws Exception {
        Field graphField = Lab1.class.getDeclaredField("graph");
        graphField.setAccessible(true);
        graphField.set(null, graph);
    }

    private void assertBridgeWordsResult(
            String caseName, String word1, String word2, String expected) {
        String actual = Lab1.queryBridgeWords(word1, word2);
        System.out.println("[" + caseName + "]");
        System.out.println("input: (" + word1 + ", " + word2 + ")");
        System.out.println("expected: " + expected);
        System.out.println("actual:   " + actual);
        assertEquals(expected, actual);
    }

    @Test
    public void testQueryBridgeWordsWithOneBridgeWord() {
        String expected = "The bridge words from \"a\" to \"c\" is: \"b\"";
        assertBridgeWordsResult("one bridge word", "a", "c", expected);
    }

    @Test
    public void testQueryBridgeWordsWithMultipleBridgeWords() throws Exception {
        injectGraph(buildGraph(
                "a->b",
                "b->d",
                "a->c",
                "c->d"));

        String expected = "The bridge words from \"a\" to \"d\" are: \"b\" and \"c\"";
        assertBridgeWordsResult("multiple bridge words", "a", "d", expected);
    }

    @Test
    public void testQueryBridgeWordsWithNoBridgeWord() {
        String expected = "No bridge words from \"b\" to \"a\"!";
        assertBridgeWordsResult("no bridge word", "b", "a", expected);
    }

    @Test
    public void testQueryBridgeWordsWhenWord1NotInGraph() {
        String expected = "No \"missing\" in the graph!";
        assertBridgeWordsResult("word1 missing", "missing", "c", expected);
    }

    @Test
    public void testQueryBridgeWordsWhenWord2NotInGraph() {
        String expected = "No \"missing\" in the graph!";
        assertBridgeWordsResult("word2 missing", "a", "missing", expected);
    }

    @Test
    public void testQueryBridgeWordsWhenBothWordsNotInGraph() {
        String expected = "No \"foo\" and \"bar\" in the graph!";
        assertBridgeWordsResult("both words missing", "foo", "bar", expected);
    }
}
