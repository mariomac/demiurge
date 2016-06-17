package es.bsc.demiurge.openstackjclouds;

import es.bsc.demiurge.core.drivers.Monitoring;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class OpenStackMonitoring implements Monitoring<HostOpenStack> {
    private static OpenStackJclouds openStackJclouds;

        @Override
        public HostOpenStack createHost(String hostName) {
            // Initialize JClouds variables
            openStackJclouds = new OpenStackJclouds(
                    new String[]{ "bscgrid28", "bscgrid29" , "bscgrid30", "bscgrid31"}, // I am ignoring the sec. groups and hosts
                    new String[]{ "" });

            return new HostOpenStack(hostName, openStackJclouds);
        }

        @Override
        public HostOpenStack createHost(String hostname, int totalCpus, double totalMemoryMb, double totalDiskGb) {
            return null;
        }
    }

