package edu.put.ma.descs.contacts;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import edu.put.ma.utils.ExecutorUtils;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.operator.Operator;
import net.objecthunter.exp4j.function.Function;
import net.objecthunter.exp4j.ExpressionBuilder;

public class ExpressionEvaluatorImpl implements ExpressionEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionEvaluatorImpl.class);

    private final AtomicInteger counter;

    private String expressionString;

    private Set<String> variableNames;

    @Getter
    private int threadsCount;

    private ExecutorService executor;

    private List<Expression> expressions;

    public ExpressionEvaluatorImpl(final String expressionString, final Set<String> variableNames,
            final int threadsCount) {
        this.expressionString = expressionString;
        this.variableNames = variableNames;
        this.threadsCount = threadsCount;
        this.counter = new AtomicInteger(0);
        prepareExecutorService(threadsCount);
        prepareExpressionsList(expressionString, variableNames, threadsCount);
    }

    @Override
    public double evaluate(final Map<String, Double> variableValues) {
        double result = Double.NaN;
        final Expression expression = setAndReturnExpression(variableValues);
        final Future<Double> future = expression.evaluateAsync(executor);
        try {
            result = future.get();
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public boolean evaluateAndTransform(Map<String, Double> variableValues) {
        return Double.compare(evaluate(variableValues), 1d) == 0;
    }

    @Override
    public void setThreadsCount(final int threadsCount) {
        this.threadsCount = threadsCount;
        prepareExecutorService(threadsCount);
        prepareExpressionsList(threadsCount);
    }

    @Override
    public void setExpression(final String expressionString, final Set<String> variableNames) {
        prepareExpressionsList(expressionString, variableNames, threadsCount);
    }

    @Override
    public void close() {
        ExecutorUtils.closeExecutor(executor);
    }

    private void prepareExecutorService(final int threadsCount) {
        executor = ExecutorUtils.prepareExecutorService(executor, threadsCount);
    }

    private void prepareExpressionsList(final int threadsCount) {
        final int currentSize = CollectionUtils.size(expressions);
        if (currentSize > threadsCount) {
            for (int i = currentSize - 1; i >= threadsCount; i--) {
                expressions.remove(i);
            }
        } else {
            appendExpressions(expressionString, variableNames, threadsCount - currentSize);
        }
    }

    private void prepareExpressionsList(final String expressionString, final Set<String> variableNames,
            final int threadsCount) {
        expressions = edu.put.ma.utils.CollectionUtils.prepareList(expressions);
        appendExpressions(expressionString, variableNames, threadsCount);
    }

    private void appendExpressions(final String expressionString, final Set<String> variableNames,
            final int count) {
        for (int i = 0; i < count; i++) {
            expressions.add(getExpression(expressionString, variableNames));
        }
    }

    private Expression setAndReturnExpression(final Map<String, Double> variableValues) {
        final int expressionIndex = counter.incrementAndGet() % threadsCount;
        final Expression expression = expressions.get(expressionIndex);
        for (Map.Entry<String, Double> entry : variableValues.entrySet()) {
            expression.setVariable(entry.getKey(), entry.getValue());
        }
        return expression;
    }

    private static boolean isArrayInitialized(final int expectedSize, final double... values) {
        return (values != null) && (ArrayUtils.getLength(values) == expectedSize);
    }

    private static final Expression getExpression(final String expressionString,
            final Set<String> variableNames) {
        return new ExpressionBuilder(expressionString).operator(Operators.SET.asList())
                .functions(Functions.SET.asList()).variables(variableNames).build();
    }

    private static final class Operators {

        private static final Operator GREATER_OR_EQUAL_OPERATOR = new Operator(">=", 2, true,
                Operator.PRECEDENCE_ADDITION - 1) {
            @Override
            public double apply(final double... values) {
                if (isArrayInitialized(numOperands, values) && (values[0] >= values[1])) {
                    return 1d;
                }
                return 0d;
            }
        };

        private static final Operator GREATER_OPERATOR = new Operator(">", 2, true,
                Operator.PRECEDENCE_ADDITION - 1) {
            @Override
            public double apply(final double... values) {
                if (isArrayInitialized(numOperands, values) && (values[0] > values[1])) {
                    return 1d;
                }
                return 0d;
            }
        };

        private static final Operator LESS_OPERATOR = new Operator("<", 2, true,
                Operator.PRECEDENCE_ADDITION - 1) {
            @Override
            public double apply(final double... values) {
                if (isArrayInitialized(numOperands, values) && (values[0] < values[1])) {
                    return 1d;
                }
                return 0d;
            }
        };

        private static final Operator LESS_OR_EQUAL_OPERATOR = new Operator("<=", 2, true,
                Operator.PRECEDENCE_ADDITION - 1) {
            @Override
            public double apply(final double... values) {
                if (isArrayInitialized(numOperands, values) && (values[0] <= values[1])) {
                    return 1d;
                }
                return 0d;
            }
        };

        private static final Operator EQUAL_OPERATOR = new Operator("=", 2, true,
                Operator.PRECEDENCE_ADDITION - 1) {
            @Override
            public double apply(final double... values) {
                if (isArrayInitialized(numOperands, values) && (Double.compare(values[0], values[1]) == 0)) {
                    return 1d;
                }
                return 0d;
            }
        };

        private static final ImmutableSet<Operator> SET = ImmutableSet.of(EQUAL_OPERATOR, GREATER_OPERATOR,
                LESS_OPERATOR, GREATER_OR_EQUAL_OPERATOR, LESS_OR_EQUAL_OPERATOR);

        private Operators() {
            // hidden constructor
        }
    }

    private static final class Functions {

        private static final Function AND_FUNCTION = new Function("AND", 2) {
            @Override
            public double apply(final double... values) {
                if (isArrayInitialized(numArguments, values)) {
                    boolean isTrue = true;
                    for (int i = 0; i < numArguments; i++) {
                        isTrue = isTrue && (Double.compare(values[i], 0d) != 0);
                    }
                    return isTrue ? 1d : 0d;
                }
                return 0d;
            }
        };

        private static final Function OR_FUNCTION = new Function("OR", 2) {
            @Override
            public double apply(final double... values) {
                if (isArrayInitialized(numArguments, values)) {
                    for (int i = 0; i < numArguments; i++) {
                        if (Double.compare(values[i], 0d) != 0) {
                            return 1d;
                        }
                    }
                }
                return 0d;
            }
        };

        private static final Function NOT_FUNCTION = new Function("NOT", 1) {
            @Override
            public double apply(final double... values) {
                if (isArrayInitialized(numArguments, values)) {
                    return (Double.compare(values[0], 0d) != 0) ? 0d : 1d;
                }
                return 0d;
            }
        };

        private static final ImmutableSet<Function> SET = ImmutableSet.of(AND_FUNCTION, OR_FUNCTION,
                NOT_FUNCTION);

        private Functions() {
            // hidden constructor
        }
    }
}
