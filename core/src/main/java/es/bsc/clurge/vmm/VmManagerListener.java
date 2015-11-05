package es.bsc.clurge.vmm;

import es.bsc.clurge.domain.VirtualMachine;

/**
 *
 * The VM Manager component does not catch any exception when calling the next methods.
 * It's the responsibility of the
 * implementor to avoid the next methods trowing any exception to not interrupt the normal
 * operation of the VMM.
 *
 * Created by mmacias on 3/11/15.
 */
public interface VmManagerListener {
	void onVmDeployment(VirtualMachine vm);
	void onVmDestruction(VirtualMachine vm);
	void onVmMigration(VirtualMachine vm);
	void onVmAction(VirtualMachine vm, VmAction action);

	void onPreVmDeployment(VirtualMachine vm);
}

