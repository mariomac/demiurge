package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud;

import com.google.common.base.MoreObjects;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.config.VmmConfig;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.contextualizers.MediaStreamingContextualizer;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.contextualizers.WebSearchJmeterContextualizer;
import es.bsc.demiurge.cloudsuiteperformancedriver.logging.CloudSuiteSchedulerLogger;
import es.bsc.demiurge.cloudsuiteperformancedriver.logging.Deployment;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.CloudSuiteBenchmark;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.Host;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.PlacementDecision;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.VmSize;
import es.bsc.demiurge.cloudsuiteperformancedriver.vmmclient.models.Node;
import es.bsc.demiurge.cloudsuiteperformancedriver.vmmclient.models.Vm;
import es.bsc.demiurge.cloudsuiteperformancedriver.vmmclient.vmm.VmManagerClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
import es.bsc.vmmclient.models.Node;
import es.bsc.vmmclient.models.Vm;
import es.bsc.vmmclient.vmm.VmManagerClient;*/

public class VmmAdapter {

    private final VmManagerClient vmm;
    private final List<String> hostsToBeUsed = new ArrayList<>();
    private final String hostForClients;
    private final ImageRepo imageRepo;

    private static final int WEB_SEARCH_JMETER_CLIENT_CPUS = 4;
    private static final int WEB_SEARCH_JMETER_CLIENT_RAMGB = 4;
    private static final int WEB_SEARCH_JMETER_CLIENT_DISKGB = 10;

    private static final int MEDIA_STREAMING_CLIENT_CPUS = 4;
    private static final int MEDIA_STREAMING_CLIENT_RAMGB = 4;
    private static final int MEDIA_STREAMING_CLIENT_DISKGB = 10;

    private static final int WEB_SERVING_CLIENT_CPUS = 4;
    private static final int WEB_SERVING_CLIENT_RAMGB = 4;
    private static final int WEB_SERVING_CLIENT_DISKGB = 10;

    public VmmAdapter(VmmConfig vmmConfig, ImageRepo imageRepo) {
        vmm = new VmManagerClient(vmmConfig.getVmmUrl());
        hostsToBeUsed.addAll(vmmConfig.getHosts());
        hostForClients = vmmConfig.getHostForClients();
        this.imageRepo = imageRepo;
    }

    public List<String> deployBenchmark(CloudSuiteBenchmark benchmark, Map<String, String> bootScripts,
                                        PlacementDecision placementDecision, Modeller modeller) {
        return deploy(benchmark, placementDecision.getVmSize(), placementDecision.getHost(), bootScripts, modeller);
    }

    public List<Host> getHostsToBeUsed() {
        List<Host> result = new ArrayList<>();
        for (Host host : getAllHosts()) {
            if (hostsToBeUsed.contains(host.getHostname())) {
                result.add(host);
            }
        }
        return result;
    }

    public void destroy(String vmId) {
        vmm.destroyVm(vmId);
        CloudSuiteSchedulerLogger.logVmDestroy(vmId);
    }

