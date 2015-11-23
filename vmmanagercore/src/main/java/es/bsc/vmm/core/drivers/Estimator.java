package es.bsc.vmm.core.drivers;

import es.bsc.vmm.core.models.scheduling.VmAssignmentToHost;
import es.bsc.vmm.core.models.scheduling.DeploymentPlan;
import es.bsc.vmm.core.models.vms.VmDeployed;

import java.util.List;
import java.util.Map;

/**
 * @author Mario Macías (http://github.com/mariomac)
 */
public interface Estimator {
    String getLabel();
    double getDeploymentEstimation(VmAssignmentToHost vma, List<VmDeployed> vmsDeployed, DeploymentPlan deploymentPlan);
	double getCurrentEstimation(String vmId, Map options); // for pricing modeler, options wil include the "false" value for undeployed
}
