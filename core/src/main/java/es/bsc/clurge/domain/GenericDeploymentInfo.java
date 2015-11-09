package es.bsc.clurge.domain;

/**
 * Created by mmacias on 9/11/15.
 */
public class GenericDeploymentInfo implements DeploymentInfo {
	private String deploymentId;
	private String ipAddress;
	private String state;
	private long creationTimeStamp;
	private PhysicalHost host;

	public GenericDeploymentInfo(String deploymentId, String ipAddress, String state, long creationTimeStamp, PhysicalHost host) {
		this.deploymentId = deploymentId;
		this.ipAddress = ipAddress;
		this.state = state;
		this.creationTimeStamp = creationTimeStamp;
		this.host = host;
	}

	public String getDeploymentId() {
		return deploymentId;
	}

	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public long getCreationTimeStamp() {
		return creationTimeStamp;
	}

	public void setCreationTimeStamp(long creationTimeStamp) {
		this.creationTimeStamp = creationTimeStamp;
	}

	public PhysicalHost getHost() {
		return host;
	}

	public void setHost(PhysicalHost host) {
		this.host = host;
	}
}
