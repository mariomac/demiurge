package es.bsc.clurge.domain;

import com.google.common.base.Preconditions;
import es.bsc.clurge.Clurge;
import es.bsc.clurge.monit.HostButtonPresserRunnable;
import es.bsc.clurge.monit.HostPowerButtonAction;
import es.bsc.clurge.monit.PhysicalHostMonitoringInfo;
import es.bsc.clurge.monit.ServerLoad;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by mmacias on 5/11/15.
 */
public class PhysicalHost {
	private static final String CONFIG_TURN_OFF_DELAY_SECONDS = "turnOffDelaySeconds";
	private static final String CONFIG_TURN_ON_DELAY_SECONDS = "turnOnDelaySeconds";

	private PhysicalHostMonitoringInfo monitoringInfo;

	protected final String hostname;
	protected int totalCpus;
	protected double totalMemoryMb;
	protected double totalDiskGb;
	protected double assignedCpus;
	protected double assignedMemoryMb;
	protected double assignedDiskGb;
	protected double currentPower;

	protected AtomicBoolean turnedOff = new AtomicBoolean(false); // Several threads might try to turn on/off
	protected final int turnOnDelaySeconds;
	protected final int turnOffDelaySeconds;

	private Logger log = LogManager.getLogger(PhysicalHost.class);

	/**
	 * Class constructor
	 * @param hostname host name
	 */
	public PhysicalHost(String hostname, PhysicalHostMonitoringInfo info) {
		if(info == null) throw new IllegalArgumentException("PhysicalMonitoringInfo must not be null");
		this.hostname = hostname;
		this.turnOnDelaySeconds = Clurge.INSTANCE.getConfiguration().getInt(CONFIG_TURN_ON_DELAY_SECONDS);
		this.turnOffDelaySeconds = Clurge.INSTANCE.getConfiguration().getInt(CONFIG_TURN_OFF_DELAY_SECONDS);
	}

	public PhysicalHost(String hostname, int turnOnDelaySeconds, int turnOffDelaySeconds, PhysicalHostMonitoringInfo info) {
		if(info == null) throw new IllegalArgumentException("PhysicalMonitoringInfo must not be null");
		this.hostname = hostname;
		this.turnOnDelaySeconds = turnOnDelaySeconds;
		this.turnOffDelaySeconds = turnOffDelaySeconds;
	}

	/**
	 * Checks whether a host has enough available resources to host a VM.
	 * @param cpus number of CPUs needed by the VM
	 * @param memoryMb memory needed by the VM (in MB)
	 * @param diskGb disk space needed by the VM (in GB)
	 * @return Returns true if the host has enough available resources to host the VM.
	 * Returns false if the host does not have enough available resources
	 */
	public boolean hasEnoughResources(int cpus, int memoryMb, int diskGb) {
		return (getFreeCpus() >= cpus) && (getFreeMemoryMb() >= memoryMb) && (getFreeDiskGb() >= diskGb);
	}

	/**
	 * Checks whether a specific host has enough resources to deploy a set of VMs.
	 *
	 * @param vms the list of VMs
	 * @return true if the host has enough resources available, false otherwise
	 */
	public boolean hasEnoughResourcesToDeployVms(List<VirtualMachine> vms) {
		int totalCpus, totalRamMb, totalDiskGb;
		totalCpus = totalRamMb = totalDiskGb = 0;
		for (VirtualMachine vm : vms) {
			totalCpus += vm.getCpus();
			totalRamMb += vm.getRamMb();
			totalDiskGb += vm.getDiskGb();
		}
		return hasEnoughResources(totalCpus, totalRamMb, totalDiskGb);
	}

	/**
	 * @return host name
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * @return total number of CPUs of the host
	 */
	public int getTotalCpus() {
		return totalCpus;
	}

	/**
	 * @return total memory of the host (in MB)
	 */
	public double getTotalMemoryMb() {
		return totalMemoryMb;
	}

	/**
	 * @return total disk space of the host (in GB)
	 */
	public double getTotalDiskGb() {
		return totalDiskGb;
	}

	/**
	 * @return assigned CPUs of the host
	 */
	public double getAssignedCpus() {
		return assignedCpus;
	}

	/**
	 * @return assigned memory of the host (in MB)
	 */
	public double getAssignedMemoryMb() {
		return assignedMemoryMb;
	}

	/**
	 * @return assigned disk space of the host (in GB)
	 */
	public double getAssignedDiskGb() {
		return assignedDiskGb;
	}

	/**
	 * @return number of available CPUs of the host
	 */
	public double getFreeCpus() {
		return totalCpus - assignedCpus;
	}

