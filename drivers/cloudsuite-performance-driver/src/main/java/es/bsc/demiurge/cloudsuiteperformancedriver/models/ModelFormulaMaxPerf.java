package es.bsc.demiurge.cloudsuiteperformancedriver.models;

import java.util.List;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class ModelFormulaMaxPerf extends ModelFormula{

    private final double maxPerformance;

    public ModelFormulaMaxPerf(List<Double> cpusCoefficients, List<Double> ramGbCoefficients, List<Double> diskGbCoefficients, double independentTerm, double maxPerformance) {
        super(cpusCoefficients, ramGbCoefficients, diskGbCoefficients, independentTerm);
        this.maxPerformance = maxPerformance;
    }

    public double getMaxPerformance() {
        return maxPerformance;
    }
}
