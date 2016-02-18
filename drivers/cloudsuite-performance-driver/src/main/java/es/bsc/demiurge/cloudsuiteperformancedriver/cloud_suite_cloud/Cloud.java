package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud;

import com.google.common.base.MoreObjects;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.boot_scripts.BootScriptGenerator;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.schedulers.Scheduler;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.CloudSuiteBenchmark;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.PlacementDecision;

import java.util.List;

public class Cloud {

    private final Scheduler scheduler;
    private final VmmAdapter vmmAdapter;

    public Cloud(Scheduler scheduler, VmmAdapter vmmAdapter) {
        this.scheduler = scheduler;
        this.vmmAdapter = vmmAdapter;
    }

    public List<String> deployBenchmark(CloudSuiteBenchmark benchmark, double perf, Modeller modeller) {
        PlacementDecision placementDecision = scheduler.findBestPlacement(
                benchmark, perf, vmmAdapter.getHostsToBeUsed());
        return vmmAdapter.deployBenchmark(
                benchmark,
                BootScriptGenerator.generateBootScripts(benchmark, placementDecision.getVmSize()),
                placementDecision,
                modeller);
    }

    public void destroyVm(String id) {
        vmmAdapter.destroy(id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("scheduler", scheduler)
                .add("vmmAdapter", vmmAdapter)
                .toString();
    }

}
