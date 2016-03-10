package edu.put.ma.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExecutorUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorUtils.class);

    private ExecutorUtils() {
        // hidden constructor
    }

    public static final ExecutorService prepareExecutorService(final ExecutorService executor,
            final int threadsCount) {
        closeExecutor(executor);
        if (threadsCount > 1) {
            return Executors.newFixedThreadPool(threadsCount);
        }
        return Executors.newSingleThreadExecutor();
    }

    public static final void closeExecutor(final ExecutorService executor) {
        if (executor != null) {
            executor.shutdown();
            while (!executor.isTerminated()) {
                LOGGER.debug("Executor is properly terminated");
            }
        }
    }
}
