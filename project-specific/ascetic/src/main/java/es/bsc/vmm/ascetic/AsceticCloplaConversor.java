package es.bsc.vmm.ascetic;

import es.bsc.vmm.ascetic.scheduler.clopla.CloplaAsceticEnergyModeller;
import es.bsc.vmm.core.clopla.placement.config.VmPlacementConfig;
import es.bsc.vmm.core.manager.components.EstimatesManager;
import es.bsc.vmm.core.models.scheduling.RecommendedPlanRequest;
import es.bsc.vmm.core.vmplacement.CloplaConversor;

/**
 * Created by mmacias on 23/11/15.
 */
public class AsceticCloplaConversor extends CloplaConversor {

	public VmPlacementConfig getCloplaConfig(String schedAlgorithmName,
											 RecommendedPlanRequest recommendedPlanRequest,
											 EstimatesManager estimatesManager)
	{
		int timeLimitSec = recommendedPlanRequest.getTimeLimitSeconds();
		if (getLocalSearch(recommendedPlanRequest) == null) {
			timeLimitSec = 1; // It does not matter because the local search alg will not be run, but the
			// VM placement library complains if we send 0
		}

		return new VmPlacementConfig.Builder(
				getPolicy(schedAlgorithmName),
				timeLimitSec,
				getConstructionHeuristic(recommendedPlanRequest.getConstructionHeuristicName()),
				getLocalSearch(recommendedPlanRequest),
				false)
				.estimatesManager(estimatesManager)
				.build();
	}
}
