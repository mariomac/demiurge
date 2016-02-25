package es.bsc.demiurge.renewit.utils;

import es.bsc.demiurge.core.clopla.domain.Host;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public  class CloudsuiteUtils {

    public static es.bsc.demiurge.cloudsuiteperformancedriver.models.Host convertClusterHostToPerformanceHost(Host host){

        return new es.bsc.demiurge.cloudsuiteperformancedriver.models.Host(host.getHostname(), host.getNcpus(), (int) host.getRamMb()*1024, (int) host.getDiskGb(), 0, 0, 0);
        //return new es.bsc.demiurge.cloudsuiteperformancedriver.models.Host("bscgrid30", 32, 24*1024, 1500, 0, 0, 0);

    }
}
