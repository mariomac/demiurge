package es.bsc.clurge.vmm.generic;

import es.bsc.clurge.Clurge;
import es.bsc.clurge.PhysicalHostManager;
import es.bsc.clurge.domain.PhysicalHost;

import java.util.*;

/**
 * Created by mmacias on 5/11/15.
 */
public class GenericPhysicalHostManager implements PhysicalHostManager {


	private final Map<String, PhysicalHost> hosts = new HashMap<>();

	@Override
	public void addHost(PhysicalHost h) {
		hosts.put(h.getHostname(),h);
	}

	/**
	 * Returns the hosts of the infrastructure.
	 *
	 * @return the list of hosts
	 */
	@Override
	public List<PhysicalHost> getHosts() {
		refreshHostsMonitoringInfo();
		return Collections.unmodifiableList(new ArrayList<PhysicalHost>(hosts.values()));
	}

	/**
	 * Returns a host by hostname.
	 *
	 * @param hostname the hostname
	 * @return the host
	 */
	@Override
	public PhysicalHost getHost(String hostname) {
		PhysicalHost host = hosts.get(hostname);
		if(host != null) {
			Clurge.INSTANCE.getMonitoringManager().updateMonitoringInfo(host);
		}
		return host;
	}

	/**
	 * Simulates pressing the power button of a host
	 * @param hostname the hostname
	 */
	@Override
	public void pressHostPowerButton(String hostname) {
		PhysicalHost host = hosts.get(hostname);
		if(host == null) throw new IllegalArgumentException(hostname + " does not exists");
		host.pressPowerButton();
	}

	/**
	 * Refresh the data for all the hosts. This operation can be costly because it needs to query the
	 * monitoring infrastructure (Ganglia, Zabbix, etc.)
	 */
	void refreshHostsMonitoringInfo() {
		for (PhysicalHost host: hosts.values()) {
			Clurge.INSTANCE.getMonitoringManager().updateMonitoringInfo(host);
		}
	}


}
