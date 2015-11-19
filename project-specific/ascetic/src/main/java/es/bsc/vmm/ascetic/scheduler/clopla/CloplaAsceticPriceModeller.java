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

package es.bsc.vmm.ascetic.scheduler.clopla;

import es.bsc.clopla.domain.Host;
import es.bsc.clopla.domain.Vm;
import es.bsc.clopla.modellers.PriceModeller;
import es.bsc.vmm.ascetic.modellers.price.ascetic.AsceticPricingModellerAdapter;
import es.bsc.vmm.core.manager.components.EstimatesManager;

import java.util.List;

/**
 * This class is a pricing modeller that can be used by the Vm Placement library.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class CloplaAsceticPriceModeller implements PriceModeller {

    private final EstimatesManager estimatesManager;

    public CloplaAsceticPriceModeller(EstimatesManager estimatesManager) {
        this.estimatesManager = estimatesManager;
    }

    @Override
    public double getCost(Host host, List<Vm> vmsDeployedInHost) {
		AsceticPricingModellerAdapter pma = (AsceticPricingModellerAdapter) estimatesManager.get(AsceticPricingModellerAdapter.class);

        double result = 0.0;
        for (Vm vm: vmsDeployedInHost) {
            result += pricingModeller.getVMChargesPrediction(vm.getNcpus(), vm.getRamMb(), vm.getDiskGb(), host.getHostname());
        }
        return result;
    }

}
