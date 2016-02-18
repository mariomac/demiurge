package es.bsc.demiurge.cloudsuiteperformancedriver.logging;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class CloudSuiteSchedulerLogger {

    private static Logger logger = LogManager.getLogger(CloudSuiteSchedulerLogger.class);

    public static void logDeployment(Deployment deployment) {
        logger.debug(deployment.getTimestamp() + ","
                + deployment.getBenchmark() + ","
                + deployment.getHostname() + ","
                + deployment.getVmSize().getCpus() + ","
                + deployment.getVmSize().getRamGb() + ","
                + deployment.getVmSize().getDiskGb() + ","
                + deployment.getExpectedPerf() + ","
                + deployment.getExpectedPower());
    }

    public static void logVmDestroy(String vmId) {
        logger.debug("Destroyed VM with ID: " + vmId);
    }

}
