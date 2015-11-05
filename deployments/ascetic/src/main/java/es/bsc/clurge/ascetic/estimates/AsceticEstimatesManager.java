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

package es.bsc.clurge.ascetic.estimates;


import es.bsc.clurge.Clurge;
import es.bsc.clurge.ascetic.modellers.energy.EnergyModeller;
import es.bsc.clurge.ascetic.modellers.price.PricingModeller;
import es.bsc.clurge.estimates.DeploymentPlanEstimation;
import es.bsc.clurge.sched.Scheduler;


import java.util.List;

/**
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class AsceticEstimatesManager implements EstimatesManager {

    private final EstimatesGenerator estimatesGenerator = new EstimatesGenerator();

    private final EnergyModeller energyModeller;
	private final PricingModeller pricingModeller;

    public AsceticEstimatesManager(EnergyModeller energyModeller, PricingModeller pricingModeller) {
        this.energyModeller = energyModeller;
        this.pricingModeller = pricingModeller;
    }
    
    public List<DeploymentPlanEstimation> getVmEstimates(List<DeploymentPlanEstimation> vmsToBeEstimated) {
        Scheduler scheduler = new Scheduler(
				Clurge.INSTANCE.getPersistenceManager().getCurrentSchedulingAlg(),
				Clurge.INSTANCE.getVmManager().getAllVms(),
				this);

        return estimatesGenerator.getVmEstimates(
                scheduler.chooseBestDeploymentPlan(vmsToBeEstimated),
				Clurge.INSTANCE.getVmManager().getHosts(),
				Clurge.INSTANCE.getVmManager().getAllVms(),


				hostsManager.getHosts()),
                        vmsManager.getAllVms(), 
                energyModeller, 
                pricingModeller);
    }
    
}
