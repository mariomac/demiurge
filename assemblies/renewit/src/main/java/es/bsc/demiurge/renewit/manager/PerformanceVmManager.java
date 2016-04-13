package es.bsc.demiurge.renewit.manager;

import es.bsc.demiurge.cloudsuiteperformancedriver.core.PerformanceDriverCore;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.CloudSuiteBenchmark;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.VmSize;
import es.bsc.demiurge.core.cloudmiddleware.CloudMiddlewareException;
import es.bsc.demiurge.core.configuration.Config;
import es.bsc.demiurge.core.manager.GenericVmManager;
import es.bsc.demiurge.core.manager.components.VmsManager;
import es.bsc.demiurge.core.models.scheduling.RecommendedPlan;
import es.bsc.demiurge.core.models.scheduling.RecommendedPlanRequest;
import es.bsc.demiurge.core.models.scheduling.VmPlacement;
import es.bsc.demiurge.core.models.vms.ListVmsDeployed;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.models.vms.VmDeployed;
import es.bsc.demiurge.core.monitoring.hosts.Host;
import es.bsc.demiurge.renewit.utils.CloudsuiteUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class PerformanceVmManager extends GenericVmManager {
    private Logger logger = LogManager.getLogger(PerformanceVmManager.class);
    private PerformanceDriverCore performanceDriverCore = new PerformanceDriverCore();


    public PerformanceVmManager() {
        super();

    }


    public PerformanceDriverCore getPerformanceDriverCore() {
        return performanceDriverCore;
    }


    /**
     * This function calculates a deployment plan based on a request. It uses the VM placement library.
     *
     * @param recommendedPlanRequest the request
     * @param assignVmsToCurrentHosts indicates whether the hosts should be set in the VM instances
     * @param vmsToDeploy list of VMs that need to be deployed
     * @return the recommended plan
     */
    @Override
    public RecommendedPlan getRecommendedPlan(RecommendedPlanRequest recommendedPlanRequest,
                                              boolean assignVmsToCurrentHosts,
                                              List<Vm> vmsToDeploy) throws CloudMiddlewareException {

        if (Config.INSTANCE.getVmManager().getCurrentSchedulingAlgorithm().startsWith(Config.INSTANCE.PERF_POWER_ALGORITHM_PREFIX)) {

            RecommendedPlan recommendedPlan = super.vmPlacementManager.getRecommendedPlanDiscardHostNoPerformance(super.getDB().getCurrentSchedulingAlg(), recommendedPlanRequest, assignVmsToCurrentHosts, vmsToDeploy, performanceDriverCore);

            /*logger.info("Recommended plan: ");
            for (Map.Entry entry : recommendedPlan.getPlan().entrySet()) {
                logger.info(entry.getKey() + " -> " + entry.getValue());
            }*/
            // Convert from clopla VM to VMM VM
            // When using performance models, CPU, Ram and disk are not set. They must be set depending on the host chosen by optaplanner.

            VmsManager vmsManager = super.getVmsManager();

            for (Vm vm : vmsToDeploy){
                VmPlacement vmPlacement = vmsManager.findVmPlacementByVmId(
                        recommendedPlan.getVMPlacements(), vm.getName());

                Host host = super.getHostsManager().getHost(vmPlacement.getHostname());
                VmSize vmSize = getVmSizesVMM(vm, host);

                vm.setCpus(vmSize.getCpus());
                vm.setRamMb(vmSize.getRamGb()*1024);
                vm.setDiskGb(vmSize.getDiskGb());

                logger.info(VmPlacementToString(vm, host));

            }

            return recommendedPlan;

        }else{
            return super.vmPlacementManager.getRecommendedPlan(super.getDB().getCurrentSchedulingAlg(),recommendedPlanRequest, assignVmsToCurrentHosts, vmsToDeploy);
        }

    }


    public VmSize getVmSizesVMM(Vm vm, Host h){
        return performanceDriverCore.getModeller().getMinVmSizesWithAtLeastPerformance(vm.getExtraParameters().getPerformance(), vm.getExtraParameters().getBenchmark(), CloudsuiteUtils.convertVMMHostToPerformanceHost(h));

    }
    public VmSize getVmSizesClopla(es.bsc.demiurge.core.clopla.domain.Vm vm, es.bsc.demiurge.core.clopla.domain.Host h){
        return performanceDriverCore.getModeller().getMinVmSizesWithAtLeastPerformance(vm.getExtraParameters().getPerformance(), vm.getExtraParameters().getBenchmark(), CloudsuiteUtils.convertClusterHostToPerformanceHost(h));

    }

    @Override
    public double getClusterConsumption() {
        List<Host> hosts = this.getHosts();
        ListVmsDeployed vms = new ListVmsDeployed(this.getAllVms());
        double pow = 0;

        HashMap <String, Integer> hmap = new HashMap<>();
        for (Host h : hosts){
            hmap.put(h.getHostname(), 0);
            if (h.isOn()) {
                pow += performanceDriverCore.getModeller().getIdlePowerHost(h.getType());
            }

        }
        for (VmDeployed vm : vms.getVms()){

            VmSize vmSize = new VmSize(vm.getCpus(), vm.getRamMb()*1024, vm.getDiskGb());
            String hostName = vm.getHostName();
            Host host = getHost(hostName);
            CloudSuiteBenchmark benchmark = vm.getExtraParameters().getBenchmark();

            double vmPowerEstimation = performanceDriverCore.getModeller().getBenchmarkAvgPower(benchmark, host.getType(), vmSize);
            pow += vmPowerEstimation - host.getIdlePower();

            if (hmap.get(hostName) == 0){
                pow += 5;
                hmap.put(hostName, 1);
            }

        }

        return pow;

    }


    private String VmPlacementToString(Vm vm, Host host){
        return "Vm:{ " +
                "\n\tname: " + vm.getName() +
                "\n\timageId: " + vm.getImage() +
                "\n\tCPUs: " + vm.getCpus() +
                "\n\tRAM: " + vm.getRamMb() +
                "\n\tDisk: " + vm.getDiskGb() +
                "\n\tHost: " + host.getHostname() +
                "\n\tBenchmark: " + vm.getExtraParameters().getBenchmark() +
                "\n\tPerformance: " + vm.getExtraParameters().getPerformance() +
                "\n\tRunning Time: " + vm.getExtraParameters().getRunningTime() +
                "\n}";
    }

}
