package es.bsc.vmm.ascetic.mq;

import es.bsc.vmm.core.drivers.VmAction;
import es.bsc.vmm.core.drivers.VmmListener;
import es.bsc.vmm.core.models.vms.Vm;
import es.bsc.vmm.core.models.vms.VmDeployed;

/**
 * Created by mmacias on 19/11/15.
 */
public class MQEventNotificator implements VmmListener {
	@Override
	public void onVmDeployment(VmDeployed vm) {
		MessageQueue.publishMessageVmDeployed(vm);
	}
	@Override
	public void onVmDestruction(VmDeployed vm) {
		MessageQueue.publishMessageVmDestroyed(vm);
	}
	@Override
	public void onVmMigration(VmDeployed vm) {}
	@Override
	public void onVmAction(VmDeployed vm, VmAction action) {
		MessageQueue.publishMessageVmChangedState(vm, action);

	}

	@Override
	public void onPreVmDeployment(Vm vm) {}
}
