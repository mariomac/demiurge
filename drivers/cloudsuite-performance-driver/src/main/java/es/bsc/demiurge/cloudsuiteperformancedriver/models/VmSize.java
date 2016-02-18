package es.bsc.demiurge.cloudsuiteperformancedriver.models;

import com.google.common.base.MoreObjects;

public class VmSize {

    private final int cpus;
    private final int ramGb;
    private final int diskGb;

    public VmSize(int cpus, int ramGb, int diskGb) {
        this.cpus = cpus;
        this.ramGb = ramGb;
        this.diskGb = diskGb;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VmSize)) return false;

        VmSize vmSize = (VmSize) o;

        if (cpus != vmSize.cpus) return false;
        if (ramGb != vmSize.ramGb) return false;
        return diskGb == vmSize.diskGb;

    }

    @Override
    public int hashCode() {
        int result = cpus;
        result = 31 * result + ramGb;
        result = 31 * result + diskGb;
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("cpus", cpus)
                .add("ramGb", ramGb)
                .add("diskGb", diskGb)
                .toString();
    }

}
