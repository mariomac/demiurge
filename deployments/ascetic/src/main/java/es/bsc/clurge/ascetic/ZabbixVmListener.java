package es.bsc.clurge.ascetic;

import es.bsc.clurge.ascetic.monitoring.ZabbixConnector;
import es.bsc.clurge.common.models.vms.VmDeployed;
import es.bsc.clurge.common.vmm.VmAction;
import es.bsc.clurge.common.vmm.VmManagerListener;

public class ZabbixVmListener implements VmManagerListener{
	private static final String ASCETIC_ZABBIX_SCRIPT_PATH = "/DFS/ascetic/vm-scripts/zabbix_agents.sh";


	@Override
	public void onVmDeployment(VmDeployed vm) {
		// If the monitoring system is Zabbix, then we need to call the Zabbix wrapper to initialize
		// the Zabbix agents. To register the VM we agreed to use the name <vmId>_<hostWhereTheVmIsDeployed>
		ZabbixConnector.registerVmInZabbix(vm.getId(), vm.getHostName(), vm.getIpAddress());

	}

	@Override
	public void onVmDestruction(VmDeployed vm) {
		ZabbixConnector.deleteVmFromZabbix(vm.getId(), vm.getHostName());
	}

	@Override
	public void onVmMigration(VmDeployed vm) {
		ZabbixConnector.migrateVmInZabbix(vm.getId(), vm.getIpAddress());
	}

	@Override
	public void onVmAction(VmDeployed vm, VmAction action) {

	}
}
