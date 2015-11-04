package es.bsc.clurge.monit.ganglia;

import es.bsc.clurge.monit.HostsMonitoringManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mmacias on 4/11/15.
 */
public class HostsGangliaManager implements HostsMonitoringManager {
	private static Map<String, es.bsc.clurge.monit.Host> hosts = new HashMap<>(); // List of hosts already created

	@Override
	public void generateHosts(String[] hostNames) {
		for(String name : hostNames) {
			hosts.put(name, new HostGanglia(name));
		}

	}

	@Override
	public es.bsc.clurge.monit.Host getHost(String hostname) {
		// If the host already exists, return it
		es.bsc.clurge.monit.Host host = hosts.get(hostname);
		if (host != null) {
			host.refreshMonitoringInfo();
			return host;
		}
		return null;
	}
}
