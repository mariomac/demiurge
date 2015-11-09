package es.bsc.clurge.vmm.generic;

import es.bsc.clopla.domain.ConstructionHeuristic;
import es.bsc.clurge.Clurge;
import es.bsc.clurge.SchedulingManager;
import es.bsc.clurge.domain.VirtualMachine;
import es.bsc.clurge.exception.CloudMiddlewareException;
import es.bsc.clurge.sched.*;
import es.bsc.clurge.sched.opts.AfterVmDeleteSelfAdaptationOps;
import es.bsc.clurge.sched.opts.AfterVmDeploymentSelfAdaptationOps;
import es.bsc.clurge.sched.opts.PeriodicSelfAdaptationOps;
import es.bsc.clurge.sched.opts.SelfAdaptationOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mmacias on 9/11/15.
 */
public class GenericSchedulingManager implements SchedulingManager {

	private Logger logger = LogManager.getLogger(GenericSchedulingManager.class);

	private SchedulingAlgorithm currentSchedulingAlgorithm;

	/**
	 * This function updates the configuration options in the DB.
	 *
	 * @param selfAdaptationOptions the options
	 */
	@Override
	public void saveSelfAdaptationOptions(SelfAdaptationOptions selfAdaptationOptions) {
		Clurge.INSTANCE.getPersistenceManager().saveSelfAdaptationOptions(selfAdaptationOptions);
	}

	/**
	 * Returns the self-adaptation options for the self-adaptation capabilities of the VMM.
	 * If the system does not have any self-adaptation options save, it returns a set of options that have
	 * been defined as the default ones.
	 *
	 * @return the options
	 */
	@Override
	public SelfAdaptationOptions getSelfAdaptationOptions() {
		if (Clurge.INSTANCE.getPersistenceManager().getSelfAdaptationOptions() == null) {
			return getDefaultSelfAdaptationOptions();
		}
		return Clurge.INSTANCE.getPersistenceManager().getSelfAdaptationOptions();
	}

	/**
	 * Returns a recommended plan for deployment according to the self-adaptation options defined.
	 *
	 * @return the recommended plan
	 */
	@Override
	public RecommendedPlan getRecommendedPlanForDeployment(List<VirtualMachine> vmsToDeploy) throws CloudMiddlewareException {
		AfterVmDeploymentSelfAdaptationOps options = getSelfAdaptationOptions().getAfterVmDeploymentSelfAdaptationOps();
		String constrHeuristicName = options.getConstructionHeuristic().getName();

		// Prepare the request to get a recommended deployment plan
		RecommendedPlanRequest recommendedPlanRequest = new RecommendedPlanRequest(
				options.getMaxExecTimeSeconds(),
				constrHeuristicName,
				null);

		return Clurge.INSTANCE.getSchedulingManager().getRecommendedPlan(recommendedPlanRequest, true, vmsToDeploy);
	}

	/**
	 * Applies the self-adaptation configured to take place after a deployment request.
	 */
	@Override
	public void applyAfterVmsDeploymentSelfAdaptation() {
		logger.info("Executing after vm deployment self-adaptation");
		AfterVmDeploymentSelfAdaptationOps options = getSelfAdaptationOptions().getAfterVmDeploymentSelfAdaptationOps();

		// Decide local search algorithm
		LocalSearchAlgorithmOptionsSet localSearchAlg = null;
		if (options.getMaxExecTimeSeconds() > 0) {
			localSearchAlg = options.getLocalSearchAlgorithm();
		}

		// Prepare the request to get a recommended deployment plan
		RecommendedPlanRequest recommendedPlanRequest = new RecommendedPlanRequest(
				options.getMaxExecTimeSeconds(),
				null,
				localSearchAlg);

		if (localSearchAlg != null) {
			try {
				executeDeploymentPlan(
						getRecommendedPlan(recommendedPlanRequest, true, new ArrayList<VirtualMachine>()).getVMPlacements());
			} catch (CloudMiddlewareException e) {
				logger.error(e.getMessage(),e);
			}
		}
	}

	/**
	 * Applies the self-adaptation configured to take place after deleting a VM.
	 */
	@Override
	public void applyAfterVmDeleteSelfAdaptation() {
		logger.info("Executing Self-adaptation after VM deletion");
		AfterVmDeleteSelfAdaptationOps options = getSelfAdaptationOptions().getAfterVmDeleteSelfAdaptationOps();

		if (options.getLocalSearchAlgorithm() != null && options.getMaxExecTimeSeconds() > 0) {
			RecommendedPlanRequest recommendedPlanRequest = new RecommendedPlanRequest(
					options.getMaxExecTimeSeconds(), null, options.getLocalSearchAlgorithm());

			try {
				executeDeploymentPlan(getRecommendedPlan(recommendedPlanRequest, true, new ArrayList<VirtualMachine>()).getVMPlacements());
			} catch (CloudMiddlewareException e) {
				logger.error(e.getMessage(),e);
			}
		}
	}

	@Override
	public void applyOnDemandSelfAdaptation() throws CloudMiddlewareException {
		AfterVmDeploymentSelfAdaptationOps ops = getSelfAdaptationOptions().getAfterVmDeploymentSelfAdaptationOps();
		RecommendedPlanRequest recommendedPlanRequest = new RecommendedPlanRequest(
				ops.getMaxExecTimeSeconds(),ops.getConstructionHeuristic().getName(),ops.getLocalSearchAlgorithm());

		VmPlacement[] deploymentPlan = getRecommendedPlan(recommendedPlanRequest,
				true,
				new ArrayList<VirtualMachine>()
		).getVMPlacements();
		executeDeploymentPlan(deploymentPlan);
	}

	/**
	 * Applies the self-adaptation configured to take place periodically.
	 */
	@Override
	public void applyPeriodicSelfAdaptation() {
		logger.info("Executing periodic self-adaptation");
		try {
			PeriodicSelfAdaptationOps options = getSelfAdaptationOptions().getPeriodicSelfAdaptationOps();

			if (options.getLocalSearchAlgorithm() != null && options.getMaxExecTimeSeconds() > 0) {
				// The construction heuristic is set to first fit, but anyone could be selected because in this case,
				// all the VMs are already assigned to a host. Therefore, it is not needed to apply a construction heuristic
				RecommendedPlanRequest recommendedPlanRequest = new RecommendedPlanRequest(
						options.getMaxExecTimeSeconds(), null, options.getLocalSearchAlgorithm());

				executeDeploymentPlan(getRecommendedPlan(recommendedPlanRequest, true, new ArrayList<VirtualMachine>()).getVMPlacements());
			}
		} catch(CloudMiddlewareException ex) {
			logger.error(ex.getMessage(),ex);
		}
	}

	/**
	 * Returns the default self-adaptation options.
	 *
	 * @return the default self-adaptation options
	 */
	private SelfAdaptationOptions getDefaultSelfAdaptationOptions() {
		return new SelfAdaptationOptions(
				new AfterVmDeploymentSelfAdaptationOps(new ConstructionHeuristic("FIRST_FIT"), null, 0),
				new AfterVmDeleteSelfAdaptationOps(null, 0),
				new PeriodicSelfAdaptationOps(null, 0, 0));
	}




}
