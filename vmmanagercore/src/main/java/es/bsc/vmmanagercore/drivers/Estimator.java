package es.bsc.vmmanagercore.drivers;

import es.bsc.vmmanagercore.models.scheduling.DeploymentPlan;
import es.bsc.vmmanagercore.models.scheduling.VmAssignmentToHost;
import es.bsc.vmmanagercore.models.vms.VmDeployed;

import java.util.List;

/**
 * @author Mario Macías (http://github.com/mariomac)
 */
public interface Estimator {
    String getName();
    double getValue(VmAssignmentToHost vma, List<VmDeployed> vmsDeployed, DeploymentPlan deploymentPlan);

}
