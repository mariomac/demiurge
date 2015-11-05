/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.clurge.ascetic.monitoring;

import es.bsc.clurge.domain.PhysicalHost;
import es.bsc.clurge.monit.PhysicalHostMonitoringInfo;

import java.util.Map;

/**
 * Status of a host monitored by Zabbix.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
class HostZabbix implements PhysicalHostMonitoringInfo {

    /* Keys to identify each metric in Zabbix.
       Note: The metrics used for the disk space are specific for the Ascetic project. Also, some metrics might not
       be available by default in Zabbix. I had to add some of them manually. */
    private static final String NUMBER_OF_CPUS_KEY = "system.cpu.num";
    private static final String SYSTEM_CPU_LOAD_KEY = "system.cpu.load[all,avg1]";
    private static final String TOTAL_MEMORY_BYTES_KEY = "vm.memory.size[total]";
    private static final String AVAILABLE_MEMORY_BYTES_KEY = "vm.memory.size[available]";
    private static final String TOTAL_DISK_BYTES_KEY = "vfs.fs.size[/var/lib/nova/instances,total]";
    private static final String USED_DISK_BYTES_KEY = "vfs.fs.size[/var/lib/nova/instances,used]";
    private static final String POWER_KEY = "power";

    private final int zabbixId; // Each host has an ID in Zabbix and this ID is not the hostname

	private final PhysicalHost host;
    /**
     * Class constructor.
     *
     * @param theHost the hostname
     */
    public HostZabbix(PhysicalHost theHost) {
		host = theHost;
        zabbixId = getZabbixId(theHost.getHostname());
        updateMetrics();
    }

    /**
     * Returns the Zabbix ID for a specific host given its hostname.
     *
     * @param hostname the hostname
     * @return the Zabbix ID
     */
    private int getZabbixId(String hostname) {
        if (!ZabbixConnector.hostIds.containsKey(hostname)) {
            throw new IllegalArgumentException("The host specified does not seem to be registered in Zabbix.");
        }
        return ZabbixConnector.hostIds.get(hostname);
    }

    /**
     * This function updates the metrics (assigned cpus, memory, etc.) of the host based on the information
     * received from Zabbix.
     */
    private void updateMetrics() {
        Map<String, Double> latestMetricValues = ZabbixConnector.getHostItems(zabbixId);

        /* If there is an error while trying to get data from Zabbix, some of the fields that I expect to be in
           the latestMetricValues map are not going to be there. Therefore, I need to check that they are not null */
        if (latestMetricValues.get(NUMBER_OF_CPUS_KEY) != null) {
            host.setTotalCpus(latestMetricValues.get(NUMBER_OF_CPUS_KEY).intValue());
        }
        if (latestMetricValues.get(TOTAL_MEMORY_BYTES_KEY) != null) {
            host.setTotalMemoryMb( (int) (latestMetricValues.get(TOTAL_MEMORY_BYTES_KEY) / (1024 * 1024)));
        }
        if (latestMetricValues.get(TOTAL_DISK_BYTES_KEY) != null) {
            host.setTotalDiskGb( latestMetricValues.get(TOTAL_DISK_BYTES_KEY) / (1024.0 * 1024 * 1024));
        }
        if (latestMetricValues.get(SYSTEM_CPU_LOAD_KEY) != null) {
            host.setAssignedCpus( latestMetricValues.get(SYSTEM_CPU_LOAD_KEY));
        }
        if (latestMetricValues.get(AVAILABLE_MEMORY_BYTES_KEY) != null) {
            host.setAssignedMemoryMb( host.getTotalMemoryMb() - latestMetricValues.get(AVAILABLE_MEMORY_BYTES_KEY) / (1024 * 1024));
        }
        if (latestMetricValues.get(USED_DISK_BYTES_KEY) != null) {
            host.setAssignedDiskGb( latestMetricValues.get(USED_DISK_BYTES_KEY) / (1024.0 * 1024 * 1024));
        }
        if (latestMetricValues.get(POWER_KEY) != null) {
            host.setCurrentPower( latestMetricValues.get(POWER_KEY));
        }
		//LogManager.getLogger(HostZabbix.class).trace(
		//		"Updated host metrics: " + toString()
		//);
    }

    @Override
    public void refresh() {
        updateMetrics();
    }

	@Override
	public String toString() {
		return "HostZabbix{" +
				"zabbixId=" + zabbixId +
				"} " + super.toString();
	}
}
