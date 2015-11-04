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

import es.bsc.clurge.estimates.AbstractVmEstimate;
import es.bsc.clurge.models.vms.Vm;

/**
 * VM power and price estimates.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
class AsceticVmEstimate extends AbstractVmEstimate {
	private final double powerEstimate;
    private final double priceEstimate;

    public AsceticVmEstimate(Vm vm, double powerEstimate, double priceEstimate) {
        super(vm);
        this.powerEstimate = powerEstimate;
        this.priceEstimate = priceEstimate;
    }

    public final double getPowerEstimate() {
        return powerEstimate;
    }

    public final double getPriceEstimate() {
        return priceEstimate;
    }

}
