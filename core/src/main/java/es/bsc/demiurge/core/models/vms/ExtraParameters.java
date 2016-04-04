package es.bsc.demiurge.core.models.vms;

import es.bsc.demiurge.cloudsuiteperformancedriver.models.CloudSuiteBenchmark;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class ExtraParameters {

    private CloudSuiteBenchmark benchmark;
    private double performance;

    public ExtraParameters(CloudSuiteBenchmark benchmark, double performance) {
        this.benchmark = benchmark;
        this.performance = performance;
    }

    /**
     * @return The benchmark
     */
    public CloudSuiteBenchmark getBenchmark() {
        return benchmark;
    }

    /**
     * @param benchmark The benchmark
     */
    public void setBenchmark(CloudSuiteBenchmark benchmark) {
        this.benchmark = benchmark;
    }

    /**
     * @return The performance
     */
    public double getPerformance() {
        return performance;
    }

    /**
     * @param performance The performance
     */
    public void setPerformance(Integer performance) {
        this.performance = performance;
    }
}
