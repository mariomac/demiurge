package es.bsc.clurge.sched;

import es.bsc.clurge.domain.PhysicalHost;

import java.util.List;

/**
 * Created by mmacias on 5/11/15.
 */
public interface SchedulingAlgorithm {
	String getName();

	/**
	 * Given a list of deployment plans, chooses the best according to the scheduling algorithm.
	 *
	 * @param deploymentPlans the list of deployment plans
	 * @param hosts the list of hosts
	 * @param deploymentId ID used to identify the deployment in the log messages
	 * @return the best deployment plan
	 */
	DeploymentPlan chooseBestDeploymentPlan(List<DeploymentPlan> deploymentPlans, List<PhysicalHost> hosts,
											String deploymentId);
}
