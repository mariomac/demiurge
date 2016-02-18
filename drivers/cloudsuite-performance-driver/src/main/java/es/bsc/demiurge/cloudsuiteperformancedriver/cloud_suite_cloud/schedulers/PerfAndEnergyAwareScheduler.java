package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.schedulers;

import com.google.common.base.MoreObjects;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.Modeller;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.CloudSuiteBenchmark;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.Host;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.PlacementDecision;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.VmSize;

import java.util.ArrayList;
import java.util.List;

public class PerfAndEnergyAwareScheduler implements Scheduler {

    private final Modeller modeller;

    public PerfAndEnergyAwareScheduler(Modeller modeller) {
        this.modeller = modeller;
    }

    public PlacementDecision findBestPlacement(CloudSuiteBenchmark benchmark, double perf, List<Host> hosts) {
        double bestAvgPower = Double.MAX_VALUE;
        Host bestHost = null;
        VmSize bestVmSize = null;

        for (Host host : hosts) {
            List<VmSize> vmSizesThatGiveEnoughPerformance =
                    modeller.getVmSizesWithAtLeastPerformance(perf, benchmark, host);
            List<VmSize> possibleVmSizes =
                    filterVmSizesAccordingToAvailableSpace(vmSizesThatGiveEnoughPerformance, host);

            for (VmSize vmSize : possibleVmSizes) {
                double expectedAvgPower = modeller.getBenchmarkAvgPower(benchmark, host.getHostname(), vmSize);
                if (expectedAvgPower < bestAvgPower) {
                    bestAvgPower = expectedAvgPower;
                    bestHost = host;
                    bestVmSize = vmSize;
                }
            }
        }

        if (bestHost != null) {
            return new PlacementDecision(bestHost, bestVmSize);
        }

        throw new RuntimeException("Could not find a placement that satisfies the performance requirements of the VM");
    }

    private List<VmSize> filterVmSizesAccordingToAvailableSpace(List<VmSize> vmSizes, Host host) {
        List<VmSize> result = new ArrayList<>();
        for (VmSize vmSize : vmSizes) {
            if (host.hasEnoughSpaceToHost(vmSize)) {
                result.add(vmSize);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("modeller", modeller)
                .toString();
    }

}
