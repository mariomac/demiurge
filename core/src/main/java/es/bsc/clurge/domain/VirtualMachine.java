package es.bsc.clurge.domain;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import java.io.File;
import java.util.Date;

public class VirtualMachine {

	private String name;
	private String image; // It can be an ID or a URL
	private int cpus;
	private int ramMb;
	private int diskGb;
	private int swapMb;

	private String initScript;
	private String applicationId;

	// The next three parameters are just valid within the Ascetic project.
	// It would be better to put them in a subclass

	private PreDeploymentOptions preDeploymentOptions;

	private boolean deployed = false;
	/* the next properties only should be set when the VM is deployed */
	private DeploymentInfo deploymentInfo;
	/* */


	private VirtualMachine() {
	}

	public final boolean isNeedingFloatingIp() {
		return needingFloatingIp;
	}

	public final boolean isDeployed() {
		return deployed;
	}

	public final String getName() {
		return name;
	}

	public final String getImage() {
		return image;
	}

	public final int getCpus() {
		return cpus;
	}

	public final int getRamMb() {
		return ramMb;
	}

	public final int getDiskGb() {
		return diskGb;
	}

	public final int getSwapMb() {
		return swapMb;
	}

	public final String getInitScript() {
		return initScript;
	}

	public void setInitScript(String initScript) {
		// If a path for an init script was specified
		if (initScript != null && !initScript.equals("")) {
			// Check that the path is valid and the file can be read
			File f = new File(initScript);
			if (!f.isFile() || !f.canRead()) {
				throw new IllegalArgumentException("The path for the init script is not valid");
			}
			this.initScript = initScript;
		}
	}

	public final String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public final String getOvfId() {
		return ovfId;
	}

	public void setOvfId(String ovfId) {
		this.ovfId = ovfId;
	}

	public final String getSlaId() {
		return slaId;
	}

	public void setSlaId(String slaId) {
		this.slaId = slaId;
	}

	public final String getPreferredHost() {
		return preferredHost;
	}

	public final boolean isBelongingToAnApp() {
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

	public String getDeploymentId() {
		return deploymentId;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getState() {
		return state;
	}

	public long getCreationTimeStamp() {
		return creationTimeStamp;
	}

	public PhysicalHost getHost() {
		return host;
	}

	public void setHost(PhysicalHost host) {
		this.host = host;
	}

	public void setDeployed(boolean deployed) {
		this.deployed = deployed;
	}

	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setCreationTimeStamp(long creationTimeStamp) {
		this.creationTimeStamp = creationTimeStamp;
	}

	public static class Builder {
		private String name;
		public Builder setName(String name) {
			this.name = name;
			return this;
		}
		private String image; // It can be an ID or a URL
		public Builder setImage(String image) {
			this.image = image;
			return this;		}
		private int cpus;
		public Builder setCpus(int cpus) {
			this.cpus = cpus;
			return this;
		}
		private int ramMb;
		public Builder setRamMb(int ramMb) {
			this.ramMb = ramMb;
			return this;
		}
		private int diskGb;
		public Builder setDiskGb(int diskGb) {
			this.diskGb = diskGb;
			return this;
		}
		private int swapMb;
		public Builder setSwapMb(int swapMb) {
			this.swapMb = swapMb;
			return this;
		}

		private String initScript;
		public Builder setInitScript(String initScript) {
			this.initScript = initScript;
			return this;
		}
		private String applicationId;
		public Builder setApplicationId(String applicationId) {
			this.applicationId = applicationId;
			return this;
		}
		private String ovfId = "";
		public Builder setOvfId(String ovfId) {
			this.ovfId = ovfId;
			return this;
		}
		private String slaId = "";
		public Builder setSlaId(String slaId) {
			this.slaId = slaId;
			return this;
		}
		private boolean needingFloatingIp = false;
		public Builder setNeedingFloatingIp(boolean needingFloatingIp) {
			this.needingFloatingIp = needingFloatingIp;
			return this;
		}

		private String preferredHost;
		public Builder setPreferredHost(String preferredHost) {
			this.preferredHost = preferredHost;
			return this;
		}

		private boolean deployed = false;
		public Builder setDeployed(boolean deployed) {
			this.deployed = deployed;
			return this;
		}
		/* the next properties only should be set when the VM is deployed */
		private String deploymentId;
		public Builder setDeploymentId(String deploymentId) {
			this.deploymentId = deploymentId;
			return this;
		}
		private String ipAddress;
		public Builder setIpAddress(String ip) {
			this.ipAddress = ip;
			return this;
		}
		private String state;
		public Builder setState(String state) {
			this.state = state;
			return this;
		}
		private long creationTimeStamp;
		public Builder setCreationTimeStamp(long creationTimeStamp) {
			this.creationTimeStamp = creationTimeStamp;
			return this;
		}
		private PhysicalHost host;
		public Builder setHost(PhysicalHost host) {
			this.host = host;
			return this;
		}

		public VirtualMachine build() {
			VirtualMachine vm = new VirtualMachine();
			vm.applicationId = applicationId;
			vm.cpus = cpus;
			vm.creationTimeStamp = creationTimeStamp;
			vm.deployed = deployed;
			vm.deploymentId = deploymentId;
			vm.diskGb = diskGb;
			vm.host = host;
			vm.image = image;
			vm.initScript = initScript;
			vm.ipAddress = ipAddress;
			vm.name = name;
			vm.needingFloatingIp = needingFloatingIp;
			vm.ovfId = ovfId;
			vm.preferredHost = preferredHost;
			vm.ramMb = ramMb;
			vm.slaId = slaId;
			vm.state = state;
			vm.swapMb = swapMb;
			return vm;
		}
	}
}
