package es.bsc.demiurge.cloudsuiteperformancedriver.models;

public enum CloudSuiteBenchmark {

    DATA_ANALYTICS(new VmSize(1, 4, 100), new VmSize(32, 28, 150), PerformanceValue.DESCENDANT_PERFORMANCE),
    DATA_CACHING(new VmSize(1, 2, 10), new VmSize(32, 24, 50), PerformanceValue.ASCENDANT_PERFORMANCE),
    DATA_SERVING(new VmSize(1, 2, 10), new VmSize(32, 24, 50), PerformanceValue.ASCENDANT_PERFORMANCE),
    GRAPH_ANALYTICS(new VmSize(1, 8, 10), new VmSize(32, 20, 50), PerformanceValue.DESCENDANT_PERFORMANCE),
    MEDIA_STREAMING(new VmSize(1, 1, 10), new VmSize(32, 24, 50), PerformanceValue.ASCENDANT_PERFORMANCE),
    SOFTWARE_TESTING(new VmSize(1, 2, 10), new VmSize(32, 24, 50), PerformanceValue.ASCENDANT_PERFORMANCE),
    WEB_SEARCH(new VmSize(1, 1, 10), new VmSize(32, 30, 50), PerformanceValue.ASCENDANT_PERFORMANCE),
    WEB_SERVING(new VmSize(1, 1, 30), new VmSize(16, 12, 50), PerformanceValue.ASCENDANT_PERFORMANCE);

    private final VmSize minimumVmSize;
    private final VmSize maximumVmSize;
    private final PerformanceValue performanceValue;


    CloudSuiteBenchmark(VmSize minimumVmSize, VmSize maximumVmSize, PerformanceValue performanceValue) {
        this.minimumVmSize = minimumVmSize;
        this.maximumVmSize = maximumVmSize;
        this.performanceValue = performanceValue;

    }

    public VmSize getMinimumVmSize() {
        return minimumVmSize;
    }

    public VmSize getMaximumVmSize() {
        return maximumVmSize;
    }

    public PerformanceValue getPerformanceValue() {
        return performanceValue;
    }


}
