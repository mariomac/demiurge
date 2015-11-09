package es.bsc.clurge;

import es.bsc.clurge.cloudmw.CloudMiddleware;
import es.bsc.clurge.domain.PhysicalHost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mmacias on 5/11/15.
 */
public interface PhysicalHostManager {

	void addHost(PhysicalHost h);

	/**
	 * Returns the hosts of the infrastructure.
	 *
	 * @return the list of hosts
	 */
	List<PhysicalHost> getHosts();

	/**
	 * Returns a host by hostname.
	 *
	 * @param hostname the hostname
	 * @return the host
	 */
	PhysicalHost getHost(String hostname);

	/**
	 * Simulates pressing the power button of a host
	 * @param hostname the hostname
	 */
	void pressHostPowerButton(String hostname);
}
