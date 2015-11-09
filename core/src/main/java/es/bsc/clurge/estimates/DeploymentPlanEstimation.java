package es.bsc.clurge.estimates;

import es.bsc.clurge.Clurge;
import es.bsc.clurge.domain.VirtualMachine;
import es.bsc.clurge.sched.DeploymentPlan;
import es.bsc.clurge.models.vms.Vm;
import es.bsc.clurge.models.vms.VmDeployed;

import java.util.List;

/**
 * Created by mmacias on 4/11/15.
 */
public class DeploymentPlanEstimation {
	private List<VirtualMachine> vmsDeployed;
	private DeploymentPlan deploymentPlan;

	public DeploymentPlanEstimation(List<VirtualMachine> vmsDeployed, DeploymentPlan deploymentPlan) {
		this.vmsDeployed = vmsDeployed;
		this.deploymentPlan = deploymentPlan;
	}

	public <V> V getEstimateValue(Class<? extends Estimator<V>> clazz) {
		Estimator e = Clurge.INSTANCE.getEstimator(clazz);
		return (V) e.getDeploymentPlanEstimation(vmsDeployed, deploymentPlan);
	}
}
