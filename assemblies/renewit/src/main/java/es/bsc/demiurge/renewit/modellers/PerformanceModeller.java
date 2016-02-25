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

    @Override
    public double getCloplaEstimation(Host host, List<Vm> vmsDeployedInHost) {
        return 0;
    }
}
