package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.boot_scripts;

import es.bsc.demiurge.cloudsuiteperformancedriver.models.CloudSuiteBenchmark;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.VmSize;

import java.util.Map;

public class BootScriptGenerator {

    // Some benchmarks use more than 1 VM. That is why we return a map :
    // vm (frontend, backend, etc.) => script
    public static Map<String, String> generateBootScripts(CloudSuiteBenchmark benchmark, VmSize vmSize) {
        switch(benchmark) {
            case DATA_ANALYTICS:
                return DataAnalyticsScriptGenerator.generateScripts(vmSize);
            case DATA_CACHING:
                return DataCachingScriptGenerator.generateScripts(vmSize);
            case DATA_SERVING:
                return DataServingScriptGenerator.generateScripts(vmSize);
            case GRAPH_ANALYTICS:
                return GraphAnalyticsScriptGenerator.generateScripts(vmSize);
            case MEDIA_STREAMING:
                return MediaStreamingScriptGenerator.generateScripts(vmSize);
            case SOFTWARE_TESTING:
                return SoftwareTestingScriptGenerator.generateScripts(vmSize);
            case WEB_SEARCH:
                return WebSearchJmeterScriptGenerator.generateScripts(vmSize);
            case WEB_SERVING:
                return WebServingScriptGenerator.generateScripts(vmSize);
        }
        throw new RuntimeException("Asked for bootscript of non-existing benchmark.");
    }

}