    private List<String> deploy(CloudSuiteBenchmark benchmark, VmSize vmSize, Host host,
                                Map<String, String> bootScripts, Modeller modeller) {
        List<Vm> vms = new ArrayList<>();

        if (imageRepo.getImages(benchmark).size() == 1) {
            vms.add(new Vm(benchmark.toString(), imageRepo.getImages(benchmark).get(0), vmSize.getCpus(),
                    vmSize.getRamGb()*1024, vmSize.getDiskGb(), 0, bootScripts.get("default"),
                    null, null, null, host.getHostname()));
        }
        else if (benchmark == CloudSuiteBenchmark.MEDIA_STREAMING) {
            vms.add(new Vm(benchmark.toString(), imageRepo.getImages(benchmark).get(1), vmSize.getCpus(),
                    vmSize.getRamGb()*1024, vmSize.getDiskGb(), 0, bootScripts.get("default"),
                    null, null, null, host.getHostname()));
            vms.add(new Vm(benchmark.toString(), imageRepo.getImages(benchmark).get(0), MEDIA_STREAMING_CLIENT_CPUS,
                    MEDIA_STREAMING_CLIENT_RAMGB*1024, MEDIA_STREAMING_CLIENT_DISKGB, 0, "",
                    null, null, null, hostForClients));
        }

        else if (benchmark == CloudSuiteBenchmark.WEB_SEARCH) {
            vms.add(new Vm(benchmark.toString(), imageRepo.getImages(benchmark).get(0), vmSize.getCpus(),
                    vmSize.getRamGb()*1024, vmSize.getDiskGb(), 0, bootScripts.get("default"),
                    null, null, null, host.getHostname()));
            vms.add(new Vm(benchmark.toString(), imageRepo.getImages(benchmark).get(1), WEB_SEARCH_JMETER_CLIENT_CPUS,
                    WEB_SEARCH_JMETER_CLIENT_RAMGB*1024, WEB_SEARCH_JMETER_CLIENT_DISKGB, 0, bootScripts.get("client"),
                    null, null, null, hostForClients));
        }

        else if (benchmark == CloudSuiteBenchmark.WEB_SERVING) {
            vms.add(new Vm(benchmark.toString(), imageRepo.getImages(benchmark).get(0), WEB_SERVING_CLIENT_CPUS,
                    WEB_SERVING_CLIENT_RAMGB*1024, WEB_SERVING_CLIENT_DISKGB, 0, bootScripts.get("client"),
                    null, null, null, hostForClients));
            vms.add(new Vm(benchmark.toString(), imageRepo.getImages(benchmark).get(1), vmSize.getCpus(),
                    vmSize.getRamGb()*1024, vmSize.getDiskGb(), 0, bootScripts.get("frontend"),
                    null, null, null, host.getHostname()));
            vms.add(new Vm(benchmark.toString(), imageRepo.getImages(benchmark).get(2), vmSize.getCpus(),
                    vmSize.getRamGb()*1024, vmSize.getDiskGb(), 0, bootScripts.get("backend"),
                    null, null, null, host.getHostname()));
        }

        // Log deployment
        CloudSuiteSchedulerLogger.logDeployment(new Deployment(
                System.currentTimeMillis(),
                benchmark.name(),
                host.getHostname(),
                vmSize,
                modeller.getBenchmarkPerformance(benchmark, host.getHostname(), vmSize),
                modeller.getBenchmarkAvgPower(benchmark, host.getHostname(), vmSize)));

        final List<String> vmIds = vmm.deployVms(vms);

        // For some benchmarks we need to perform some actions before they can start
        if (benchmark == CloudSuiteBenchmark.MEDIA_STREAMING) {
            // Execute in separated thread. Otherwise, the execution will be stuck performing the post-deployment
            // actions and the program will not be able to continue the execution (for example to schedule the
            // deletion of the VMs
            new Thread(new Runnable() {
                public void run() {
                    // Wait some time to make sure that the VMs have time to receive an IP
                    try {
                        Thread.sleep(60*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    performPostDeploymentActionMediaStreaming(
                            vmm.getVm(vmIds.get(0)).getIpAddress(),
                            vmm.getVm(vmIds.get(1)).getIpAddress());
                }
            }).start();
        }

        else if (benchmark == CloudSuiteBenchmark.WEB_SEARCH) {
            // Execute in separate thread for same reason
            new Thread(new Runnable() {
                public void run() {
                    // Wait some time to make sure that the VMs have time to receive an IP
                    try {
                        Thread.sleep(60*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    performPostDeploymentActionWebSearch(
                            vmm.getVm(vmIds.get(0)).getIpAddress(),
                            vmm.getVm(vmIds.get(1)).getIpAddress());
                }
            }).start();
        }

        else if (benchmark == CloudSuiteBenchmark.WEB_SERVING) {
            performPostDeploymentActionWebServing();
        }

        return vmIds;
    }

    private void performPostDeploymentActionMediaStreaming(String ipClientVm, String ipServerVm) {
        // Wait a couple of minutes to make sure that the VMs are ready
        try {
            Thread.sleep(2 * 60 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MediaStreamingContextualizer.startMediaStreamingClient(ipClientVm, ipServerVm);
    }

    private void performPostDeploymentActionWebSearch(String ipServerVm, String ipClientVm) {
        // Wait a couple of minutes to make sure that the VMs are ready
        try {
            Thread.sleep(2*60*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebSearchJmeterContextualizer.startWebSearch(ipServerVm);
        WebSearchJmeterContextualizer.startJmeterClient(ipClientVm, ipServerVm);
    }

    private void performPostDeploymentActionWebServing() {
        // TODO
    }

    private List<Host> getAllHosts() {
        List<Host> result = new ArrayList<>();
        for (Node node : vmm.getNodes()) {
            result.add(new Host(
                    node.getHostname(),
                    node.getTotalCpus(),
                    (int) node.getTotalMemoryMb()/1024,
                    (int) node.getTotalDiskGb(),
                    (int) node.getAssignedCpus(),
                    (int) node.getAssignedMemoryMb()/1024,
                    (int) node.getAssignedDiskGb()));
        }
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("vmm", vmm)
                .add("hostsToBeUsed", hostsToBeUsed)
                .add("imageRepo", imageRepo)
                .toString();
    }

}
