package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.config.models;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

public class HostModelsConfig {

    private final String hostname;
    private final double idlePower;
    private final double maxPower;
    private final List<FormulaConfig> powerModels = new ArrayList<>();
    private final List<FormulaConfig> performanceModels = new ArrayList<>();

    public HostModelsConfig(String hostname, List<FormulaConfig> powerModels, List<FormulaConfig> performanceModels) {
        this.hostname = hostname;
        this.powerModels.addAll(powerModels);
        this.performanceModels.addAll(performanceModels);
        this.idlePower = 0;
        this.maxPower = 500;
    }
    public HostModelsConfig(String hostname, List<FormulaConfig> powerModels, List<FormulaConfig> performanceModels, double idlePower, double maxPower) {
        this.hostname = hostname;
        this.powerModels.addAll(powerModels);
        this.performanceModels.addAll(performanceModels);
        this.idlePower = idlePower;
        this.maxPower = maxPower;
    }

    public String getHostname() {
        return hostname;
    }

    public List<FormulaConfig> getPowerModels() {
        return new ArrayList<>(powerModels);
    }

    public List<FormulaConfig> getPerformanceModels() {
        return new ArrayList<>(performanceModels);
    }

    public double getIdlePower() { return idlePower; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("hostname", hostname)
                .add("powerModels", powerModels)
                .add("performanceModels", performanceModels)
                .toString();
    }

    public Double getMaxPower() {
        return maxPower;
    }
}
