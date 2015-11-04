/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.clurge.sched;

import es.bsc.clurge.Clurge;
import es.bsc.clurge.cloudmw.CloudMiddlewareException;
import es.bsc.clurge.models.scheduling.*;
import es.bsc.clurge.models.vms.Vm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Self-adaptation Manager.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class SelfAdaptationManager {

	private Logger logger = LogManager.getLogger(SelfAdaptationManager.class);

    /**
     * This function updates the configuration options in the DB.
     *
     * @param selfAdaptationOptions the options
     */
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
    public RecommendedPlan getRecommendedPlanForDeployment(List<Vm> vmsToDeploy) throws CloudMiddlewareException {
        AfterVmDeploymentSelfAdaptationOps options = getSelfAdaptationOptions().getAfterVmDeploymentSelfAdaptationOps();
        String constrHeuristicName = options.getConstructionHeuristic().getName();

        // Prepare the request to get a recommended deployment plan
        RecommendedPlanRequest recommendedPlanRequest = new RecommendedPlanRequest(
                options.getMaxExecTimeSeconds(),
                constrHeuristicName,
                null);

        return Clurge.INSTANCE.getVmManager().getRecommendedPlan(recommendedPlanRequest, true, vmsToDeploy);
    }

    /**
     * Applies the self-adaptation configured to take place after a deployment request.
     */
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
				Clurge.INSTANCE.getVmManager().executeDeploymentPlan(
						Clurge.INSTANCE.getVmManager().getRecommendedPlan(recommendedPlanRequest, true, new ArrayList<Vm>()).getVMPlacements());
			} catch (CloudMiddlewareException e) {
				logger.error(e.getMessage(),e);
			}
		}
    }

    /**
     * Applies the self-adaptation configured to take place after deleting a VM.
     */
    public void applyAfterVmDeleteSelfAdaptation() {
        logger.info("Executing Self-adaptation after VM deletion");
        AfterVmDeleteSelfAdaptationOps options = getSelfAdaptationOptions().getAfterVmDeleteSelfAdaptationOps();

        if (options.getLocalSearchAlgorithm() != null && options.getMaxExecTimeSeconds() > 0) {
            RecommendedPlanRequest recommendedPlanRequest = new RecommendedPlanRequest(
                    options.getMaxExecTimeSeconds(), null, options.getLocalSearchAlgorithm());

			try {
				Clurge.INSTANCE.getVmManager().executeDeploymentPlan(
						Clurge.INSTANCE.getVmManager().getRecommendedPlan(recommendedPlanRequest, true, new ArrayList<Vm>()).getVMPlacements());
			} catch (CloudMiddlewareException e) {
				logger.error(e.getMessage(),e);
			}
		}
    }

	public void applyOnDemandSelfAdaptation() throws CloudMiddlewareException {
		AfterVmDeploymentSelfAdaptationOps ops = getSelfAdaptationOptions().getAfterVmDeploymentSelfAdaptationOps();
		RecommendedPlanRequest recommendedPlanRequest = new RecommendedPlanRequest(
				ops.getMaxExecTimeSeconds(),ops.getConstructionHeuristic().getName(),ops.getLocalSearchAlgorithm());

		VmPlacement[] deploymentPlan = Clurge.INSTANCE.getVmManager().getRecommendedPlan(recommendedPlanRequest,
				true,
				new ArrayList<Vm>()
			).getVMPlacements();
		Clurge.INSTANCE.getVmManager().executeDeploymentPlan(deploymentPlan);
	}

    /**
     * Applies the self-adaptation configured to take place periodically.
     */
    public void applyPeriodicSelfAdaptation() {
        logger.info("Executing periodic self-adaptation");
		try {
			PeriodicSelfAdaptationOps options = getSelfAdaptationOptions().getPeriodicSelfAdaptationOps();

			if (options.getLocalSearchAlgorithm() != null && options.getMaxExecTimeSeconds() > 0) {
				// The construction heuristic is set to first fit, but anyone could be selected because in this case,
				// all the VMs are already assigned to a host. Therefore, it is not needed to apply a construction heuristic
				RecommendedPlanRequest recommendedPlanRequest = new RecommendedPlanRequest(
						options.getMaxExecTimeSeconds(), null, options.getLocalSearchAlgorithm());

				Clurge.INSTANCE.getVmManager().executeDeploymentPlan(
						Clurge.INSTANCE.getVmManager().getRecommendedPlan(recommendedPlanRequest, true, new ArrayList<Vm>()).getVMPlacements());
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
