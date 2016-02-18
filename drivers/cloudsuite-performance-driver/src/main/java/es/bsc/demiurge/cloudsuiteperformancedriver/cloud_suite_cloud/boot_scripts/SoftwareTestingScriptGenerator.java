package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.boot_scripts;

import es.bsc.demiurge.cloudsuiteperformancedriver.models.VmSize;

import java.util.HashMap;
import java.util.Map;

public class SoftwareTestingScriptGenerator {

    private static final String HOSTS_FILE = "/home/ubuntu/cloud9-cloudsuite/cloud9/infra/hosts/bsc.hosts";
    private static final String EXP_FILE = "/home/ubuntu/cloud9-cloudsuite/cloud9/infra/exp/bsc.exp";
    private static final String KLEE_STATS_PATH =
            "/home/ubuntu/cloud9-cloudsuite/cloud9/Release+Asserts/bin/klee-stats";
    private static final String END_OF_LINE = System.getProperty("line.separator");

    private static final int RUN_TIME_SECONDS = 600;

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
                + generateAddAuthorizedKeysCommand() + END_OF_LINE
                + generateModifyHostsFileCommand(vmSize.getCpus()) + END_OF_LINE
                + generateModifyExpDetailsCommand(vmSize.getCpus()) + END_OF_LINE
                + generateExecuteBenchmarkManyTimesCommand(RUN_TIME_SECONDS) + END_OF_LINE
                + generatePrintResultsCommand() + END_OF_LINE
                + END_OF_LINE;
    }

    private static String generateAddAuthorizedKeysCommand() {
        return " - cat /root/.ssh/id_rsa.pub >> /root/.ssh/authorized_keys" + END_OF_LINE
                + " - cat /root/.ssh/id_rsa.pub >> /home/ubuntu/.ssh/authorized_keys" + END_OF_LINE
                + " - cat /etc/ssh/ssh_host_rsa_key.pub >> /root/.ssh/authorized_keys" + END_OF_LINE
                + " - cat /etc/ssh/ssh_host_rsa_key.pub >> /home/ubuntu/.ssh/authorized_keys";
    }

    private static String generateModifyHostsFileCommand(int cpus) {
        return " - sed -i.bak -e '2d' " + HOSTS_FILE + " " + END_OF_LINE
                + " - sed -i.bak '1 a\\localhost      " + cpus + "            /home/ubuntu/cloud9-cloudsuite/cloud9        root           /home/ubuntu/cloud9-cloudsuite/cloud9/log /home/ubuntu/cloud9-cloudsuite/cloud9/targets' " + HOSTS_FILE;
    }

    private static String generateModifyExpDetailsCommand(int cpus) {
        String newLine = "printf " + cpus + " localhost " + cpus;
        return " - sed -i '1s/.*/" + newLine + "/' " + EXP_FILE;
    }

    private static String generateExecuteBenchmarkManyTimesCommand(int seconds) {
        return " - for i in `seq 1000`; do " + runExperimentCommand(seconds) + ";sleep 15; done";
    }

    private static String runExperimentCommand(int seconds) {
        return " cd /home/ubuntu/cloud9-cloudsuite/cloud9/infra;"
                + "su ubuntu;"
                + "/usr/bin/time -f \"real_time %e\" ./run-experiment.py -t " + seconds
                + " bsc coreutils bsc coreutils coreutils";
    }

    private static String generatePrintResultsCommand() {
        return " - " + KLEE_STATS_PATH + " /home/ubuntu/cloud9-cloudsuite/cloud9/log/last/";
    }

    private static String generatePrintTimestampStartCommand() {
        return " - echo \"timestamp_start:$(date +%s)\"";
    }

}

