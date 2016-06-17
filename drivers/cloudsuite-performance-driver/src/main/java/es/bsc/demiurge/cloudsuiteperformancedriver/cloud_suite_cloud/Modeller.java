package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud;

import com.google.common.base.MoreObjects;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.config.models.FormulaConfig;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.config.models.HostModelsConfig;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.config.models.ModelsConfig;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Modeller {

    private static final double PERF_ACCEPTABLE_DELTA = 0.05;

    private final Map<String, Map<CloudSuiteBenchmark, ModelFormula>> powerModels = new HashMap<>();
    private final Map<String, Map<CloudSuiteBenchmark, ModelFormulaMaxPerf>> performanceModels = new HashMap<>();

    private final HashMap<String, Double> idleValues = new HashMap<>();
    private final HashMap<String, Double> maxPowerValues = new HashMap<>();

    public Modeller(ModelsConfig modelsConfig) {
        for (HostModelsConfig hostModelsConfig : modelsConfig.getModels()) {
            String hostname = hostModelsConfig.getHostname();

            Map<CloudSuiteBenchmark, ModelFormula> benchmarkFormula = new HashMap<>();
            for (FormulaConfig formulaConfig : hostModelsConfig.getPowerModels()) {
                benchmarkFormula.put(
                        getBenchmarkFromName(formulaConfig.getBenchmark()),
                        new ModelFormula(
                                formulaConfig.getCpusCoefficients(),
                                formulaConfig.getRamGbCoefficients(),
                                formulaConfig.getDiskGbCoefficients(),
                                formulaConfig.getIndependentTerm()));
            }
            powerModels.put(hostname, benchmarkFormula);

            Map<CloudSuiteBenchmark, ModelFormulaMaxPerf> benchmarkFormulaMaxPerf = new HashMap<>();
            for (FormulaConfig formulaConfig : hostModelsConfig.getPerformanceModels()) {
                benchmarkFormulaMaxPerf.put(
                        getBenchmarkFromName(formulaConfig.getBenchmark()),
                        new ModelFormulaMaxPerf(
                                formulaConfig.getCpusCoefficients(),
                                formulaConfig.getRamGbCoefficients(),
                                formulaConfig.getDiskGbCoefficients(),
                                formulaConfig.getIndependentTerm(),
                                formulaConfig.getMaxPerformance()));
            }
            performanceModels.put(hostname, benchmarkFormulaMaxPerf);


            idleValues.put(hostname, hostModelsConfig.getIdlePower());
            maxPowerValues.put(hostname, hostModelsConfig.getMaxPower());
        }
    }

    /**
     * Returns a list of VM sizes for which the given benchmark has a minimum performance of perf when
     * executed in the specified host.
     *
     * @param perf the minimum performance desired
     * @param benchmark the benchmark
     * @param host the host where the benchmark is executed
     * @return the list of VM sizes
     */
    public List<VmSize> getVmSizesWithAtLeastPerformance(double perf, CloudSuiteBenchmark benchmark,
                                                         Host host) {

        List<VmSize> result = new ArrayList<>();
        int minCpus = benchmark.getMinimumVmSize().getCpus();
        int minRamGb = benchmark.getMinimumVmSize().getRamGb();
        int minDiskGb = benchmark.getMinimumVmSize().getDiskGb();

        for (int cpus = minCpus; cpus <= benchmarkMaxCpus(benchmark, host); ++cpus) {
            for (int ramGb = minRamGb; ramGb <= benchmarkMaxRamGb(benchmark, host); ++ramGb) {
                for (int diskGb = minDiskGb; diskGb <= benchmarkMaxDiskGb(benchmark, host); diskGb += 10) {
                    if (benchmark.getPerformanceValue() == PerformanceValue.ASCENDANT_PERFORMANCE) {
                        if (getBenchmarkPerformance(benchmark, host.getType(), new VmSize(cpus, ramGb, diskGb))
                                >= (perf - PERF_ACCEPTABLE_DELTA)) {
                            result.add(new VmSize(cpus, ramGb, diskGb));
                        }
                    }
                    else {
                        if (getBenchmarkPerformance(benchmark, host.getType(), new VmSize(cpus, ramGb, diskGb))
                                <= (perf - PERF_ACCEPTABLE_DELTA)) {
                            result.add(new VmSize(cpus, ramGb, diskGb));
                        }
                    }
                }
            }
        }

        return result;
    }


    public boolean hostSupportPerformance(Host host, CloudSuiteBenchmark benchmark, double requiredPerf){
        //System.out.println("comparing " + requiredPerf + ": max supported: " + performanceModels.get(host.getHostname()).get(benchmark).getMaxPerformance());
        if ( requiredPerf <= performanceModels.get(host.getHostname()).get(benchmark).getMaxPerformance()){
            return true;
        }else
            return false;

    }

    /**
     * Returns the smaller VM size for which the given benchmark has a minimum performance of perf when
     * executed in the specified host.
     *
     * @param perf the minimum performance desired
     * @param benchmark the benchmark
     * @param host the host where the benchmark is executed
     * @return the list of VM sizes
     */
    public VmSize getMinVmSizesWithAtLeastPerformance(double perf, CloudSuiteBenchmark benchmark,
                                                      Host host){

        int minCpus = benchmark.getMinimumVmSize().getCpus();
        int minRamGb = benchmark.getMinimumVmSize().getRamGb();
        int minDiskGb = benchmark.getMinimumVmSize().getDiskGb();

        for (int cpus = minCpus; cpus <= benchmarkMaxCpus(benchmark, host); ++cpus) {
            for (int ramGb = minRamGb; ramGb <= benchmarkMaxRamGb(benchmark, host); ++ramGb) {
                for (int diskGb = minDiskGb; diskGb <= benchmarkMaxDiskGb(benchmark, host); diskGb += 10) {
                    if (benchmark.getPerformanceValue() == PerformanceValue.ASCENDANT_PERFORMANCE) {
                        if (getBenchmarkPerformance(benchmark, host.getType(), new VmSize(cpus, ramGb, diskGb))
                                >= (perf - PERF_ACCEPTABLE_DELTA)) {
                            return (new VmSize(cpus, ramGb, diskGb));
                        }
                    }
                    else {
                        double predPerf = getBenchmarkPerformance(benchmark, host.getType(), new VmSize(cpus, ramGb, diskGb));
                        if (predPerf
                                <= (perf - PERF_ACCEPTABLE_DELTA)) {
                            return (new VmSize(cpus, ramGb, diskGb));
                        }
                    }
                }
            }
        }
        return null;
    }


    public double getPerformanceFromVmSize(VmSize vmSize, CloudSuiteBenchmark benchmark,
                                           String hostType){

        int minCpus = benchmark.getMinimumVmSize().getCpus();
        int minRamGb = benchmark.getMinimumVmSize().getRamGb();
        int minDiskGb = benchmark.getMinimumVmSize().getDiskGb();

        if (minCpus > vmSize.getCpus() || minRamGb > vmSize.getRamGb() || minDiskGb > vmSize.getDiskGb()){
            return (getBenchmarkPerformance(benchmark, hostType, new VmSize(minCpus, minRamGb, minDiskGb)));

        }

        return (getBenchmarkPerformance(benchmark, hostType, vmSize));

    }

    public double getBenchmarkPerformance(CloudSuiteBenchmark benchmark, String hostname, VmSize vmSize) {
        return performanceModels.get(hostname).get(benchmark).applyFormula(
                vmSize.getCpus(), vmSize.getRamGb(), vmSize.getDiskGb());
    }

    public double getBenchmarkMaxPerformanceHost(CloudSuiteBenchmark benchmark, String hostname) {
        return performanceModels.get(hostname).get(benchmark).getMaxPerformance();
    }

    public double getBenchmarkAvgPower(CloudSuiteBenchmark benchmark, String hostname, VmSize vmSize) {
        return powerModels.get(hostname).get(benchmark).applyFormula(
                vmSize.getCpus(), vmSize.getRamGb(), vmSize.getDiskGb());
    }

    private int benchmarkMaxCpus(CloudSuiteBenchmark benchmark, Host host) {
        return Math.min(host.getCpus(), benchmark.getMaximumVmSize().getCpus());
    }

    private int benchmarkMaxRamGb(CloudSuiteBenchmark benchmark, Host host) {
        // Minus 4 because reserving all the memory for a single VM can affect performance
        return Math.max(1, Math.min(host.getRamGb(), benchmark.getMaximumVmSize().getRamGb() - 4));
    }

    private int benchmarkMaxDiskGb(CloudSuiteBenchmark benchmark, Host host) {
        return Math.min(host.getDiskGb(), benchmark.getMaximumVmSize().getDiskGb());
    }

    // This should be done in a better way, but it's ok for now
    public static CloudSuiteBenchmark getBenchmarkFromName(String name) {
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

    public double getIdlePowerHost(String hostname){

        return idleValues.get(hostname);

    }

    public double getMaxPower(String hostname) {
        return maxPowerValues.get(hostname);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("powerModels", powerModels)
                .add("performanceModels", performanceModels)
                .toString();
    }

}
