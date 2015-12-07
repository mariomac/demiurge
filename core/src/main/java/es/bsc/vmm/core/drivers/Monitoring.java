package es.bsc.vmm.core.drivers;

import es.bsc.vmm.core.monitoring.hosts.Host;

/**
 * @author Mario Macías http://github.com/mariomac
 */
public interface Monitoring<T extends Host> {
	T createHost(String hostName);
}
