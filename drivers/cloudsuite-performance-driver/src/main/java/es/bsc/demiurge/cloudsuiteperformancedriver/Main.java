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
    }

}
