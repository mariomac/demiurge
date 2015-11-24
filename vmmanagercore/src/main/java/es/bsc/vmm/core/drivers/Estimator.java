package es.bsc.vmm.core.drivers;

import es.bsc.vmm.core.clopla.domain.Host;
import es.bsc.vmm.core.clopla.domain.Vm;
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

	/**
	 * for deployment estimations
	 * @param vma
	 * @param vmsDeployed
	 * @param deploymentPlan
	 * @return
	 */
    double getDeploymentEstimation(VmAssignmentToHost vma, List<VmDeployed> vmsDeployed, DeploymentPlan deploymentPlan);

	/**
	 * for current estimations
	 * @param vmId
	 * @param options
	 * @return
	 */
	double getCurrentEstimation(String vmId, Map options); // for pricing modeler, options wil include the "false" value for undeployed

	/**
	 * For clopla estimations. *CAUTION* the parameters are not VMM-specific host and vm classes but Clopla ones.
	 * @param host
	 * @param vmsDeployedInHost
	 * @return
	 */
	double getCloplaEstimation(Host host, List<Vm> vmsDeployedInHost);
}
