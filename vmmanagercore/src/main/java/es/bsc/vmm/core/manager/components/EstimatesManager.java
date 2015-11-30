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

import es.bsc.vmm.core.cloudmiddleware.CloudMiddlewareException;
import es.bsc.vmm.core.manager.VmManager;
import es.bsc.vmm.core.models.estimates.VmEstimate;
import es.bsc.vmm.core.models.estimates.VmToBeEstimated;
import es.bsc.vmm.core.db.VmManagerDb;
import es.bsc.vmm.core.drivers.Estimator;
import es.bsc.vmm.core.models.estimates.ListVmEstimates;
import es.bsc.vmm.core.models.scheduling.DeploymentPlan;
import es.bsc.vmm.core.models.scheduling.VmAssignmentToHost;
import es.bsc.vmm.core.models.vms.Vm;
import es.bsc.vmm.core.models.vms.VmDeployed;
import es.bsc.vmm.core.scheduler.EstimatesGenerator;
import es.bsc.vmm.core.scheduler.Scheduler;
import es.bsc.vmm.core.scheduler.SchedulingAlgorithmsRepository;
import freemarker.ext.beans.HashAdapter;

import java.util.*;

/**
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class EstimatesManager implements Iterable<Estimator> {

    private final EstimatesGenerator estimatesGenerator = new EstimatesGenerator();
    private final VmManager vmManager;
    private final HostsManager hostsManager;
    private final VmManagerDb db;

    public EstimatesManager(VmManager vmm, Set<Estimator> estimators) {

		for(Estimator e: estimators) {
			this.estimators.put(e.getClass(),e);
			this.estimatorsByLabel.put(e.getLabel(),e);
		}
        this.vmManager = vmm;
        this.hostsManager = vmm.getHostsManager();
        this.db = vmm.getDB();
    }

	/**
	 * Returns price and energy estimates for each VM in a deployment plan.
	 *
	 * @param vmsToBeEstimated The VMs to be estimated
	 * @return the price and energy estimates for each VM
	 */
    public ListVmEstimates getVmEstimates(List<VmToBeEstimated> vmsToBeEstimated) throws CloudMiddlewareException {
		DeploymentPlan deploymentPlan = vmManager.getVmsManager().chooseBestDeploymentPlan(
				vmsToBeEstimatedToVms(vmsToBeEstimated));

		List<VmEstimate> vmEstimates = new ArrayList<>();
		for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {
			vmEstimates.add(vmAssignmentToHost.getVmEstimate(vmManager.getAllVms(), deploymentPlan, this));
		}

		return new ListVmEstimates(vmEstimates);
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
	private Map<String, Estimator> estimatorsByLabel = new HashMap<>();

	public Estimator getByLabel(String label) {
		return estimatorsByLabel.get(label);
	}

	public Estimator get(Class<? extends Estimator> e) {
		return estimators.get(e);
	}

	@Override
	public Iterator<Estimator> iterator() {
		return estimators.values().iterator();
	}
    
}
