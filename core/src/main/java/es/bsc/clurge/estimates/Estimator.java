package es.bsc.clurge.estimates;

import es.bsc.clurge.domain.PhysicalHost;
import es.bsc.clurge.domain.VirtualMachine;
import es.bsc.clurge.sched.DeploymentPlan;

import java.util.List;

/**
 * Created by mmacias on 5/11/15.
 */
public interface Estimator<ReturnType> {
	ReturnType getDeploymentPlanEstimation(List<VirtualMachine> vmsDeployed, DeploymentPlan deploymentPlan);
	ReturnType getVmEstimation(VirtualMachine vm, PhysicalHost host, List<VirtualMachine> vmsDeployed, DeploymentPlan deploymentPlan);
}
