package es.bsc.demiurge.renewit.manager;

import es.bsc.demiurge.cloudsuiteperformancedriver.core.PerformanceDriverCore;
import es.bsc.demiurge.core.cloudmiddleware.CloudMiddlewareException;
import es.bsc.demiurge.core.manager.GenericVmManager;
import es.bsc.demiurge.core.models.scheduling.RecommendedPlan;
import es.bsc.demiurge.core.models.scheduling.RecommendedPlanRequest;
import es.bsc.demiurge.core.models.vms.Vm;

import java.util.List;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class PerformanceVmManager extends GenericVmManager {

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

        return super.vmPlacementManager.getRecommendedPlanWithHostIdle(super.getDB().getCurrentSchedulingAlg(),recommendedPlanRequest, assignVmsToCurrentHosts, vmsToDeploy, performanceDriverCore);
    }



}
