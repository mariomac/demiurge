package es.bsc.demiurge.cloudsuiteperformancedriver.workloads;

import com.google.common.base.MoreObjects;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.Cloud;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.Modeller;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.CloudSuiteBenchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class WorkloadExecutor {

    private final Cloud cloud;
    private final Modeller modeller;

    public WorkloadExecutor(Cloud cloud, Modeller modeller) {
        this.cloud = cloud;
        this.modeller = modeller;
    }

    public void executeWorkload(Workload workload) {
        ScheduledExecutorService scheduledExecutorService =
                Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

        List<ScheduledFuture> scheduledDeployments = scheduleVmDeployments(workload, scheduledExecutorService);
        scheduleVMDestroys(workload, scheduledExecutorService, scheduledDeployments);

        scheduledExecutorService.shutdown();
    }

    private List<ScheduledFuture> scheduleVmDeployments(Workload workload,
                                                        ScheduledExecutorService scheduledExecutorService) {
        List<ScheduledFuture> scheduledDeployments = new ArrayList<>();
        for(final BenchmarkExecution benchmarkExecution : workload.getBenchmarkExecutionList()) {
            scheduledDeployments.add(
                    scheduledExecutorService.schedule(
                            new Callable() {
                                public List<String> call() throws Exception {
                                    return cloud.deployBenchmark(
                                            getBenchmarkFromName(benchmarkExecution.getBenchmark()),
                                            benchmarkExecution.getPerformance(),
                                            modeller);
                                }
                            },
                            benchmarkExecution.getTimeStartSeconds(),
                            TimeUnit.SECONDS));
        }
        return scheduledDeployments;
    }

    private void scheduleVMDestroys(Workload workload, ScheduledExecutorService scheduledExecutorService,
                                    List<ScheduledFuture> scheduledDeployments) {
        for (int i = 0; i < scheduledDeployments.size(); ++i) {
            BenchmarkExecution benchmarkExecution = workload.getBenchmarkExecutionList().get(i);
            List<String> vmIds = new ArrayList<>();
            try {
                vmIds = (List<String>) scheduledDeployments.get(i).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            for (final String vmId : vmIds) {
                scheduledExecutorService.schedule(
                        new Callable() {
                            public Object call() throws Exception {
                                cloud.destroyVm(vmId);
                                return "";
                            }
                        },
                        benchmarkExecution.getTimeStartSeconds() + benchmarkExecution.getRuntimeSeconds(),
                        TimeUnit.SECONDS);
            }
        }
    }

    // This should be done in a better way, but it's ok for now
    private CloudSuiteBenchmark getBenchmarkFromName(String name) {
        switch (name) {
            case "data_analytics":
                return CloudSuiteBenchmark.DATA_ANALYTICS;
            case "data_caching":
                return CloudSuiteBenchmark.DATA_CACHING;
            case "data_serving":
                return CloudSuiteBenchmark.DATA_SERVING;
            case "graph_analytics":
                return CloudSuiteBenchmark.GRAPH_ANALYTICS;
            case "media_streaming":
                return CloudSuiteBenchmark.MEDIA_STREAMING;
            case "software_testing":
                return CloudSuiteBenchmark.SOFTWARE_TESTING;
            case "web_search":
                return CloudSuiteBenchmark.WEB_SEARCH;
            case "web_serving":
                return CloudSuiteBenchmark.WEB_SERVING;
        }
        throw new RuntimeException("The benchmark name specified is not correct.");
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("cloud", cloud)
                .toString();
    }

}
