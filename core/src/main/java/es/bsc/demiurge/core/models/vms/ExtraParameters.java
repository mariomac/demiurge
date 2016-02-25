package es.bsc.demiurge.core.models.vms;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class ExtraParameters {

    private String benchmark;
    private double performance;

    public ExtraParameters(String benchmark, double performance) {
        this.benchmark = benchmark;
        this.performance = performance;
    }

    /**
     * @return The benchmark
     */
    public String getBenchmark() {
        return benchmark;
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
}
