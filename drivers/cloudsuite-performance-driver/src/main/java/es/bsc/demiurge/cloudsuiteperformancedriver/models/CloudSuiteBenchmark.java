package es.bsc.demiurge.cloudsuiteperformancedriver.models;

public enum CloudSuiteBenchmark {

    DATA_ANALYTICS(new VmSize(1, 4, 100), new VmSize(32, 28, 150), PerformanceValue.DESCENDANT_PERFORMANCE, "data_analytics"),
    DATA_CACHING(new VmSize(1, 2, 10), new VmSize(32, 24, 50), PerformanceValue.ASCENDANT_PERFORMANCE, "data_caching"),
    DATA_SERVING(new VmSize(1, 2, 10), new VmSize(32, 24, 50), PerformanceValue.ASCENDANT_PERFORMANCE, "data_serving"),
    GRAPH_ANALYTICS(new VmSize(1, 8, 10), new VmSize(32, 20, 50), PerformanceValue.DESCENDANT_PERFORMANCE, "graph_analytics"),
    MEDIA_STREAMING(new VmSize(1, 1, 10), new VmSize(32, 24, 50), PerformanceValue.ASCENDANT_PERFORMANCE, "media_streaming"),
    SOFTWARE_TESTING(new VmSize(1, 2, 10), new VmSize(32, 24, 50), PerformanceValue.ASCENDANT_PERFORMANCE, "software_testing"),
    WEB_SEARCH(new VmSize(1, 1, 10), new VmSize(32, 30, 50), PerformanceValue.ASCENDANT_PERFORMANCE, "web_search"),
    WEB_SERVING(new VmSize(1, 1, 30), new VmSize(16, 12, 50), PerformanceValue.ASCENDANT_PERFORMANCE, "web_serving");

    private final VmSize minimumVmSize;
    private final VmSize maximumVmSize;
    private final PerformanceValue performanceValue;
    private final String name;

    CloudSuiteBenchmark(VmSize minimumVmSize, VmSize maximumVmSize, PerformanceValue performanceValue, String name) {
        this.minimumVmSize = minimumVmSize;
        this.maximumVmSize = maximumVmSize;
        this.performanceValue = performanceValue;
        this.name = name;
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

    public String getName() { return name; }
}
