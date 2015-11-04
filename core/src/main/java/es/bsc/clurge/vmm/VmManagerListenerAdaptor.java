package es.bsc.clurge.vmm;

import es.bsc.clurge.models.vms.VmDeployed;


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
