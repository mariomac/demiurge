package es.bsc.autonomicbenchmarks.models;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class VmAutonomic {
    private final String id;
    private String name;
    private int cpus;
    private int ramGb;
    private int diskGb;
    private final String ipAddress;
    private String hostName;



    public VmAutonomic(String id, String name, String ipAddress, int cpus, int ramGb, int diskGb, String hostName) {
        this.id = id;
        this.name = name;
        this.ipAddress = ipAddress;
        this.cpus = cpus;
        this.ramGb = ramGb;
        this.diskGb = diskGb;
        this.hostName = hostName;
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

    public String getIpAddress() {
        return ipAddress;
    }

    public String getHostName() {
        return hostName;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

}
