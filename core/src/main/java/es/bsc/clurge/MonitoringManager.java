package es.bsc.clurge;

import es.bsc.clurge.domain.PhysicalHost;

/**
 * Created by mmacias on 5/11/15.
 */
public interface MonitoringManager {
	void generateHosts(String[] hostNames);
	void updateMonitoringInfo(PhysicalHost host);
}
