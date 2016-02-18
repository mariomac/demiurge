package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.boot_scripts;

import es.bsc.demiurge.cloudsuiteperformancedriver.models.VmSize;

import java.util.HashMap;
import java.util.Map;

public class MediaStreamingScriptGenerator {

    private static final int SERVER_OUTPUT_PRINT_INTERVAL_SECS = 20;
    private static final String END_OF_LINE = System.getProperty("line.separator");

    public static Map<String, String> generateScripts(VmSize vmSize) {
        Map<String, String> result = new HashMap<>();
        result.put("default", generateDefaultVmScript(vmSize));
        return result;
    }

    private static String generateDefaultVmScript(VmSize vmSize) {
        return "#cloud-config" + END_OF_LINE
                + "password: bsc" + END_OF_LINE
                + "chpasswd: { expire: False }" + END_OF_LINE
                + "ssh_pwauth: True" + END_OF_LINE
                + "runcmd:" + END_OF_LINE
                + generatePrintTimestampStartCommand() + END_OF_LINE
                + generateSetLimitsCommands() + END_OF_LINE
                + generateExecuteServerCommand() + END_OF_LINE
                + END_OF_LINE;
    }

    private static String generateSetLimitsCommands() {
        // Maybe not all of these are needed. Just in case...
        return " - ulimit -n 65535" + END_OF_LINE
                + " - ulimit -d unlimited" + END_OF_LINE
                + " - ulimit -f unlimited" + END_OF_LINE
                + " - ulimit -i unlimited" + END_OF_LINE
                + " - ulimit -m unlimited" + END_OF_LINE
                + " - ulimit -s unlimited" + END_OF_LINE
                + " - ulimit -t unlimited" + END_OF_LINE
                + " - ulimit -u unlimited" + END_OF_LINE
                + " - ulimit -v unlimited" + END_OF_LINE
                + " - ulimit -x unlimited" + END_OF_LINE;
    }

    private static String generateExecuteServerCommand() {
        return " - trickle -u 100000 -d 100000 " +
                "/usr/local/sbin/DarwinStreamingServer -dDS " + SERVER_OUTPUT_PRINT_INTERVAL_SECS;
    }

    private static String generatePrintTimestampStartCommand() {
        return " - echo \"timestamp_start:$(date +%s)\"";
    }

}
