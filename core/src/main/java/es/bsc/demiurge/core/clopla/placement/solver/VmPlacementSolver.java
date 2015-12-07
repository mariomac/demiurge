/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package es.bsc.demiurge.core.clopla.placement.solver;

import es.bsc.demiurge.core.clopla.placement.config.VmPlacementConfig;
import es.bsc.demiurge.core.configuration.VmmConfig;
import org.optaplanner.core.api.solver.Solver;

/**
 * This class creates an instance of an OptaPlanner Solver from an instance of VmPlacementConfig.
 *  
 * @author Mario Macias (github.com/mariomac), David Ortiz (david.ortiz@bsc.es)
 */
public class VmPlacementSolver {
    
    private final VmPlacementSolverFactory vmPlacementSolverFactory;

    public VmPlacementSolver(VmPlacementConfig vmPlacementConfig) {
        this.vmPlacementSolverFactory = new VmPlacementSolverFactory(vmPlacementConfig,
				VmmConfig.INSTANCE.getPlacementPolicies());
    }

    /**
     * Returns a solver built from the configuration options specified in vmPlacementConfig.
     *
     * @return the solver
     */
    public Solver buildSolver() {
        return vmPlacementSolverFactory.getSolverFactory().buildSolver();
    }

}