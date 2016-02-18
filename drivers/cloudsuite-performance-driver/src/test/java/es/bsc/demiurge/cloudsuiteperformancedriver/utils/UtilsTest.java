package es.bsc.demiurge.cloudsuiteperformancedriver.utils;

import org.junit.Test;

public class UtilsTest {

    @Test
    public void doesNotRaiseExceptionWhenReadingConfigFiles() {
        String[] configFilesToTest = {"imageRepoConfig.json", "modelsConfig.json", "cloudConfig.json"};
        for (String configFile : configFilesToTest) {
            Utils.readFile(configFile);
        }
    }

}
