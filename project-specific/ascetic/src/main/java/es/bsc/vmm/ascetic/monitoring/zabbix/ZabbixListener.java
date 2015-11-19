package es.bsc.vmm.ascetic.monitoring.zabbix;

import es.bsc.vmm.core.drivers.VmmListener;

/**
 * Created by mmacias on 19/11/15.
 */
public class ZabbixListener implements VmmListener {
	private Logger log = ;
	@Override
	void onVmDeployment(VmDeployed vm) {
		ZabbixConnector.registerVmInZabbix(vm.getId(), vm.getHostName(), vm.getIpAddress());

	}
	@Override
	void onVmDestruction(VmDeployed vm) {
		try {
			ZabbixConnector.deleteVmFromZabbix(vm.getId(), vm.getHostName());
		} catch(Exception e) {
			log.error(e.getMessage(),e);
		}
	}
	@Override
	void onVmMigration(VmDeployed vm) {
		ZabbixConnector.migrateVmInZabbix(vm.getId(), vm.getIpAddress());
	}
	@Override
	void onVmAction(VmDeployed vm, VmAction action) {}

	@Override
	void onPreVmDeployment(Vm vm) {}
}
