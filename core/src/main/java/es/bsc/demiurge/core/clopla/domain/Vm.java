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

package es.bsc.demiurge.core.clopla.domain;

import es.bsc.demiurge.core.clopla.domain.comparators.HostStrengthComparator;
import es.bsc.demiurge.core.clopla.domain.comparators.VmDifficultyComparator;
import es.bsc.demiurge.core.models.vms.ExtraParameters;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

/**
 * This class represents a virtual machine.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz (david.ortiz@bsc.es)
 */
@PlanningEntity(difficultyComparatorClass = VmDifficultyComparator.class)
public class Vm extends AbstractPersistable {

    private int ncpus;
    private int ramMb;
    private int diskGb;
    private String appId;
    private Host host; // The host where the Vm should be deployed according to the planner.
    private String alphaNumericId; /* This might be needed in some cases. For example, OpenStack uses alphanumeric
                                   IDs, and optaplanner needs an ID of type long. */

    // Added by Mauro
    private boolean isDeployed;  //this is set to true if the Vm was already deployed
    private ExtraParameters extraParameters;// This is valid within the RenewIT project
    private double powerConsumption; // This is valid within the RenewIT project
    private double powerEstimation; // This is valid within the RenewIT project
    private boolean deployAfter = false;
    private long timeForDeploy = 0;

    public Vm() { } // OptaPlanner needs no arg constructor to clone

    public static class Builder {

        // Required parameters
        private final Long id;
        private final int ncpus;
        private final int ramMb;
        private final int diskGb;

        // Optional parameters
        private String appId;
        private String alphaNumericId;
        private boolean isDeployed;
        private ExtraParameters extraParameters;
        private double powerConsumption;
        private double powerEstimation;

        public Builder (Long id, int ncpus, int ramMb, int diskGb) {
            this.id = id;
            this.ncpus = ncpus;
            this.ramMb = ramMb;
            this.diskGb = diskGb;
        }

        public Builder appId(String appId) {
            this.appId = appId;
            return this;
        }

        public Builder isDeployed(boolean isDeployed) {
            this.isDeployed = isDeployed;
            return this;
        }

        public Builder extraParameters(ExtraParameters extraParameters) {
            this.extraParameters = extraParameters;
            return this;
        }

        public Builder alphaNumericId(String alphaNumericId) {
            this.alphaNumericId = alphaNumericId;
            return this;
        }
        public Builder powerConsumption(double powerConsumption) {
            this.powerConsumption = powerConsumption;
            return this;
        }

        public Builder powerEstimation(double powerEstimation) {
            this.powerEstimation = powerEstimation;
            return this;
        }

        public Vm build() {
            return new Vm(this);
        }

    }
    
    private Vm(Builder builder) {
        id = builder.id;
        ncpus = builder.ncpus;
        ramMb = builder.ramMb;
        diskGb = builder.diskGb;
        appId = builder.appId;
        alphaNumericId = builder.alphaNumericId;
        isDeployed = builder.isDeployed;
        extraParameters = builder.extraParameters;
        powerConsumption = builder.powerConsumption;
        powerEstimation = builder.powerEstimation;
    }

    /**
     * Checks whether this VM is deployed in the same host as the given one.
     *
     * @param vm the given VM
     * @return True if the two VMs are deployed in the same host, False otherwise.
     */
    public boolean isInTheSameHost(Vm vm) {
        if (vm.getHost() == null && host == null) {
            return true;
        }
        else if (host != null && vm.getHost() != null) {
            return host.getId().equals(vm.getHost().getId());
        }
        return false;
    }
    
    public int getNcpus() {
        return ncpus;
    }

    public int getRamMb() {
        return ramMb;
    }

    public int getDiskGb() {
        return diskGb;
    }

    public String getAppId() {
        return appId;
    }

    public String getAlphaNumericId() {
        return alphaNumericId;
    }

    public boolean isDeployed() {
        return isDeployed;
    }

    public ExtraParameters getExtraParameters() {
        return extraParameters;
    }

    public double getPowerConsumption() {
        return powerConsumption;
    }

    public void setPowerConsumption(double powerConsumption) {
        this.powerConsumption = powerConsumption;
    }


    public double getPowerEstimation() {
        return powerEstimation;
    }

    public void setPowerEstimation(double powerEstimation) {
        this.powerEstimation = powerEstimation;
    }

    @PlanningVariable(valueRangeProviderRefs = {"hostRange"}, strengthComparatorClass = HostStrengthComparator.class)
    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public void setNcpus(int ncpus) {
        this.ncpus = ncpus;
    }

    public void setRamMb(int ramMb) {
        this.ramMb = ramMb;
    }

    public void setDiskGb(int diskGb) {
        this.diskGb = diskGb;
    }

    @Override
    public String toString() {
        return "VM - ID:" + id.toString() + ", cpus:" + ncpus + ", ram:" + ramMb + ", disk:" + diskGb
                + ", app:" + appId;
    }

    public boolean isDeployAfter() {
        return deployAfter;
    }


    public long getTimeForDeploy() {
        return timeForDeploy;
    }

    public void setTimeForDeploy(long timeForDeploy) {
        if (timeForDeploy != 0){
            this.deployAfter = true;
        }
        this.timeForDeploy = timeForDeploy;
    }

}
