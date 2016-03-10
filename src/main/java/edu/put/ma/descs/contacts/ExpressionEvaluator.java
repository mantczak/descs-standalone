package edu.put.ma.descs.contacts;

import java.util.Map;
import java.util.Set;

public interface ExpressionEvaluator {

    double evaluate(Map<String, Double> variableValues);

    boolean evaluateAndTransform(Map<String, Double> variableValues);

    int getThreadsCount();

    void setThreadsCount(int threadsCount);

    void setExpression(String expressionString, Set<String> variableNames);

    void close();
}
