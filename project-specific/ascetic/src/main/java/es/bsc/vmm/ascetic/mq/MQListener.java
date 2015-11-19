package es.bsc.vmm.ascetic.mq;

/**
 * Created by mmacias on 19/11/15.
 */
public class MQListener implements VmmListener {
	@Override
	void onVmDeployment(VmDeployed vm) {
		MessageQueue.publishMessageVmDeployed(vm);
	}
	@Override
	void onVmDestruction(VmDeployed vm) {
		MessageQueue.publishMessageVmDestroyed(vm);
	}
	@Override
	void onVmMigration(VmDeployed vm) {}
	@Override
	void onVmAction(VmDeployed vm, VmAction action) {
		MessageQueue.publishMessageVmChangedState(vm, action);

	}

	@Override
	void onPreVmDeployment(Vm vm) {}
}
