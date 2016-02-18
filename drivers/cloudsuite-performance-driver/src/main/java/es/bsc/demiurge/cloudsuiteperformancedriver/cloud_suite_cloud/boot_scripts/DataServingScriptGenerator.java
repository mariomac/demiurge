package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.boot_scripts;

import es.bsc.demiurge.cloudsuiteperformancedriver.models.VmSize;

import java.util.HashMap;
import java.util.Map;

public class DataServingScriptGenerator {

    private static final String END_OF_LINE = System.getProperty("line.separator");
    private static final String YCSB_PATH = "/home/ubuntu/YCSB";
    private static final String CASSANDRA_PATH = "/home/ubuntu/apache-cassandra-0.7.3";
    private static final String RUN_CONFIG_PATH = "/home/ubuntu/YCSB/settings.dat";

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
                + " - [ cd, " + CASSANDRA_PATH + " ]" + END_OF_LINE
                + generateStartCassandraCommand() + END_OF_LINE
                + " - [ cd, " + YCSB_PATH + " ]" + END_OF_LINE
                + generatedModifyThreadsCommand(vmSize.getCpus()) + END_OF_LINE
                + generatePrintTimestampStartCommand() + END_OF_LINE
                + generateRunCommand() + END_OF_LINE
                + END_OF_LINE;
    }

    private static String generateStartCassandraCommand() {
        return " - [ bin/cassandra ]";
    }

    private static String generatedModifyThreadsCommand(int cpus) {
        return " - sed -i.bak -e '2d' " + RUN_CONFIG_PATH + " " + END_OF_LINE
                + " - sed -i.bak '1 a\\threadcount=" + cpus + "' " + RUN_CONFIG_PATH;
    }

    private static String generateRunCommand() {
        return " - [ /usr/bin/time, " + " -f, " + "\"real_time %e\","
                + " ./run.command,"
                + " \"&> results.txt\" ]";
    }

    private static String generatePrintTimestampStartCommand() {
        return " - echo \"timestamp_start:$(date +%s)\"";
    }

}

