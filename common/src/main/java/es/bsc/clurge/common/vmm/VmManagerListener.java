package es.bsc.clurge.common.vmm;

import es.bsc.clurge.common.models.vms.VmDeployed;

/**
 * Created by mmacias on 3/11/15.
 */
public interface VmManagerListener {
	void onVmDeployment(VmDeployed vm);
	void onVmDestruction(VmDeployed vm);
	void onVmMigration(VmDeployed vm);
	void onVmAction(VmDeployed vm, VmAction action);
}

