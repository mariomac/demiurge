package es.bsc.clurge.estimates;

import es.bsc.clurge.sched.DeploymentPlan;
import es.bsc.clurge.models.vms.Vm;
import es.bsc.clurge.models.vms.VmDeployed;
import es.bsc.clurge.monit.Host;

import java.util.List;

/**
 * Created by mmacias on 5/11/15.
 */
public interface Estimator<ReturnType> {
	ReturnType getDeploymentPlanEstimation(List<VmDeployed> vmsDeployed, DeploymentPlan deploymentPlan);
	ReturnType getVmEstimation(Vm vm, Host host, List<VmDeployed> vmsDeployed, DeploymentPlan deploymentPlan);
}
