package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.config.models;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

public class FormulaConfig {

    private final String benchmark;
    private final List<Double> cpusCoefficients = new ArrayList<>();
    private final List<Double> ramGbCoefficients = new ArrayList<>();
    private final List<Double> diskGbCoefficients = new ArrayList<>();
    private final double independentTerm;
    private double maxPerformance;
    private double minPerformance;


    public FormulaConfig(String benchmark, List<Double> cpusCoefficients, List<Double> ramGbCoefficients,
                         List<Double> diskGbCoefficients, double independentTerm) {
        this.benchmark = benchmark;
        this.cpusCoefficients.addAll(cpusCoefficients);
        this.ramGbCoefficients.addAll(ramGbCoefficients);
        this.diskGbCoefficients.addAll(diskGbCoefficients);
        this.independentTerm = independentTerm;
    }

    public FormulaConfig(String benchmark, List<Double> cpusCoefficients, List<Double> ramGbCoefficients,
                         List<Double> diskGbCoefficients, double independentTerm, double maxPerf) {
        this.benchmark = benchmark;
        this.cpusCoefficients.addAll(cpusCoefficients);
        this.ramGbCoefficients.addAll(ramGbCoefficients);
        this.diskGbCoefficients.addAll(diskGbCoefficients);
        this.independentTerm = independentTerm;
        this.maxPerformance = maxPerformance;
    }


    public String getBenchmark() {
        return benchmark;
    }

    public double getIndependentTerm() {
        return independentTerm;
    }

    public List<Double> getCpusCoefficients() {
        return new ArrayList<>(cpusCoefficients);
    }

    public List<Double> getRamGbCoefficients() {
        return new ArrayList<>(ramGbCoefficients);
    }

    public List<Double> getDiskGbCoefficients() {
        return new ArrayList<>(diskGbCoefficients);
    }

    public double getMaxPerformance() {
        return maxPerformance;
    }

    public double getMinPerformance() {
        return minPerformance;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("benchmark", benchmark)
                .add("cpusCoefficients", cpusCoefficients)
                .add("ramGbCoefficients", ramGbCoefficients)
                .add("diskGbCoefficients", diskGbCoefficients)
                .add("independentTerm", independentTerm)
        //        .add("maxPerf", maxPerf)
                .toString();
    }

}
