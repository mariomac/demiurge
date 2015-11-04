package es.bsc.clurge.ascetic;

import es.bsc.clurge.ascetic.message_queue.MessageQueue;
import es.bsc.clurge.models.vms.VmDeployed;
import es.bsc.clurge.vmm.VmAction;
import es.bsc.clurge.vmm.VmManagerListener;

/**
 * Created by mmacias on 4/11/15.
 */
public class MessageQueueVmListener implements VmManagerListener {
	@Override
	public void onVmDeployment(VmDeployed vm) {
		MessageQueue.publishMessageVmDeployed(vm);
	}

	@Override
	public void onVmDestruction(VmDeployed vm) {
		MessageQueue.publishMessageVmDestroyed(vm);

	}

	@Override
	public void onVmMigration(VmDeployed vm) {

	}

	@Override
	public void onVmAction(VmDeployed vm, VmAction action) {
		MessageQueue.publishMessageVmChangedState(vm, action.getCamelCase());
	}
}
