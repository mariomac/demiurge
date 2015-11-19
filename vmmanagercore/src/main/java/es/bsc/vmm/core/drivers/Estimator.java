package es.bsc.vmm.core.drivers;

import es.bsc.vmm.core.models.scheduling.VmAssignmentToHost;
import es.bsc.vmm.core.models.scheduling.DeploymentPlan;
import es.bsc.vmm.core.models.vms.VmDeployed;

import java.util.List;

/**
 * @author Mario Macías (http://github.com/mariomac)
 */
public interface Estimator {
    String getName();
    double getValue(VmAssignmentToHost vma, List<VmDeployed> vmsDeployed, DeploymentPlan deploymentPlan);

}
