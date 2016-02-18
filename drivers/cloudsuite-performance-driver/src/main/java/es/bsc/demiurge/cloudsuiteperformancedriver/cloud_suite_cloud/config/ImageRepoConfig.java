package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.config;

public class ImageRepoConfig {

    private final String data_analytics_image_id;
    private final String data_caching_image_id;
    private final String data_serving_image_id;
    private final String graph_analytics_image_id;
    private final String media_streaming_server_image_id;
    private final String media_streaming_client_image_id;
    private final String software_testing_image_id;
    private final String web_search_image_id;
    private final String web_search_client_image_id;
    private final String web_serving_client_image_id;
    private final String web_serving_frontend_image_id;
    private final String web_serving_backend_image_id;

    public ImageRepoConfig(String data_analytics_image_id, String data_caching_image_id,
                           String data_serving_image_id, String graph_analytics_image_id,
                           String media_streaming_server_image_id, String media_streaming_client_image_id,
                           String software_testing_image_id, String web_search_image_id,
                           String web_search_client_image_id, String web_serving_client_image_id,
                           String web_serving_frontend_image_id, String web_serving_backend_image_id) {
        this.data_analytics_image_id = data_analytics_image_id;
        this.data_caching_image_id = data_caching_image_id;
        this.data_serving_image_id = data_serving_image_id;
        this.graph_analytics_image_id = graph_analytics_image_id;
        this.media_streaming_server_image_id = media_streaming_server_image_id;
        this.media_streaming_client_image_id = media_streaming_client_image_id;
        this.software_testing_image_id = software_testing_image_id;
        this.web_search_image_id = web_search_image_id;
        this.web_search_client_image_id = web_search_client_image_id;
        this.web_serving_client_image_id = web_serving_client_image_id;
        this.web_serving_frontend_image_id = web_serving_frontend_image_id;
        this.web_serving_backend_image_id = web_serving_backend_image_id;
    }

    public String getWeb_serving_backend_image_id() {
        return web_serving_backend_image_id;
    }

    public String getData_analytics_image_id() {
        return data_analytics_image_id;
    }

    public String getData_caching_image_id() {
        return data_caching_image_id;
    }

    public String getData_serving_image_id() {
        return data_serving_image_id;
    }

    public String getGraph_analytics_image_id() {
        return graph_analytics_image_id;
    }

    public String getMedia_streaming_server_image_id() {
        return media_streaming_server_image_id;
    }

    public String getMedia_streaming_client_image_id() {
        return media_streaming_client_image_id;
    }

    public String getSoftware_testing_image_id() {
        return software_testing_image_id;
    }

    public String getWeb_search_image_id() {
        return web_search_image_id;
    }

    public String getWeb_search_client_image_id() {
        return web_search_client_image_id;
    }

    public String getWeb_serving_client_image_id() {
        return web_serving_client_image_id;
    }

    public String getWeb_serving_frontend_image_id() {
        return web_serving_frontend_image_id;
    }

}
