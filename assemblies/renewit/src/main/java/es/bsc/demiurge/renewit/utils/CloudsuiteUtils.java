package es.bsc.demiurge.renewit.utils;

import es.bsc.demiurge.core.clopla.domain.Host;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public  class CloudsuiteUtils {

    public static es.bsc.demiurge.cloudsuiteperformancedriver.models.Host convertClusterHostToPerformanceHost(Host host){

        return new es.bsc.demiurge.cloudsuiteperformancedriver.models.Host(host.getHostname(), host.getNcpus(), (int) host.getRamMb()*1024, (int) host.getDiskGb(), 0, 0, 0, host.getType());
        //return new es.bsc.demiurge.cloudsuiteperformancedriver.models.Host("bscgrid30", 32, 24*1024, 1500, 0, 0, 0);

    }

    public static es.bsc.demiurge.cloudsuiteperformancedriver.models.Host convertVMMHostToPerformanceHost(es.bsc.demiurge.core.monitoring.hosts.Host host) {
        return new es.bsc.demiurge.cloudsuiteperformancedriver.models.Host(host.getHostname(), host.getTotalCpus(), (int) host.getTotalMemoryMb()*1024, (int) host.getTotalDiskGb(), (int) host.getAssignedCpus(), (int) host.getAssignedMemoryMb()*1024, (int) host.getAssignedDiskGb(), host.getType());
    }
}
