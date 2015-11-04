package es.bsc.clurge.monit.ganglia;

import es.bsc.clurge.common.monit.Host;
import es.bsc.clurge.common.monit.HostsMonitoringManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mmacias on 4/11/15.
 */
public class HostsGangliaManager implements HostsMonitoringManager {
	private static Map<String, Host> hosts = new HashMap<>(); // List of hosts already created

	@Override
	public void generateHosts(String[] hostNames) {

	}

	@Override
	public Host getHost(String hostname) {
		// If the host already exists, return it
		Host host = hosts.get(hostname);
		if (host != null) {
			host.refreshMonitoringInfo();
			return host;
		}

		// If the host does not already exist, create and return it.
		// If the type is Fake, this switch will not be called, because all the fake hosts are created beforehand.
		// This is because we do not want to have to read the description file more than once.
		Host newHost = new HostGanglia(hostname);

		hosts.put(hostname, newHost);
		return newHost;	}
}
