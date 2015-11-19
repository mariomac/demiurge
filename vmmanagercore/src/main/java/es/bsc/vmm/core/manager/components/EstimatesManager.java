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

package es.bsc.vmm.core.manager.components;

import es.bsc.vmm.core.manager.VmManager;
import es.bsc.vmm.core.models.estimates.VmToBeEstimated;
import es.bsc.vmm.core.db.VmManagerDb;
import es.bsc.vmm.core.drivers.Estimator;
import es.bsc.vmm.core.models.estimates.ListVmEstimates;
import es.bsc.vmm.core.models.vms.Vm;
import es.bsc.vmm.core.scheduler.EstimatesGenerator;
import es.bsc.vmm.core.scheduler.Scheduler;
import es.bsc.vmm.core.scheduler.SchedulingAlgorithmsRepository;

import java.util.*;

/**
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class EstimatesManager implements Iterable<Estimator> {

    private final EstimatesGenerator estimatesGenerator = new EstimatesGenerator();
    private final VmManager vmManager;
    private final HostsManager hostsManager;
    private final VmManagerDb db;
	private SchedulingAlgorithmsRepository schedulingAlgorithmsRepository;
    
    public EstimatesManager(VmManager vmm, Set<Estimator> estimators, SchedulingAlgorithmsRepository schedulingAlgorithmsRepository) {

		for(Estimator e: estimators) {
			this.estimators.put(e.getClass(),e);
		}
        this.vmManager = vmm;
        this.hostsManager = vmm.getHostsManager();
        this.db = vmm.getDB();
		this.schedulingAlgorithmsRepository = schedulingAlgorithmsRepository;
    }
    
    public ListVmEstimates getVmEstimates(List<VmToBeEstimated> vmsToBeEstimated) {
        Scheduler scheduler = new Scheduler(
				db.getCurrentSchedulingAlg(),
				vmManager.getVmsManager().getAllVms(),
				this,
				schedulingAlgorithmsRepository
				);
        return estimatesGenerator.getVmEstimates(
                scheduler.chooseBestDeploymentPlan(
                        vmsToBeEstimatedToVms(vmsToBeEstimated), 
                        hostsManager.getHosts()),
				vmManager.getVmsManager().getAllVms(),
                		this);
    }

    /**
     * Transforms a list of VMs to be estimated to a list of VMs.
     *
     * @param vmsToBeEstimated the list of VMs to be estimated
     * @return the list of VMs
     */
    // Note: this function would not be needed if VmToBeEstimated inherited from Vm
    private List<Vm> vmsToBeEstimatedToVms(List<VmToBeEstimated> vmsToBeEstimated) {

        List<Vm> result = new ArrayList<>();
        for (VmToBeEstimated vmToBeEstimated: vmsToBeEstimated) {
            result.add(vmToBeEstimated.toVm());
        }
        return result;
    }


	private Map<Class<? extends Estimator>, Estimator> estimators = new HashMap<>();


	public Estimator get(Class<? extends Estimator> e) {
		return estimators.get(e);
	}

	@Override
	public Iterator<Estimator> iterator() {
		return estimators.values().iterator();
	}
    
}
