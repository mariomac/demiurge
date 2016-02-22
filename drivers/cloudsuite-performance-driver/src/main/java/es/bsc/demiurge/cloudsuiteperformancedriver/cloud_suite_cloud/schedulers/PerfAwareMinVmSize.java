package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.schedulers;

import com.google.common.base.MoreObjects;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.Modeller;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.CloudSuiteBenchmark;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.Host;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.PlacementDecision;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.VmSize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerfAwareMinVmSize implements Scheduler {

    private final Modeller modeller;

    public PerfAwareMinVmSize(Modeller modeller) {
        this.modeller = modeller;
    }

    public PlacementDecision findBestPlacement(CloudSuiteBenchmark benchmark, double perf, List<Host> hosts) {
        Map<Host, VmSize> minSizePerHost = new HashMap<>();
        for (Host host : hosts) {
            List<VmSize> vmSizesThatGiveEnoughPerformance =
                    modeller.getVmSizesWithAtLeastPerformance(perf, benchmark, host);
            List<VmSize> possibleVmSizes =
                    filterVmSizesAccordingToAvailableSpace(vmSizesThatGiveEnoughPerformance, host);
            minSizePerHost.put(host, getMinVmSize(possibleVmSizes));
        }
        return getMinVmSize(minSizePerHost);
    }

    private VmSize getMinVmSize(List<VmSize> vmSizes) {
        if (vmSizes.size() == 0) {
            return null;
        }

        int minCpus = vmSizes.get(0).getCpus();
        int minRamGb = vmSizes.get(0).getRamGb();
        int minDiskGb = vmSizes.get(0).getDiskGb();
        for (VmSize vmSize : vmSizes) {
            if (vmSize.getCpus() < minCpus) {
                minCpus = vmSize.getCpus();
            }
            if (vmSize.getRamGb() < minRamGb) {
                minRamGb = vmSize.getRamGb();
            }
            if (vmSize.getDiskGb() < minDiskGb) {
                minDiskGb = vmSize.getDiskGb();
            }
        }

        VmSize result = null;
        double bestVmSizeScore = Double.MAX_VALUE;
        for (VmSize vmSize : vmSizes) {
            double currentVmSizeScore = ((double)vmSize.getCpus()/minCpus
                    + (double)vmSize.getRamGb()/minRamGb
                    + (double)vmSize.getDiskGb()/minDiskGb)/3.0;
            if (currentVmSizeScore < bestVmSizeScore) {
                bestVmSizeScore = currentVmSizeScore;
                result = vmSize;
            }
        }
        return result;
    }


    private PlacementDecision getMinVmSize(Map<Host, VmSize> vmSizes) {
        if (vmSizes.size() == 0) {
            return null;
        }

        int minCpus = Integer.MAX_VALUE;
        int minRamGb = Integer.MAX_VALUE;
        int minDiskGb = Integer.MAX_VALUE;

        for (Map.Entry<Host, VmSize> vmSize : vmSizes.entrySet()) {
            if (vmSize.getValue().getCpus() < minCpus) {
                minCpus = vmSize.getValue().getCpus();
            }
            if (vmSize.getValue().getRamGb() < minRamGb) {
                minRamGb = vmSize.getValue().getRamGb();
            }
            if (vmSize.getValue().getDiskGb() < minDiskGb) {
                minDiskGb = vmSize.getValue().getDiskGb();
            }
        }

        PlacementDecision result = null;
        double bestVmSizeScore = Double.MAX_VALUE;
        for (Map.Entry<Host, VmSize> vmSize : vmSizes.entrySet()) {
            double currentVmSizeScore = ((double)vmSize.getValue().getCpus()/minCpus
                    + (double)vmSize.getValue().getRamGb()/minRamGb
                    + (double)vmSize.getValue().getDiskGb()/minDiskGb)/3.0;
            if (currentVmSizeScore < bestVmSizeScore) {
                bestVmSizeScore = currentVmSizeScore;
                result = new PlacementDecision(vmSize.getKey(), vmSize.getValue());
            }
        }
        return result;
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
