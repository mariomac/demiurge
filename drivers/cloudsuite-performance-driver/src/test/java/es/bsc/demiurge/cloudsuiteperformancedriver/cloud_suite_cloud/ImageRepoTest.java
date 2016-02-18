package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud;

import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.config.ImageRepoConfig;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.CloudSuiteBenchmark;
import org.junit.Test;

import static junit.framework.TestCase.*;

public class ImageRepoTest {

    private final ImageRepo imageRepo = new ImageRepo(
            new ImageRepoConfig(
                    "id1", "id2", "id3", "id4", "id5", "id6", "id7", "id8", "id9", "id10", "id11", "id12"));

    @Test
    public void returnsOneIdForBenchmarksWithOneVm() {
        assertEquals(1, imageRepo.getImages(CloudSuiteBenchmark.DATA_ANALYTICS).size());
    }

    @Test
    public void returnsSeveralIdsForBenchmarksWithSeveralVms() {
        assertTrue(imageRepo.getImages(CloudSuiteBenchmark.WEB_SERVING).size() > 1);
    }

    @Test
    public void returnsIdsForEachBenchmark() {
        for (CloudSuiteBenchmark benchmark : CloudSuiteBenchmark.values()) {
            assertFalse(imageRepo.getImages(benchmark).isEmpty());
        }
    }

}
