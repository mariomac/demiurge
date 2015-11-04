package es.bsc.clurge.common.vmm;

import es.bsc.clurge.common.models.vms.VmDeployed;


public abstract class VmManagerListenerAdaptor implements VmManagerListener {

	@Override
	public void onVmDeployment(VmDeployed vm) {

	}

	@Override
	public void onVmDestruction(VmDeployed vm) {

	}

	@Override
	public void onVmMigration(VmDeployed vm) {

	}
}
