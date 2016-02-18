package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.config;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

public class VmmConfig {

    public final List<String> hosts = new ArrayList<>();
    public final String vmmUrl;
    public final String hostForClients;

    public VmmConfig(List<String> hosts, String vmmUrl, String hostForClients) {
        this.hosts.addAll(hosts);
        this.vmmUrl = vmmUrl;
        this.hostForClients = hostForClients;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public String getVmmUrl() {
        return vmmUrl;
    }

    public String getHostForClients() {
        return hostForClients;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("hosts", hosts)
                .add("vmmUrl", vmmUrl)
                .add("hostForClients", hostForClients)
                .toString();
    }

}
