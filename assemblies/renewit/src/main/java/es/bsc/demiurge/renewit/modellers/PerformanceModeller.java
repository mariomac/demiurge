package es.bsc.demiurge.renewit.modellers;

import es.bsc.demiurge.core.clopla.domain.Host;
import es.bsc.demiurge.core.clopla.domain.Vm;
import es.bsc.demiurge.core.cloudmiddleware.CloudMiddlewareException;
import es.bsc.demiurge.core.drivers.Estimator;
import es.bsc.demiurge.core.models.scheduling.DeploymentPlan;
import es.bsc.demiurge.core.models.scheduling.VmAssignmentToHost;
import es.bsc.demiurge.core.models.vms.VmDeployed;

import java.util.List;
import java.util.Map;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class PerformanceModeller  implements Estimator {
    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public double getDeploymentEstimation(VmAssignmentToHost vma, List<VmDeployed> vmsDeployed, DeploymentPlan deploymentPlan) {
        return 0;
    }

    @Override
    public double getCurrentEstimation(String vmId, Map options) throws CloudMiddlewareException {
        return 0;
    }

    /**
     *
     * This method return the power consumption of the host. Actual consumption can be calculate in 2 ways:
     *  - Directly reading the power meter of the host and summing the non-deployed-VMs power estimation.
     *  - If the host does not have a power meter summing the power estimation of all the VMs
     *
     * @param host
     * @param vmsDeployedInHost
     * @return
     */

    @Override
    public double getCloplaEstimation(Host host, List<Vm> vmsDeployedInHost){return 0;}


}
