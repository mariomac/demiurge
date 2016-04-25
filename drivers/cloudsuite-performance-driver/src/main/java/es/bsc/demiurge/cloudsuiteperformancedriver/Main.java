package es.bsc.demiurge.cloudsuiteperformancedriver;

import com.google.gson.Gson;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.Cloud;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.ImageRepo;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.Modeller;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.VmmAdapter;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.config.ImageRepoConfig;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.config.VmmConfig;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.config.models.ModelsConfig;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.schedulers.PerfAndEnergyAwareScheduler;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.schedulers.Scheduler;
import es.bsc.demiurge.cloudsuiteperformancedriver.utils.Utils;
import es.bsc.demiurge.cloudsuiteperformancedriver.workloads.Workload;
import es.bsc.demiurge.cloudsuiteperformancedriver.workloads.WorkloadExecutor;

public class Main {

    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        ImageRepoConfig imagesConfig =
                gson.fromJson(Utils.readFile("imageRepoConfig.json"), ImageRepoConfig.class);
        ImageRepo imageRepo = new ImageRepo(imagesConfig);
        Modeller modeller = new Modeller(gson.fromJson(Utils.readFile("modelsConfig.json"), ModelsConfig.class));

        Scheduler scheduler = new PerfAndEnergyAwareScheduler(modeller);
        VmmAdapter vmmAdapter = new VmmAdapter(gson.fromJson(Utils.readFile("cloudConfig.json"), VmmConfig.class),
                imageRepo);
        Cloud cloud = new Cloud(scheduler, vmmAdapter);

        WorkloadExecutor workloadExecutor = new WorkloadExecutor(cloud, modeller);
        workloadExecutor.executeWorkload(gson.fromJson(Utils.readFile("workload.json"), Workload.class));
/*
        //bscgrid30
        VmSize size = new VmSize(8,32,150);
        String type = "bscgrid27";
        System.out.println("Max: "  +type);
        double perf = modeller.getPerformanceFromVmSize(size, CloudSuiteBenchmark.DATA_ANALYTICS, type);
        System.out.println("DATA_ANALYTICS: " + perf);

         perf = modeller.getPerformanceFromVmSize(size, CloudSuiteBenchmark.DATA_CACHING, type);
        System.out.println("DATA_CACHING: " + perf);

         perf = modeller.getPerformanceFromVmSize(size, CloudSuiteBenchmark.DATA_SERVING, type);
        System.out.println("DATA_SERVING: " + perf);

         perf = modeller.getPerformanceFromVmSize(size, CloudSuiteBenchmark.GRAPH_ANALYTICS, type);
        System.out.println("GRAPH_ANALYTICS: " + perf);

         perf = modeller.getPerformanceFromVmSize(size, CloudSuiteBenchmark.SOFTWARE_TESTING, type);
        System.out.println("SOFTWARE_TESTING: " + perf);

        perf = modeller.getPerformanceFromVmSize(size, CloudSuiteBenchmark.MEDIA_STREAMING, type);
        System.out.println("MEDIA_STREAMING: " + perf);

        perf = modeller.getPerformanceFromVmSize(size, CloudSuiteBenchmark.WEB_SEARCH, type);
        System.out.println("WEB_SEARCH: " + perf);

        perf = modeller.getPerformanceFromVmSize(size, CloudSuiteBenchmark.WEB_SERVING, type);
        System.out.println("WEB_SERVING: " + perf);


        //bscgrid31

         size = new VmSize(0,0,0);
        type = "bscgrid27";
        System.out.println("\n Min:" + type);
        perf = modeller.getPerformanceFromVmSize(size, CloudSuiteBenchmark.DATA_ANALYTICS, type);
        System.out.println("DATA_ANALYTICS: " + perf);

        perf = modeller.getPerformanceFromVmSize(size, CloudSuiteBenchmark.DATA_CACHING, type);
        System.out.println("DATA_CACHING: " + perf);

        perf = modeller.getPerformanceFromVmSize(size, CloudSuiteBenchmark.DATA_SERVING, type);
        System.out.println("DATA_SERVING: " + perf);

        perf = modeller.getPerformanceFromVmSize(size, CloudSuiteBenchmark.GRAPH_ANALYTICS, type);
        System.out.println("GRAPH_ANALYTICS: " + perf);

        perf = modeller.getPerformanceFromVmSize(size, CloudSuiteBenchmark.SOFTWARE_TESTING, type);
        System.out.println("SOFTWARE_TESTING: " + perf);

        perf = modeller.getPerformanceFromVmSize(size, CloudSuiteBenchmark.MEDIA_STREAMING, type);
        System.out.println("MEDIA_STREAMING: " + perf);

        perf = modeller.getPerformanceFromVmSize(size, CloudSuiteBenchmark.WEB_SEARCH, type);
        System.out.println("WEB_SEARCH: " + perf);

        perf = modeller.getPerformanceFromVmSize(size, CloudSuiteBenchmark.WEB_SERVING, type);
        System.out.println("WEB_SERVING: " + perf);

*/

    }




}
