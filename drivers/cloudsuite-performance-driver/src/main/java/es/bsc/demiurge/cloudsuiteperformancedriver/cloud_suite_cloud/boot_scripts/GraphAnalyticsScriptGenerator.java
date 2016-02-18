package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.boot_scripts;

import es.bsc.demiurge.cloudsuiteperformancedriver.models.VmSize;

import java.util.HashMap;
import java.util.Map;

public class GraphAnalyticsScriptGenerator {

    private static final String TUNKRANK_PATH = "/home/ubuntu/graph-release/release/toolkits/graph_analytics";
    private static final String END_OF_LINE = System.getProperty("line.separator");

    public static Map<String, String> generateScripts(VmSize vmSize) {
        Map<String, String> result = new HashMap<>();
        result.put("default", generateDefaultVmScript(vmSize));
        return result;
    }

    public static String generateDefaultVmScript(VmSize vmSize) {
        return "#cloud-config" + END_OF_LINE
                + "password: bsc" + END_OF_LINE
                + "chpasswd: { expire: False }" + END_OF_LINE
                + "ssh_pwauth: True" + END_OF_LINE
                + "runcmd:" + END_OF_LINE
                + generatePrintTimestampStartCommand() + END_OF_LINE
                + " - [ cd, " + TUNKRANK_PATH + " ]" + END_OF_LINE
                + generateExecuteBenchmarkManyTimesCommand(vmSize.getCpus()) + END_OF_LINE
                + END_OF_LINE;
    }

    private static String generateExecuteBenchmarkManyTimesCommand(int ncpus) {
        return " - for i in `seq 1000`; do " + tunkrankExecutionCommand(ncpus) + ";sleep 15; done" + END_OF_LINE;
    }

    private static String tunkrankExecutionCommand(int ncpus) {
        return " /usr/bin/time -f \"real_time %e\" " +
                "./tunkrank --graph=/home/ubuntu/Twitter-dataset/data/twitter_small_data_graplab.in --format=tsv " +
                "--ncpus=" + ncpus + " --engine=asynchronous";
    }

    private static String generatePrintTimestampStartCommand() {
        return " - echo \"timestamp_start:$(date +%s)\"";
    }

}
