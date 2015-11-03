package es.bsc.clurge.vmm;

import es.bsc.clurge.models.vms.Vm;
import es.bsc.clurge.models.vms.VmDeployed;

/**
 * Created by mmacias on 3/11/15.
 */
public interface VmManagerListener {
	void onVmDeployment(VmDeployed vm);
	void onVmDestruction(VmDeployed vm);
	void onVmMigration(VmDeployed vm);
}

