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

package es.bsc.clurge.core.modellers.energy.dummy;

import es.bsc.clurge.core.modellers.energy.EnergyModeller;
import es.bsc.clurge.common.models.scheduling.DeploymentPlan;
import es.bsc.clurge.common.models.vms.Vm;
import es.bsc.clurge.common.models.vms.VmDeployed;
import es.bsc.clurge.core.monitoring.hosts.Host;

import java.util.List;

/**
 * This is a dummy Energy Modeller. It always returns 0, but it can be helpful in cases where there is not any
 * Energy Modeller defined. Using this Modeller is safer than setting the Energy Modeller attribute to null in the VMM.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class DummyEnergyModeller implements EnergyModeller {

    public DummyEnergyModeller() { }

    @Override
    public double getPredictedAvgPowerVm(Vm vm, Host host, List<VmDeployed> vmsDeployed,
                                         DeploymentPlan deploymentPlan) {
        return 0;
    }

    @Override
    public double getPredictedEnergyVm(Vm vm, Host host, List<VmDeployed> vmsDeployed,
                                       DeploymentPlan deploymentPlan) {
        return 0;
    }

    @Override
    public double getHostPredictedAvgPower(String hostname, List<Vm> vms) {
        return 0;
    }

}
