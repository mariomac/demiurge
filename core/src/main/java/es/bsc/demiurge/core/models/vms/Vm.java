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

package es.bsc.demiurge.core.models.vms;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.File;

/**
 * VM.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class Vm {

    private final String name;
    private String image; // It can be an ID or a URL
    private int cpus;
    private int ramMb;
    private int diskGb;
    private final int swapMb;
    private String initScript;
    private String applicationId;

    // The next three parameters are just valid within the Ascetic project.
    // It would be better to put them in a subclass
    private String ovfId = "";
    private String slaId = "";
    private boolean needsFloatingIp = false;

    // The next two parameters are just valid within the RenewIT project
    private ExtraParameters extraParameters;
    private boolean deployAfter = false;
    private long timeForDeploy = 0;
    private double powerEstimated;
    private long timeRequest = 0; //when the vm deployment request was received
    private boolean wasPostponed = false;
    private String tempId;

    private String preferredHost;

    // TODO: apply builder pattern instead of having several constructors.
    // This really needs a refactoring, although several classes will be affected.

    /**
     * Class constructor.
     * @param name The name of the instance.
     * @param image The ID of the image or a URL containing it.
     * @param cpus The number of CPUs.
     * @param ramMb The amount of RAM in MB.
     * @param diskGb The size of the disk in GB.
     * @param swapMb The amount of swap in MB.
     * @param initScript Script that will be executed when the VM is deployed.
     */
    public Vm(String name, String image, int cpus, int ramMb, int diskGb, int swapMb,
              String initScript, String applicationId) {
        validateConstructorParams(cpus, ramMb, diskGb, swapMb);
        this.name = name;
        this.image = image;
        this.cpus = cpus;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
        this.swapMb = swapMb;
        setInitScript(initScript);
        this.applicationId = applicationId;
    }

    public Vm(String name, String image, int cpus, int ramMb, int diskGb, String initScript, String applicationId) {
        validateConstructorParams(cpus, ramMb, diskGb, 0);
        this.name = name;
        this.image = image;
        this.cpus = cpus;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
        this.swapMb = 0;
        setInitScript(initScript);
        this.applicationId = applicationId;
    }

    public Vm(String name, String image, int cpus, int ramMb, int diskGb, String initScript, String applicationId,
              String ovfId, String slaId, boolean needsFloatingIp) {
        validateConstructorParams(cpus, ramMb, diskGb, 0);
        this.name = name;
        this.image = image;
        this.cpus = cpus;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
        this.swapMb = 0;
        setInitScript(initScript);
        this.applicationId = applicationId;
        this.ovfId = ovfId;
        this.slaId = slaId;
        this.needsFloatingIp = needsFloatingIp;
    }

    public Vm(String name, String image, int cpus, int ramMb, int diskGb, String initScript, String applicationId,
              String ovfId, String slaId, String preferredHost) {
        validateConstructorParams(cpus, ramMb, diskGb, 0);
        this.name = name;
        this.image = image;
        this.cpus = cpus;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
        this.swapMb = 0;
        setInitScript(initScript);
        this.applicationId = applicationId;
        this.ovfId = ovfId;
        this.slaId = slaId;
        this.preferredHost = preferredHost;
    }

    public Vm(Vm v) {
        this.name = v.name;
        this.image = v.image;
        this.cpus = v.cpus;
        this.ramMb = v.ramMb;
        this.diskGb = v.diskGb;
        this.swapMb = 0;
        setInitScript(v.initScript);
        this.applicationId = v.applicationId;
        this.ovfId = v.ovfId;
        this.slaId = v.slaId;
        this.preferredHost = v.preferredHost;
        this.timeForDeploy = v.getTimeForDeploy();
        this.timeRequest = v.getTimeRequest();
        this.wasPostponed = v.wasPostponed;
        this.powerEstimated = v.getPowerEstimated();
        this.extraParameters = v.getExtraParameters();
        this.deployAfter = v.deployAfter;
        this.tempId = v.getTempId();
    }


    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public int getCpus() {
        return cpus;
    }

    public int getRamMb() {
        return ramMb;
    }

    public int getDiskGb() {
        return diskGb;
    }
    
    public int getSwapMb() {
        return swapMb;
    }

    public String getInitScript() {
        return initScript;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setInitScript(String initScript) {
        // If a path for an loadConfiguration script was specified
        if (initScript != null && !initScript.equals("")) {
            // Check that the path is valid and the file can be read
            File f = new File(initScript);
            if (!f.isFile() || !f.canRead()) {
                //throw new IllegalArgumentException("The path for the loadConfiguration script is not valid");

            }else{
            this.initScript = initScript;}
        }
    }

    public void setInitScriptStr(String initScript) {
            this.initScript = initScript;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getOvfId() {
        return ovfId;
    }

    public void setOvfId(String ovfId) {
        this.ovfId = ovfId;
    }

    public String getSlaId() {
        return slaId;
    }

    public void setSlaId(String slaId) {
        this.slaId = slaId;
    }

    public String getPreferredHost() {
        return preferredHost;
    }

    public boolean needsFloatingIp() {
        return needsFloatingIp;
    }

    public void setCpus(int cpus) {
        this.cpus = cpus;
    }

    public void setRamMb(int ramMb) {
        this.ramMb = ramMb;
    }

    public void setDiskGb(int diskGb) {
        this.diskGb = diskGb;
    }

    public boolean belongsToAnApp() {
        return applicationId != null && !applicationId.equals("") && !applicationId.equals(" ");
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    private void validateConstructorParams(int cpus, int ramMb, int diskGb, int swapMb) {
        Preconditions.checkArgument(cpus > 0, "CPUs was %s but expected positive", cpus);
        Preconditions.checkArgument(ramMb > 0, "RAM MB was %s but expected positive", ramMb);
        Preconditions.checkArgument(diskGb > 0, "Disk GB was %s but expected positive", diskGb);
        Preconditions.checkArgument(swapMb >= 0, "Swap MB was %s but expected non-negative", swapMb);
    }

    public ExtraParameters getExtraParameters() {
        return extraParameters;
    }

    /**
     *
     * @param extra
     * The extra parameters
     */
    public void setExtraParameters(ExtraParameters extra) {
        this.extraParameters = extra;
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

    public double getPowerEstimated() {
        return powerEstimated;
    }

    public void setPowerEstimated(double powerEstimated) {
        this.powerEstimated = powerEstimated;
    }

    public long getTimeRequest() {
        return timeRequest;
    }

    public void setTimeRequest(long timeRequest) {
        this.timeRequest = timeRequest;
    }

    public boolean isWasPostponed() {
        return wasPostponed;
    }

    public void setWasPostponed(boolean wasPostponed) {
        this.wasPostponed = wasPostponed;
    }

    public String getTempId() {
        return tempId;
    }

    public void setTempId(String tempId) {
        this.tempId = tempId;
    }
}
