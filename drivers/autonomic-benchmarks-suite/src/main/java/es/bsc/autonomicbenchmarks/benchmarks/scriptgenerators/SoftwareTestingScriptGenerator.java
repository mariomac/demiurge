package es.bsc.autonomicbenchmarks.benchmarks.scriptgenerators;


public class SoftwareTestingScriptGenerator {

    private static final String HOSTS_FILE = "/home/ubuntu/cloud9-cloudsuite/cloud9/infra/hosts/bsc.hosts";
    private static final String EXP_FILE = "/home/ubuntu/cloud9-cloudsuite/cloud9/infra/exp/bsc.exp";
    private static final String KLEE_STATS_PATH = 
            "/home/ubuntu/cloud9-cloudsuite/cloud9/Release+Asserts/bin/klee-stats";
    private static final String END_OF_LINE = System.getProperty("line.separator");

    public String generateScript(int cpus, int maxTimeSeconds) {
        return "#cloud-config" + END_OF_LINE
                + "password: bsc" + END_OF_LINE
                + "chpasswd: { expire: False }" + END_OF_LINE
                + "ssh_pwauth: True" + END_OF_LINE
                + "runcmd:" + END_OF_LINE
                + generatePrintTimestampStartCommand() + END_OF_LINE
                + generateAddAuthorizedKeysCommand() + END_OF_LINE
                + generateModifyHostsFileCommand(cpus) + END_OF_LINE
                + generateModifyExpDetailsCommand(cpus) + END_OF_LINE
                + generateRunExperimentCommand(maxTimeSeconds) + END_OF_LINE
                + generatePrintResultsCommand() + END_OF_LINE
                + END_OF_LINE;
    }


    private String generateAddAuthorizedKeysCommand() {
        return " - cat /root/.ssh/id_rsa.pub >> /root/.ssh/authorized_keys" + END_OF_LINE
                + " - cat /root/.ssh/id_rsa.pub >> /home/ubuntu/.ssh/authorized_keys" + END_OF_LINE
                + " - cat /etc/ssh/ssh_host_rsa_key.pub >> /root/.ssh/authorized_keys" + END_OF_LINE
                + " - cat /etc/ssh/ssh_host_rsa_key.pub >> /home/ubuntu/.ssh/authorized_keys";
    }
    
    private String generateModifyHostsFileCommand(int cpus) {
        return " - sed -i.bak -e '2d' " + HOSTS_FILE + " " + END_OF_LINE
                + " - sed -i.bak '1 a\\localhost      " + cpus + "            /home/ubuntu/cloud9-cloudsuite/cloud9        root           /home/ubuntu/cloud9-cloudsuite/cloud9/log /home/ubuntu/cloud9-cloudsuite/cloud9/targets' " + HOSTS_FILE;
    }
    
    private String generateModifyExpDetailsCommand(int cpus) {
        String newLine = "printf " + cpus + " localhost " + cpus;
        return " - sed -i '1s/.*/" + newLine + "/' " + EXP_FILE;
    }
    
    private String generateRunExperimentCommand(int seconds) {
        return " - cd /home/ubuntu/cloud9-cloudsuite/cloud9/infra" + END_OF_LINE
               // + " - su ubuntu" + END_OF_LINE
                + " - /usr/bin/time -f \"real_time %e\" ./run-experiment.py -t " + seconds
                + " bsc coreutils bsc coreutils coreutils";
    }
    
    private String generatePrintResultsCommand() {
        return " - " + KLEE_STATS_PATH + " /home/ubuntu/cloud9-cloudsuite/cloud9/log/last/";
    }

    private String generatePrintTimestampStartCommand() {
        return " - echo \"timestamp_start:$(date +%s)\"";
    }
    
}
