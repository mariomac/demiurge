package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud;

import com.google.common.base.MoreObjects;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.config.ImageRepoConfig;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.CloudSuiteBenchmark;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageRepo {

    private final Map<CloudSuiteBenchmark, String[]> cloudSuiteImages = new HashMap<>();

    public ImageRepo(ImageRepoConfig imagesConfig) {
        cloudSuiteImages.put(CloudSuiteBenchmark.DATA_ANALYTICS,
                new String[]{imagesConfig.getData_analytics_image_id()});
        cloudSuiteImages.put(CloudSuiteBenchmark.DATA_CACHING, new String[]{imagesConfig.getData_caching_image_id()});
        cloudSuiteImages.put(CloudSuiteBenchmark.DATA_SERVING, new String[]{imagesConfig.getData_serving_image_id()});
        cloudSuiteImages.put(CloudSuiteBenchmark.GRAPH_ANALYTICS,
                new String[]{imagesConfig.getGraph_analytics_image_id()});
        cloudSuiteImages.put(CloudSuiteBenchmark.MEDIA_STREAMING, new String[]{
                imagesConfig.getMedia_streaming_client_image_id(),
                imagesConfig.getMedia_streaming_server_image_id()});
        cloudSuiteImages.put(CloudSuiteBenchmark.SOFTWARE_TESTING,
                new String[]{imagesConfig.getSoftware_testing_image_id()});
        cloudSuiteImages.put(CloudSuiteBenchmark.WEB_SEARCH, new String[]{
                imagesConfig.getWeb_search_image_id(),
                imagesConfig.getWeb_search_client_image_id()});
        cloudSuiteImages.put(CloudSuiteBenchmark.WEB_SERVING, new String[]{
                imagesConfig.getWeb_serving_client_image_id(),
                imagesConfig.getWeb_serving_frontend_image_id(),
                imagesConfig.getWeb_serving_backend_image_id()});
    }

    public List<String> getImages(CloudSuiteBenchmark cloudSuiteBenchmark) {
        return Arrays.asList(cloudSuiteImages.get(cloudSuiteBenchmark));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("cloudSuiteImages", cloudSuiteImages)
                .toString();
    }

}
