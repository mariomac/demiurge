package es.bsc.clurge;

import es.bsc.clurge.domain.VirtualMachine;
import es.bsc.clurge.exception.CloudMiddlewareException;
import es.bsc.clurge.vmm.VmAction;
import es.bsc.clurge.vmm.VmManagerListener;

import java.util.List;

/**
 * Created by mmacias on 5/11/15.
 */
public interface VirtualMachineManager {
	/**
	 * Returns a list of the VMs deployed.
	 *
	 * @return the list of VMs deployed.
	 */
	List<VirtualMachine> getAllVms();

	/**
	 * Returns a specific VM deployed.
	 *
	 * @param vmId the ID of the VM
	 * @return the VM
	 */
	VirtualMachine getVm(String vmId) throws CloudMiddlewareException;

	/**
	 * Returns all the VMs deployed that belong to a specific application.
	 *
	 * @param appId the ID of the application
	 * @return the list of VMs
	 */
	List<VirtualMachine> getVmsOfApp(String appId);

	/**
	 * Deletes all the VMs that belong to a specific application.
	 *
	 * @param appId the ID of the application
	 */
	void deleteVmsOfApp(String appId);

	/**
	 * Deletes a VM and applies self-adaptation if it is enabled.
	 *
	 * @param vmId the ID of the VM
	 */
	void deleteVm(String vmId) throws CloudMiddlewareException;

	/**
	 * Deploys a list of VMs and returns its IDs.
	 *
	 * @param vms the VMs to deploy
	 * @return the IDs of the VMs deployed in the same order that they were received
	 */
	List<String> deployVms(List<VirtualMachine> vms) throws CloudMiddlewareException;

	/**
	 * Migrates a VM to a specific host.
	 *
	 * @param vmId the ID of the VM
	 * @param destinationHostName the host where the VM will be migrated to
	 */
	void migrateVm(String vmId, String destinationHostName) throws CloudMiddlewareException;

	/**
	 * Checks whether a VM exists.
	 *
	 * @param vmId the ID of the VM
	 * @return True if exists a VM with the input ID, false otherwise
	 */
	boolean existsVm(String vmId);

	void addListener(VmManagerListener listener);
	void removeListener(VmManagerListener listener);

	void performActionOnVm(String vmId, VmAction action) throws CloudMiddlewareException;


}
