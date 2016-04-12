package es.bsc.demiurge.cloudsuiteperformancedriver.models;

import com.google.common.base.MoreObjects;

public class Host {

    private final String hostname;
    private final int cpus;
    private final int ramGb;
    private final int diskGb;
    private final int usedCpus;
    private final int usedRamGb;
    private final int usedDiskGb;
    private final String type;

    public Host(String hostname, int cpus, int ramGb, int diskGb, int usedCpus, int usedRamGb, int usedDiskGb) {
        this.hostname = hostname;
        this.cpus = cpus;
        this.ramGb = ramGb;
        this.diskGb = diskGb;
        this.usedCpus = usedCpus;
        this.usedRamGb = usedRamGb;
        this.usedDiskGb = usedDiskGb;
        this.type = null;
    }

    public Host(String hostname, int cpus, int ramGb, int diskGb, int usedCpus, int usedRamGb, int usedDiskGb, String type) {
        this.hostname = hostname;
        this.cpus = cpus;
        this.ramGb = ramGb;
        this.diskGb = diskGb;
        this.usedCpus = usedCpus;
        this.usedRamGb = usedRamGb;
        this.usedDiskGb = usedDiskGb;
        this.type = type;
    }

    public boolean hasEnoughSpaceToHost(VmSize vmSize) {
        return (cpus - usedCpus) >= vmSize.getCpus()
                && (ramGb - usedRamGb) >= vmSize.getRamGb()
                && (diskGb - usedDiskGb) >= vmSize.getDiskGb();
    }

    public String getHostname() {
        return hostname;
    }

    public int getCpus() {
        return cpus;
    }

    public int getRamGb() {
        return ramGb;
    }

    public int getDiskGb() {
        return diskGb;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("hostname", hostname)
                .add("cpus", cpus)
                .add("ramGb", ramGb)
                .add("diskGb", diskGb)
                .add("usedCpus", usedCpus)
                .add("usedRamGb", usedRamGb)
                .add("usedDiskGb", usedDiskGb)
                .toString();
    }

}
