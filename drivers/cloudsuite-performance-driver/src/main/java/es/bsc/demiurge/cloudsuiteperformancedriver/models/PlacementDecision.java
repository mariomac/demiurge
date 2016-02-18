package es.bsc.demiurge.cloudsuiteperformancedriver.models;

import com.google.common.base.MoreObjects;

public class PlacementDecision {

    private final Host host;
    private final VmSize vmSize;

    public PlacementDecision(Host host, VmSize vmSize) {
        this.host = host;
        this.vmSize = vmSize;
    }

    public Host getHost() {
        return host;
    }

    public VmSize getVmSize() {
        return vmSize;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("host", host)
                .add("vmSize", vmSize)
                .toString();
    }

}
