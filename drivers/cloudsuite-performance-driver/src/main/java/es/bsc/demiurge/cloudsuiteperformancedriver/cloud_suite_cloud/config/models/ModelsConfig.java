package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.config.models;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

public class ModelsConfig {

    private final List<HostModelsConfig> models = new ArrayList<>();

    public ModelsConfig(List<HostModelsConfig> hostModelsConfigs) {
        this.models.addAll(hostModelsConfigs);
    }

    public List<HostModelsConfig> getModels() {
        return new ArrayList<>(models);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("models", models)
                .toString();
    }

}
