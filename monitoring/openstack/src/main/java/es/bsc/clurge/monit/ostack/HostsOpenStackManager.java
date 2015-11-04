package es.bsc.clurge.monit.ostack;

import es.bsc.clurge.monit.Host;
import es.bsc.clurge.monit.HostsMonitoringManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mmacias on 4/11/15.
 */
public class HostsOpenStackManager implements HostsMonitoringManager {
	private static Map<String, Host> hosts = new HashMap<>(); // List of hosts already created

	@Override
	public void generateHosts(String[] hostNames) {
		for(String hostName : hostNames) {
			hosts.put(hostName, new HostOpenStack(hostName));
		}

	}

	@Override
	public Host getHost(String hostname) {
		// If the host already exists, return it
		Host host = hosts.get(hostname);
		if (host != null) {
			host.refreshMonitoringInfo();
			return host;
		}
		return null;

	}
}
