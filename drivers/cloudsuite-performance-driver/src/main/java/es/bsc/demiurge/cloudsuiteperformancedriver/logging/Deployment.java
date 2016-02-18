package es.bsc.demiurge.cloudsuiteperformancedriver.logging;

import es.bsc.demiurge.cloudsuiteperformancedriver.models.VmSize;

public class Deployment {

    private final long timestamp;
    private final String benchmark;
    private final String hostname;
    private final VmSize vmSize;
    private final double expectedPerf;
    private final double expectedPower;

    public Deployment(long timestamp, String benchmark, String hostname, VmSize vmSize,
                      double expectedPerf, double expectedPower) {
        this.timestamp = timestamp;
        this.benchmark = benchmark;
        this.hostname = hostname;
        this.vmSize = vmSize;
        this.expectedPerf = expectedPerf;
        this.expectedPower = expectedPower;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getBenchmark() {
        return benchmark;
    }

    public String getHostname() {
        return hostname;
    }

    public VmSize getVmSize() {
        return vmSize;
    }

    public double getExpectedPerf() {
        return expectedPerf;
    }

    public double getExpectedPower() {
        return expectedPower;
    }

}
