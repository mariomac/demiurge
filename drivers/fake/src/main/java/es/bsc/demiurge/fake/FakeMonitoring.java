package es.bsc.demiurge.fake;

import es.bsc.demiurge.core.drivers.Monitoring;

/**
 * @author Mario Macías (http://github.com/mariomac)
 */
public class FakeMonitoring implements Monitoring<HostFake> {
    @Override
    public HostFake createHost(String hostName) {
        return new HostFake(hostName);
    }

    @Override
    public HostFake createHost(String hostname, int totalCpus, double totalMemoryMb, double totalDiskGb) {
        return new HostFake(hostname, totalCpus, totalMemoryMb, totalDiskGb);
    }
}
