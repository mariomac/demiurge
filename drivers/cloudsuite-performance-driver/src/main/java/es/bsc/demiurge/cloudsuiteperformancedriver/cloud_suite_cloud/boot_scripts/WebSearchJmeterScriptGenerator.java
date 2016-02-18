package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.boot_scripts;

import es.bsc.demiurge.cloudsuiteperformancedriver.models.VmSize;

import java.util.HashMap;
import java.util.Map;

public class WebSearchJmeterScriptGenerator {

    private static final String NUTCH_CONFIG_FILE = "/home/bsc/nutch-test/dis_search/conf/nutch-default.xml";
    private static final String END_OF_LINE = System.getProperty("line.separator");

    public static Map<String, String> generateScripts(VmSize vmSize) {
        Map<String, String> result = new HashMap<>();
        result.put("default", generateDefaultVmScript(vmSize));
        result.put("client", generateClientVmScript());
        return result;
    }

    private static String generateClientVmScript() {
        return "#cloud-config" + END_OF_LINE
                + "chpasswd:" + END_OF_LINE
                + "  list: |" + END_OF_LINE
                + "    ubuntu:ubuntu" + END_OF_LINE
                + "  expire: False";
    }

    public static String generateDefaultVmScript(VmSize vmSize) {
        return "#cloud-config" + END_OF_LINE
                + "password: bsc" + END_OF_LINE
                + "chpasswd: { expire: False }" + END_OF_LINE
                + "ssh_pwauth: True" + END_OF_LINE
                + "runcmd:" + END_OF_LINE
                + getChangeNutchConfigFileCommands(vmSize.getCpus()) + END_OF_LINE
                + generatePrintTimestampStartCommand() + END_OF_LINE
                + END_OF_LINE;
    }

    private static String getChangeNutchConfigFileCommands(int cpus) {
        return " - [ sed, -i.bak, -e, '905d', " + NUTCH_CONFIG_FILE + " ]" + END_OF_LINE  // num handlers
                + " - [ sed, -i.bak, '904 a\\<value>" + cpus + "</value>', " + NUTCH_CONFIG_FILE + " ] " + END_OF_LINE
                + " - sed -i.bak -e '627d' " + NUTCH_CONFIG_FILE + END_OF_LINE  // fetcher threads
                + " - sed -i.bak '626 a\\<value>" + cpus + "</value>' " + NUTCH_CONFIG_FILE + END_OF_LINE
                + " - sed -i.bak -e '634d' " + NUTCH_CONFIG_FILE + END_OF_LINE // fetcher threads per host
                + " - sed -i.bak '633 a\\<value>" + cpus + "</value>' " + NUTCH_CONFIG_FILE + END_OF_LINE
                + " - sed -i.bak -e '641d' " + NUTCH_CONFIG_FILE + END_OF_LINE // fetcher threads per host by ip (true)
                + " - sed -i.bak '640 a\\<value>true</value>' " + NUTCH_CONFIG_FILE + END_OF_LINE;
    }

    private static String generatePrintTimestampStartCommand() {
        return " - echo \"timestamp_start:$(date +%s)\"";
    }

}