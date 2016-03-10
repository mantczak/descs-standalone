package edu.put.ma.descs.contacts;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class ExpressionEvaluatorTest {

    private ExpressionEvaluator expressionEvaluator;

    @Test
    public void testBasicLogicalOperations1() throws Exception {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("X<Y", ImmutableSet.of("X", "Y"), 1);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 1d, "Y", 2d);
        assertTrue(expressionEvaluator.evaluateAndTransform(variableValues));
    }

    @Test
    public void testBasicLogicalOperations2() throws Exception {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("X<Y", ImmutableSet.of("X", "Y"), 1);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 2d, "Y", 1.5);
        assertFalse(expressionEvaluator.evaluateAndTransform(variableValues));
    }

    @Test
    public void testBasicLogicalOperations3() throws Exception {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("X>Y", ImmutableSet.of("X", "Y"), 1);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 1d, "Y", 2d);
        assertFalse(expressionEvaluator.evaluateAndTransform(variableValues));
    }

    @Test
    public void testBasicLogicalOperations4() throws Exception {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("X>Y", ImmutableSet.of("X", "Y"), 1);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 2d, "Y", 1.5);
        assertTrue(expressionEvaluator.evaluateAndTransform(variableValues));
    }

    @Test
    public void testBasicLogicalOperations5() throws Exception {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("X>=Y", ImmutableSet.of("X", "Y"), 1);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 3d, "Y", 2d);
        assertTrue(expressionEvaluator.evaluateAndTransform(variableValues));
    }

    @Test
    public void testBasicLogicalOperations6() throws Exception {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("X>=Y", ImmutableSet.of("X", "Y"), 1);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 3d, "Y", 3d);
        assertTrue(expressionEvaluator.evaluateAndTransform(variableValues));
    }

    @Test
    public void testBasicLogicalOperations7() throws Exception {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("X>=Y", ImmutableSet.of("X", "Y"), 1);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 3d, "Y", 4d);
        assertFalse(expressionEvaluator.evaluateAndTransform(variableValues));
    }

    @Test
    public void testBasicLogicalOperations8() throws Exception {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("X<=Y", ImmutableSet.of("X", "Y"), 1);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 2d, "Y", 3d);
        assertTrue(expressionEvaluator.evaluateAndTransform(variableValues));
    }

    @Test
    public void testBasicLogicalOperations9() throws Exception {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("X<=Y", ImmutableSet.of("X", "Y"), 1);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 3d, "Y", 3d);
        assertTrue(expressionEvaluator.evaluateAndTransform(variableValues));
    }

    @Test
    public void testBasicLogicalOperations10() throws Exception {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("X<=Y", ImmutableSet.of("X", "Y"), 1);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 4d, "Y", 3d);
        assertFalse(expressionEvaluator.evaluateAndTransform(variableValues));
    }

    @Test
    public void testBasicLogicalOperations11() throws Exception {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("X=Y", ImmutableSet.of("X", "Y"), 1);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 4d, "Y", 3d);
        assertFalse(expressionEvaluator.evaluateAndTransform(variableValues));
    }

    @Test
    public void testBasicLogicalOperations12() throws Exception {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("X=Y", ImmutableSet.of("X", "Y"), 1);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 3d, "Y", 4d);
        assertFalse(expressionEvaluator.evaluateAndTransform(variableValues));
    }

    @Test
    public void testBasicLogicalOperations13() throws Exception {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("X=Y", ImmutableSet.of("X", "Y"), 1);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 1.2, "Y", 1.2);
        assertTrue(expressionEvaluator.evaluateAndTransform(variableValues));
    }

    @Test
    public void testComplexLogicalOperations14() throws Exception {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("NOT(X=Y)", ImmutableSet.of("X", "Y"), 1);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 1.2, "Y", 1.2);
        assertFalse(expressionEvaluator.evaluateAndTransform(variableValues));
    }

    @Test
    public void testComplexLogicalOperations15() throws Exception {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("NOT(X=Y)", ImmutableSet.of("X", "Y"), 1);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 1d, "Y", 2d);
        assertTrue(expressionEvaluator.evaluateAndTransform(variableValues));
    }

    @Test
    public void testComplexLogicalOperations16() throws Exception {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("OR(X=Y, X=Y)", ImmutableSet.of("X", "Y"), 1);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 1d, "Y", 2d);
        assertFalse(expressionEvaluator.evaluateAndTransform(variableValues));
    }

    @Test
    public void testComplexLogicalOperations17() throws Exception {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("OR(X=Y, X=Y)", ImmutableSet.of("X", "Y"), 1);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 1.2, "Y", 1.2);
        assertTrue(expressionEvaluator.evaluateAndTransform(variableValues));
    }

    @Test
    public void testComplexLogicalOperations18() throws Exception {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("OR(X=Y, NOT(X=Y))", ImmutableSet.of("X", "Y"), 1);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 1.2, "Y", 1.2);
        assertTrue(expressionEvaluator.evaluateAndTransform(variableValues));
    }

    @Test
    public void testComplexLogicalOperations19() throws Exception {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("OR(NOT(X=Y), X=Y)", ImmutableSet.of("X", "Y"), 1);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 1.2, "Y", 1.2);
        assertTrue(expressionEvaluator.evaluateAndTransform(variableValues));
    }

    @Test
    public void testComplexLogicalOperations20() throws Exception {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("AND(X=Y, X=Y)", ImmutableSet.of("X", "Y"), 1);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 1d, "Y", 2d);
        assertFalse(expressionEvaluator.evaluateAndTransform(variableValues));
    }

    @Test
    public void testComplexLogicalOperations21() throws Exception {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("AND(NOT(X=Y), X=Y)", ImmutableSet.of("X", "Y"), 1);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 1d, "Y", 2d);
        assertFalse(expressionEvaluator.evaluateAndTransform(variableValues));
    }

    @Test
    public void testComplexLogicalOperations22() throws Exception {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("AND(X=Y, NOT(X=Y))", ImmutableSet.of("X", "Y"), 1);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 1d, "Y", 2d);
        assertFalse(expressionEvaluator.evaluateAndTransform(variableValues));
    }

    @Test
    public void testComplexLogicalOperations23() throws Exception {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("AND(NOT(X=Y), NOT(X=Y))", ImmutableSet.of("X", "Y"), 1);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 1d, "Y", 2d);
        assertTrue(expressionEvaluator.evaluateAndTransform(variableValues));
    }

    @Test
    public void testMultithreadedExecution() {
        this.expressionEvaluator = new ExpressionEvaluatorImpl("X<Y", ImmutableSet.of("X", "Y"), 2);
        final ImmutableMap<String, Double> variableValues = ImmutableMap.of("X", 1d, "Y", 2d);
        final ImmutableMap<String, Double> variableValues2 = ImmutableMap.of("X", 2d, "Y", 1d);
        final ImmutableMap<String, Double> variableValues3 = ImmutableMap.of("X", 3d, "Y", 4d);
        final ImmutableMap<String, Double> variableValues4 = ImmutableMap.of("X", 4d, "Y", 3d);
        assertTrue(expressionEvaluator.evaluateAndTransform(variableValues));
        assertFalse(expressionEvaluator.evaluateAndTransform(variableValues2));
        assertTrue(expressionEvaluator.evaluateAndTransform(variableValues3));
        assertFalse(expressionEvaluator.evaluateAndTransform(variableValues4));
    }

    @After
    public void tearDown() throws Exception {
        if (expressionEvaluator != null) {
            expressionEvaluator.close();
        }
    }
}
