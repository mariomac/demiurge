package es.bsc.clurge.estimates;

import es.bsc.clurge.models.scheduling.DeploymentPlan;
import es.bsc.clurge.models.vms.Vm;
import es.bsc.clurge.models.vms.VmDeployed;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mmacias on 4/11/15.
 */
public class DeploymentPlanEstimation {
	private List<VmDeployed> vmsDeployed;
	private DeploymentPlan deploymentPlan;

	Map<Class<? extends Estimator>, Estimator> estimators = new HashMap<>();

	public DeploymentPlanEstimation(List<VmDeployed> vmsDeployed, DeploymentPlan deploymentPlan) {
		this.vmsDeployed = vmsDeployed;
		this.deploymentPlan = deploymentPlan;
	}

	public <V> V getEstimateValue(Class<? extends Estimator<V>> clazz) {
		Estimator e = estimators.get(clazz);
		return (V) e.getDeploymentPlanEstimation(vmsDeployed, deploymentPlan);
	}
}