	/**
	 * @return available memory of the host (in MB)
	 */
	public double getFreeMemoryMb() {
		return totalMemoryMb - assignedMemoryMb;
	}

	/**
	 * @return available disk space of the host (in GB)
	 */
	public double getFreeDiskGb() {
		return totalDiskGb - assignedDiskGb;
	}

	public int getTurnOnDelaySeconds() {
		return turnOnDelaySeconds;
	}

	public int getTurnOffDelaySeconds() {
		return turnOffDelaySeconds;
	}

	/**
	 * Returns the load that a host would have if a VM was deployed in it.
	 *
	 * @param vm the VM to deploy
	 * @return the future load
	 */
	public ServerLoad getFutureLoadIfVMDeployed(VirtualMachine vm) {
		return new ServerLoad(
				(getAssignedCpus() + vm.getCpus())/getTotalCpus(),
				(getAssignedMemoryMb() + vm.getRamMb())/getTotalMemoryMb(),
				(getAssignedDiskGb() + vm.getDiskGb())/getTotalDiskGb());
	}

	public void setAssignedCpus(double assignedCpus) {
		this.assignedCpus = assignedCpus;
	}

	public void setAssignedMemoryMb(double assignedMemoryMb) {
		this.assignedMemoryMb = assignedMemoryMb;
	}

	public void setAssignedDiskGb(double assignedDiskGb) {
		this.assignedDiskGb = assignedDiskGb;
	}

	public ServerLoad getServerLoad() {
		return new ServerLoad(assignedCpus/totalCpus, assignedMemoryMb/totalMemoryMb, assignedDiskGb/totalDiskGb);
	}

	public void setTotalCpus(int totalCpus) {
		this.totalCpus = totalCpus;
	}

	public void setTotalMemoryMb(double totalMemoryMb) {
		this.totalMemoryMb = totalMemoryMb;
	}

	public void setTotalDiskGb(double totalDiskGb) {
		this.totalDiskGb = totalDiskGb;
	}

	public double getCurrentPower() {
		return currentPower;
	}

	public void setCurrentPower(double currentPower) {
		this.currentPower = currentPower;
	}

	public AtomicBoolean getTurnedOff() {
		return turnedOff;
	}

	public void setTurnedOff(AtomicBoolean turnedOff) {
		this.turnedOff = turnedOff;
	}

	/**
	 * Presses the power button of the host.
	 * If the host is turned on, then this function turns it off and vice versa.
	 */
	public void pressPowerButton() {
		// This is executed in a different thread. The reason is that we have defined a time delay between
		// the moment the power button is pressed and the moment the server is on or off.
		// In order to avoid blocking the main thread of execution, this is executed in a different thread.
		Thread thread;
		if (turnedOff.get()) {
			log.info("Server requested to be turned on: " + hostname);
			thread = new Thread(new HostButtonPresserRunnable(this, HostPowerButtonAction.TURN_ON));
		}
		else {
			log.info("Server requested to be turned off: " + hostname);
			thread = new Thread(new HostButtonPresserRunnable(this, HostPowerButtonAction.TURN_OFF));
		}
		thread.start();
	}

	/**
	 * Turns on the host.
	 */
	public void turnOn() {
		turnedOff.getAndSet(false);
		log.info("Server turned on: " + hostname);
	}

	/**
	 * Turns off the host.
	 */
	public void turnOff() {
		turnedOff.getAndSet(true);
		log.info("Server turned off: " + hostname);
	}

	/**
	 * Returns whether the host is on.
	 * @return True if the host is on, false otherwise
	 */
	public boolean isOn() {
		return !turnedOff.get();
	}


	@Override
	public String toString() {
		return "Host{" +
				"hostname='" + hostname + '\'' +
				", totalCpus=" + totalCpus +
				", totalMemoryMb=" + totalMemoryMb +
				", totalDiskGb=" + totalDiskGb +
				", assignedCpus=" + assignedCpus +
				", assignedMemoryMb=" + assignedMemoryMb +
				", assignedDiskGb=" + assignedDiskGb +
				", currentPower=" + currentPower +
				", turnedOff=" + turnedOff +
				", turnOnDelaySeconds=" + turnOnDelaySeconds +
				", turnOffDelaySeconds=" + turnOffDelaySeconds +
				'}';
	}

	public PhysicalHostMonitoringInfo getMonitoringInfo() {
		return monitoringInfo;
	}

	public void setMonitoringInfo(PhysicalHostMonitoringInfo monitoringInfo) {
		this.monitoringInfo = monitoringInfo;
	}
}
