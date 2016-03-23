package es.bsc.demiurge.cloudsuiteperformancedriver;

import com.google.gson.Gson;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.ImageRepo;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.Modeller;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.config.ImageRepoConfig;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.config.models.ModelsConfig;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.CloudSuiteBenchmark;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.VmSize;
import es.bsc.demiurge.cloudsuiteperformancedriver.utils.Utils;

import java.util.ArrayList;

public class Main {

    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        ImageRepoConfig imagesConfig =
                gson.fromJson(Utils.readFile("imageRepoConfig.json"), ImageRepoConfig.class);
        ImageRepo imageRepo = new ImageRepo(imagesConfig);
        Modeller modeller = new Modeller(gson.fromJson(Utils.readFile("modelsConfig.json"), ModelsConfig.class));

        String host = "bscgrid30";

        ArrayList<CloudSuiteBenchmark> b = new ArrayList();
        b.add(CloudSuiteBenchmark.DATA_SERVING);
        b.add(CloudSuiteBenchmark.DATA_ANALYTICS);
        b.add(CloudSuiteBenchmark.DATA_CACHING);
        b.add(CloudSuiteBenchmark.GRAPH_ANALYTICS);
        b.add(CloudSuiteBenchmark.MEDIA_STREAMING);
        b.add(CloudSuiteBenchmark.SOFTWARE_TESTING);
        b.add(CloudSuiteBenchmark.WEB_SEARCH);
        b.add(CloudSuiteBenchmark.WEB_SERVING);

        for ( CloudSuiteBenchmark a : b) {
            double res = modeller.getBenchmarkPerformance(
                    a, host,
                    new VmSize(8, 12, 300)
            );

            System.out.println(a +": "+ res);
        }



        /*
        Scheduler scheduler = new PerfAndEnergyAwareScheduler(modeller);
        VmmAdapter vmmAdapter = new VmmAdapter(gson.fromJson(Utils.readFile("cloudConfig.json"), VmmConfig.class),
                imageRepo);
        Cloud cloud = new Cloud(scheduler, vmmAdapter);

        WorkloadExecutor workloadExecutor = new WorkloadExecutor(cloud, modeller);
        workloadExecutor.executeWorkload(gson.fromJson(Utils.readFile("workload.json"), Workload.class));*/


    }




}
