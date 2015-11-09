package es.bsc.clurge;

import es.bsc.clurge.domain.VirtualMachine;
import es.bsc.clurge.exception.CloudMiddlewareException;
import es.bsc.clurge.sched.*;
import es.bsc.clurge.sched.opts.SelfAdaptationOptions;

import java.util.List;

/**
 * Created by mmacias on 5/11/15.
 */
public interface SchedulingManager {
	//================================================================================
	// Scheduling Algorithms Methods
	//================================================================================

	/**
	 * Returns the scheduling algorithms that can be applied.
	 *
	 * @return the list of scheduling algorithms
	 */
	List<SchedulingAlgorithm> getAvailableSchedulingAlgorithms();

	/**
	 * Returns the scheduling algorithm that is being used now.
	 *
	 * @return the scheduling algorithm being used
	 */
	SchedulingAlgorithm getCurrentSchedulingAlgorithm();

	/**
	 * Changes the scheduling algorithm.
	 *
	 * @param schedulingAlg the scheduling algorithm to be used
	 */
	void setSchedulingAlgorithm(SchedulingAlgorithm schedulingAlg);


	//================================================================================
	// VM Placement
	//================================================================================

	/**
	 * Returns a list of the construction heuristics supported by the VM Manager.
	 *
	 * @return the list of construction heuristics
	 */
	List<ConstructionHeuristic> getConstructionHeuristics();

	/**
	 * Returns a list of the local search algorithms supported by the VM Manager.
	 *
	 * @return the list of local search algorithms
	 */
	List<LocalSearchAlgorithm> getLocalSearchAlgorithms();

	/**
	 * This function calculates a deployment plan based on a request. It uses the VM placement library.
	 *
	 * @param recommendedPlanRequest the request
	 * @param assignVmsToCurrentHosts indicates whether the hosts should be set in the VM instances
	 * @param vmsToDeploy list of VMs that need to be deployed
	 * @return the recommended plan
	 */
	RecommendedPlan getRecommendedPlan(RecommendedPlan recommendedPlanRequest,
									   boolean assignVmsToCurrentHosts,
									   List<VirtualMachine> vmsToDeploy) throws CloudMiddlewareException;

	/**
	 * This function executes a deployment plan. This means that each of the VMs of the deployment plan are migrated
	 * to the host specified if they were not already deployed there.
	 *
	 * @param deploymentPlan the deployment plan
	 */
	void executeDeploymentPlan(VirtualMachinePlacement[] deploymentPlan) throws CloudMiddlewareException;

	void executeOnDemandSelfAdaptation() throws CloudMiddlewareException;

	SelfAdaptationOptions getSelfAdaptationOptions();
	void saveSelfAdaptationOptions(SelfAdaptationOptions selfAdaptationOptions);

}
