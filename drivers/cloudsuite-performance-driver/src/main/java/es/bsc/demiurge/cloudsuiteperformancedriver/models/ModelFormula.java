package es.bsc.demiurge.cloudsuiteperformancedriver.models;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

public class ModelFormula {

    /* This class represents a formula of the type:
    * C1*cpus^2 + C2*cpus + C3*ramGb^2 + C4*ramGb + C5*diskGb^2 + C6*diskGb + independentTerm
    * It admits cpus, ramGb, and diskGb to the power of any number.
      */

    private final List<Double> cpusCoefficients = new ArrayList<>();
    private final List<Double> ramGbCoefficients = new ArrayList<>();
    private final List<Double> diskGbCoefficients = new ArrayList<>();
    private final double independentTerm;

    public ModelFormula(List<Double> cpusCoefficients, List<Double> ramGbCoefficients,
                        List<Double> diskGbCoefficients, double independentTerm) {
        this.cpusCoefficients.addAll(cpusCoefficients);
        this.ramGbCoefficients.addAll(ramGbCoefficients);
        this.diskGbCoefficients.addAll(diskGbCoefficients);
        this.independentTerm = independentTerm;
    }

    public double applyFormula(int cpus, int ramGb, int diskGb) {
        return sumCpuCoefficients(cpus) + sumRamGbCoefficients(ramGb)
                + sumDiskGbCoefficients(diskGb) + independentTerm;
    }

    private double sumCpuCoefficients(int cpus) {
        double result = 0.0;
        for (int cpuTerm = 0; cpuTerm < cpusCoefficients.size(); ++cpuTerm) {
            result += cpusCoefficients.get(cpuTerm) * Math.pow(cpus, cpuTerm + 1);
        }
        return result;
    }

    private double sumRamGbCoefficients(int ramGb) {
        double result = 0.0;
        for (int ramGbTerm = 0; ramGbTerm < ramGbCoefficients.size(); ++ramGbTerm) {
            result += ramGbCoefficients.get(ramGbTerm) * Math.pow(ramGb, ramGbTerm + 1);
        }
        return result;
    }

    private double sumDiskGbCoefficients(int diskGb) {
        double result = 0.0;
        for (int diskGbTerm = 0; diskGbTerm < diskGbCoefficients.size(); ++diskGbTerm) {
            result += diskGbCoefficients.get(diskGbTerm) * Math.pow(diskGb, diskGbTerm + 1);
        }
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("cpusCoefficients", cpusCoefficients)
                .add("ramGbCoefficients", ramGbCoefficients)
                .add("diskGbCoefficients", diskGbCoefficients)
                .add("independentTerm", independentTerm)
                .toString();
    }

}
