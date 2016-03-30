package es.bsc.demiurge.renewit.manager;

import es.bsc.demiurge.cloudsuiteperformancedriver.core.PerformanceDriverCore;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.VmSize;
import es.bsc.demiurge.core.cloudmiddleware.CloudMiddlewareException;
import es.bsc.demiurge.core.configuration.Config;
import es.bsc.demiurge.core.manager.GenericVmManager;
import es.bsc.demiurge.core.manager.components.VmsManager;
import es.bsc.demiurge.core.models.scheduling.RecommendedPlan;
import es.bsc.demiurge.core.models.scheduling.RecommendedPlanRequest;
import es.bsc.demiurge.core.models.scheduling.VmPlacement;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.monitoring.hosts.Host;
import es.bsc.demiurge.renewit.utils.CloudsuiteUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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

        if (Config.INSTANCE.getVmManager().getCurrentSchedulingAlgorithm().equalsIgnoreCase("performanceAware")) {

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
                VmSize vmSize = getVmSizes(vm, host);

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


    private VmSize getVmSizes(Vm vm, Host h){
        return performanceDriverCore.getModeller().getMinVmSizesWithAtLeastPerformance(vm.getExtraParameters().getPerformance(), performanceDriverCore.getModeller().getBenchmarkFromName(vm.getExtraParameters().getBenchmark()), CloudsuiteUtils.convertVMMHostToPerformanceHost(h));

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
                "\n}";
    }

}
