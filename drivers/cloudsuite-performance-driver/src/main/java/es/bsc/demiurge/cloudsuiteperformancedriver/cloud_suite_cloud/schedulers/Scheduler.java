package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.schedulers;

import es.bsc.demiurge.cloudsuiteperformancedriver.models.CloudSuiteBenchmark;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.Host;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.PlacementDecision;

import java.util.List;

public interface Scheduler {

    PlacementDecision findBestPlacement(CloudSuiteBenchmark benchmark, double perf, List<Host> hosts);

}
