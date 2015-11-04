package es.bsc.clurge.ascetic.monitoring;

import es.bsc.clurge.ascetic.monitoring.ZabbixConnector;
import es.bsc.clurge.models.vms.Vm;
import es.bsc.clurge.models.vms.VmDeployed;
import es.bsc.clurge.vmm.VmAction;
import es.bsc.clurge.vmm.VmManagerListener;
import org.apache.logging.log4j.LogManager;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class ZabbixVmListener implements VmManagerListener{
	private static final String ASCETIC_ZABBIX_SCRIPT_PATH = "/DFS/ascetic/vm-scripts/zabbix_agents.sh";

	// TODO: I'M NOT SURE IF THAT WAS REMOVED IN Y2: ask to ascetic guys
	@Override
	public void onPreVmDeployment(Vm vm) {
		try {

			Path zabbixAgentsScriptPath = FileSystems.getDefault().getPath(ASCETIC_ZABBIX_SCRIPT_PATH);
			if (Files.exists(zabbixAgentsScriptPath)) {
				vm.setInitScript(ASCETIC_ZABBIX_SCRIPT_PATH);
			}
			else { // This is for when I perform tests locally and do not have access to the script (and
				// do not need it)
				vm.setInitScript(null);
			}
		} catch(Exception e) {
			LogManager.getLogger(ZabbixVmListener.class).error(e.getMessage(),e);
		}
	}

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
