package es.bsc.demiurge.cloudsuiteperformancedriver.workloads;

import com.google.common.base.MoreObjects;

public class BenchmarkExecution {

    private final int timeStartSeconds;
    private final String benchmark;
    private final double performance;
    private final int runtimeSeconds;

    public BenchmarkExecution(int timeStartSeconds, String benchmark, double performance, int runtimeSeconds) {
        this.timeStartSeconds = timeStartSeconds;
        this.benchmark = benchmark;
        this.performance = performance;
        this.runtimeSeconds = runtimeSeconds;
    }

    public int getTimeStartSeconds() {
        return timeStartSeconds;
    }

    public String getBenchmark() {
        return benchmark;
    }

    public double getPerformance() {
        return performance;
    }

    public int getRuntimeSeconds() {
        return runtimeSeconds;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("timeStartSeconds", timeStartSeconds)
                .add("benchmark", benchmark)
                .add("performance", performance)
                .add("runtimeSeconds", runtimeSeconds)
                .toString();
    }

}
