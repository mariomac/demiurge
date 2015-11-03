package es.bsc.clurge.ascetic;

import es.bsc.clurge.ascetic.monitoring.ZabbixConnector;
import es.bsc.clurge.models.vms.VmDeployed;
import es.bsc.clurge.vmm.VmManagerListener;

public class ZabbixVmListener implements VmManagerListener{


	@Override
	public void onVmDeployment(VmDeployed vm) {

	}

	@Override
	public void onVmDestruction(VmDeployed vm) {
		ZabbixConnector.deleteVmFromZabbix(vm.getId(), vm.getHostName());
	}

	@Override
	public void onVmMigration(VmDeployed vm) {

	}
}
