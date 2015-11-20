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

import es.bsc.vmm.core.db.VmManagerDb;
import es.bsc.vmm.core.scheduler.SchedulingAlgorithmsRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class SchedulingAlgorithmsManager {

    private final VmManagerDb db;
    private SchedulingAlgorithmsRepository schedulingAlgorithmsRepository;

    public SchedulingAlgorithmsManager(VmManagerDb db, SchedulingAlgorithmsRepository schedulingAlgorithmsRepository) {
        this.db = db;
		this.schedulingAlgorithmsRepository = schedulingAlgorithmsRepository;
    }
    
    /**
     * Returns the scheduling algorithms that can be applied.
     *
     * @return the list of scheduling algorithms
     */
    public List<String> getAvailableSchedulingAlgorithms() {
        return new ArrayList<>(schedulingAlgorithmsRepository.getAvailable());
    }

    /**
     * Returns the scheduling algorithm that is being used now.
     *
     * @return the scheduling algorithm being used
     */
    public String getCurrentSchedulingAlgorithm() {
        return db.getCurrentSchedulingAlg();
    }

    /**
     * Changes the scheduling algorithm.
     *
     * @param schedulingAlg the scheduling algorithm to be used
     */
    public void setSchedulingAlgorithm(String schedulingAlg) {
        db.setCurrentSchedulingAlg(schedulingAlg);
    }
    
}
