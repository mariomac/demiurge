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
import es.bsc.clurge.domain.PhysicalHost;
import es.bsc.clurge.domain.VirtualMachine;
import es.bsc.clurge.estimates.Estimator;


import java.util.List;

/**
 * VM placement to a Host.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VmAssignmentToHost {

    private final VirtualMachine vm;
    private final PhysicalHost host;

    /**
     * Class constructor.
     *
     * @param vm the VM
     * @param host the host
     */
    public VmAssignmentToHost(VirtualMachine vm, PhysicalHost host) {
        this.vm = vm;
        this.host = host;
    }

    public VirtualMachine getVm() {
        return vm;
    }

    public PhysicalHost getHost() {
        return host;
    }


	public <T> T getEstimation(List<VirtualMachine> vmsDeployed, DeploymentPlan deploymentPlan, Class<? extends Estimator<T>> estimatorClass) {
		return (T) Clurge.INSTANCE.getEstimator(estimatorClass).getVmEstimation(vm,host,vmsDeployed,deploymentPlan);

	}

    @Override
    public String toString() {
        return getVm().getName() + "-->" + getHost().getHostname();
    }

}
