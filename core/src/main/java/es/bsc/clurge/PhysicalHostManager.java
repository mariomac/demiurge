package es.bsc.clurge;

import es.bsc.clurge.cloudmw.CloudMiddleware;
import es.bsc.clurge.domain.PhysicalHost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mmacias on 5/11/15.
 */
public class PhysicalHostManager {

	MonitoringManager monitoringManager;
	CloudMiddleware cloudMiddleware;

	private final List<PhysicalHost> hosts = new ArrayList<>();

	public void addHost(PhysicalHost h) {
		hosts.add(h);
	}

	/**
	 * Returns the hosts of the infrastructure.
	 *
	 * @return the list of hosts
	 */
	public List<PhysicalHost> getHosts() {
		refreshHostsMonitoringInfo();
		return Collections.unmodifiableList(hosts);
	}

	/**
	 * Returns a host by hostname.
	 *
	 * @param hostname the hostname
	 * @return the host
	 */
	public PhysicalHost getHost(String hostname) {
		for (PhysicalHost host: hosts) {
			if (hostname.equals(host.getHostname())) {
				host.getMonitoringInfo().refresh();
				return host;
			}
		}
		return null;
	}

	/**
	 * Simulates pressing the power button of a host
	 * @param hostname the hostname
	 */
	public void pressHostPowerButton(String hostname) {
		for (PhysicalHost host: hosts) {
			if (hostname.equals(host.getHostname())) {
				host.pressPowerButton();
			}
		}
	}

	/**
	 * Refresh the data for all the hosts. This operation can be costly because it needs to query the
	 * monitoring infrastructure (Ganglia, Zabbix, etc.)
	 */
	private void refreshHostsMonitoringInfo() {
		for (PhysicalHost host: hosts) {
			host.getMonitoringInfo().refresh();
		}
	}


}
