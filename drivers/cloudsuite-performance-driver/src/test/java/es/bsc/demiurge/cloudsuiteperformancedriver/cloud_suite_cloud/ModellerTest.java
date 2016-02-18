package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud;

import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.config.models.FormulaConfig;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.config.models.HostModelsConfig;
import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.config.models.ModelsConfig;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.CloudSuiteBenchmark;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.Host;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.VmSize;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class ModellerTest {

    private static Modeller modeller;

    @BeforeClass
    public static void setUp() {
        FormulaConfig powerModelDataAnalytics = new FormulaConfig(
                "data_caching",
                Arrays.asList(2.0, 0.2),
                Collections.singletonList(0.5),
                Collections.singletonList(0.1),
                150.0);

        FormulaConfig perfModelDataAnalytics = new FormulaConfig(
                "data_caching",
                Arrays.asList(10.0, 0.2),
                Collections.singletonList(1.0),
                new ArrayList<Double>(),
                0.5);

        List<FormulaConfig> powerModels = Collections.singletonList(powerModelDataAnalytics);
        List<FormulaConfig> perfModels = Collections.singletonList(perfModelDataAnalytics);

        HostModelsConfig hostModelsConfig = new HostModelsConfig("testHost", powerModels, perfModels);
        ModelsConfig modelsConfig = new ModelsConfig(Collections.singletonList(hostModelsConfig));
        modeller = new Modeller(modelsConfig);
    }

    @Test
    public void computesCorrectlyThePerformanceOfABenchmark() {
        double calculatedPerformance = modeller.getBenchmarkPerformance(
                CloudSuiteBenchmark.DATA_CACHING,
                "testHost",
                new VmSize(2, 4, 1));
        assertEquals(25.3, calculatedPerformance, 0.1);
    }

    @Test
    public void computesCorrectlyTheAvgPowerOfABenchmark() {
        double calculatedAvgPower = modeller.getBenchmarkAvgPower(
                CloudSuiteBenchmark.DATA_CACHING,
                "testHost",
                new VmSize(2, 4, 1));
        assertEquals(156.9, calculatedAvgPower, 0.1);
    }

    @Test
    public void returnsListOfVmsWithMinimumPerformance() {
        Set<VmSize> returnedVmSizes = new HashSet<>();
        returnedVmSizes.addAll(
                modeller.getVmSizesWithAtLeastPerformance(225,
                        CloudSuiteBenchmark.DATA_CACHING,
                        new Host("testHost", 16, 16, 10, 0, 0, 0)));

        Set<VmSize> expectedVmSizes = new HashSet<>();
        expectedVmSizes.add(new VmSize(16, 14, 10));
        expectedVmSizes.add(new VmSize(16, 15, 10));
        expectedVmSizes.add(new VmSize(16, 16, 10));

        assertEquals(expectedVmSizes, returnedVmSizes);
    }

    @Test
    public void returnsEmptyListWhenAskedForVmsWithMinimumPerformanceIfPerfCannotBeAchieved() {
        assertTrue(modeller.getVmSizesWithAtLeastPerformance(800,
                CloudSuiteBenchmark.DATA_CACHING,
                new Host("testHost", 10, 10, 10, 0, 0, 0)).isEmpty());
    }

}
