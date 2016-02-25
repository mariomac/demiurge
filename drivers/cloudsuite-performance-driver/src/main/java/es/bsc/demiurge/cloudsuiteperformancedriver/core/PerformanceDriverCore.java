package es.bsc.demiurge.cloudsuiteperformancedriver.core;

import com.google.gson.Gson;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.ImageRepo;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.Modeller;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.config.ImageRepoConfig;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.config.models.ModelsConfig;
import es.bsc.demiurge.cloudsuiteperformancedriver.utils.Utils;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class PerformanceDriverCore {

    private static final Gson gson = new Gson();
    private ImageRepoConfig imagesConfig;
    private ImageRepo imageRepo;
    private Modeller modeller;

    public PerformanceDriverCore() {
        imagesConfig = gson.fromJson(Utils.readFile("imageRepoConfig.json"), ImageRepoConfig.class);
        imageRepo = new ImageRepo(imagesConfig);
        modeller = new Modeller(gson.fromJson(Utils.readFile("modelsConfig.json"), ModelsConfig.class));
    }

    public ImageRepoConfig getImagesConfig() {
        return imagesConfig;
    }

    public ImageRepo getImageRepo() {
        return imageRepo;
    }

    public Modeller getModeller() {
        return modeller;
    }
}


