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

package es.bsc.vmm.core.clopla.placement.config;

/**
 * Enumeration of the policies supported by this library.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz (david.ortiz@bsc.es)
 */
public enum Policy {

    CONSOLIDATION("Consolidation"), 
    DISTRIBUTION("Distribution"),
    GROUP_BY_APP("Group by App"), 
    RANDOM("Random"),
	ESTIMATOR_BASED("EstimatorBased");

    private final String name;

	// default
	private String estimatorLabel = "energy";

    private Policy(String name) {
        this.name = name;
    }


	// TO DO: take care about this. This is a shoddy patch for the moment
	public String getEstimatorLabel() {
		if(this == ESTIMATOR_BASED) {
			return estimatorLabel;
		} else {
			return null;
		}
	}

	public void setEstimatorLabel(String estimatorLabel) {
		this.estimatorLabel = estimatorLabel;
	}

	@Override
    public String toString() {
        return name;
    }
    
}