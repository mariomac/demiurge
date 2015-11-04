package es.bsc.clurge.ascetic.monitoring;

import es.bsc.clurge.monit.Host;
import es.bsc.clurge.monit.HostsMonitoringManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mmacias on 4/11/15.
 */

// TO DO: MAKE A GENERIC HOSTS MONITORING MANAGER AND ONLY HAVE AN MONITORING-TAILORED HOST INSTANTIATOR
public class HostsZabbixManager implements HostsMonitoringManager {
	private static Map<String, Host> hosts = new HashMap<>(); // List of hosts already created

	@Override
	public void generateHosts(String[] hostNames) {
		for(String name : hostNames) {
			// TO DO: PUT THIS IN A TRY/CATCH
			hosts.put(name, new HostZabbix(name));
		}
	}

	@Override
	public Host getHost(String hostname) {
		Host host = hosts.get(hostname);
		if (host != null) {
			host.refreshMonitoringInfo();
			return host;
		}
		return null;
	}
}
