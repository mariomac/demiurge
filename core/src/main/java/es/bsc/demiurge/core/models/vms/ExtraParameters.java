package es.bsc.demiurge.core.models.vms;

import es.bsc.demiurge.cloudsuiteperformancedriver.models.CloudSuiteBenchmark;

import static es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.Modeller.getBenchmarkFromName;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class ExtraParameters {


    private String benchmark;
    private double performance;
    private int runningTime;

    public ExtraParameters(String benchmark, double performance) {
        this.benchmark = benchmark;
        this.performance = performance;
        this.runningTime = 0;
    }

    public ExtraParameters(String benchmark, double performance, int runningTime) {
        this.benchmark = benchmark;
        this.performance = performance;
        this.runningTime = runningTime;
    }

    /**
     * @return The benchmark
     */
    public String getBenchmarkStr() { return benchmark; }

    /**
     * @return The benchmark
     */
    public CloudSuiteBenchmark getBenchmark() {

        return getBenchmarkFromName(this.benchmark);


    }

    public int getRunningTime() {
        return runningTime;
    }

    /**
     * @param benchmark The benchmark
     */
    public void setBenchmark(String benchmark) {
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

    public void setRunningTime(int runningTime) {
        this.runningTime = runningTime;
    }
}
