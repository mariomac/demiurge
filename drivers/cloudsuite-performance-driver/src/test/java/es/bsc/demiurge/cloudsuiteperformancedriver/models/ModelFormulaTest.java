package es.bsc.demiurge.cloudsuiteperformancedriver.models;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ModelFormulaTest {

    @Test
    public void computesFormulaCorrectlyWhenAllCoeffArePresent() {
        // cpus^3 * 0.3 + cpus^2 * 0.2 + cpus * 10 + ramGb^2 * 5 + ramGb * 10 + diskGb * 2 + 25
        Double[] cpusCoefficients = {10.0, 0.2, 0.3};
        Double[] ramGbCoefficients = {10.0, 5.0};
        Double[] diskGbCoefficients = {2.0};

        ModelFormula modelFormula = new ModelFormula(
                Arrays.asList(cpusCoefficients),
                Arrays.asList(ramGbCoefficients),
                Arrays.asList(diskGbCoefficients),
                25.0);

        assertEquals(660, modelFormula.applyFormula(10, 5, 20), 0.05);
    }

    @Test
    public void computesFormulaCorrectlyWhenACoeffIsNotPresent() {
        // ramGb^2 * 5 + ramGb * 10 + diskGb * 2 + 25
        Double[] cpusCoefficients = {};
        Double[] ramGbCoefficients = {10.0, 5.0};
        Double[] diskGbCoefficients = {2.0};

        ModelFormula modelFormula = new ModelFormula(
                Arrays.asList(cpusCoefficients),
                Arrays.asList(ramGbCoefficients),
                Arrays.asList(diskGbCoefficients),
                25.0);

        assertEquals(240, modelFormula.applyFormula(10, 5, 20), 0.05);
    }

    @Test
    public void computesFormulaCorrectlyWhenNoCoeffArePresent() {
        ModelFormula modelFormula = new ModelFormula(
                new ArrayList<Double>(),
                new ArrayList<Double>(),
                new ArrayList<Double>(),
                25.5);
        assertEquals(25.5, modelFormula.applyFormula(10, 5, 20), 0.05);
    }

}
