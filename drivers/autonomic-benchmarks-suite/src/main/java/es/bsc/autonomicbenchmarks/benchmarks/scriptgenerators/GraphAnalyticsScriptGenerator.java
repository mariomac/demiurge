package es.bsc.autonomicbenchmarks.benchmarks.scriptgenerators;

public class GraphAnalyticsScriptGenerator {
    
    private static final String TUNKRANK_PATH = "/home/ubuntu/graph-release/release/toolkits/graph_analytics";
    private static final String END_OF_LINE = System.getProperty("line.separator");

    public String generateScript(int cpus) {
        return "#cloud-config" + END_OF_LINE
                + "password: bsc" + END_OF_LINE
                + "chpasswd: { expire: False }" + END_OF_LINE
                + "ssh_pwauth: True" + END_OF_LINE
                + "runcmd:" + END_OF_LINE
                + " - sleep 20" + END_OF_LINE
                + generatePrintTimestampStartCommand() + END_OF_LINE
                + " - [ cd, " + TUNKRANK_PATH + " ]" + END_OF_LINE
                + generateTunkrankExecution(cpus) + END_OF_LINE
                + END_OF_LINE;
    }
    
    private String generateTunkrankExecution(int ncpus) {
        return " - date +%s > timestamp_start.txt" + END_OF_LINE
                + " - echo \"timestamp_start:$(date +%s)\" > /home/ubuntu/graphAnalytics_results.txt" + END_OF_LINE
                + " - /usr/bin/time -f \"real_time %e\" " +
                "./tunkrank --graph=/home/ubuntu/Twitter-dataset/data/twitter_small_data_graplab.in --format=tsv " +
                //"--ncpus=" + ncpus + " --engine=asynchronous &> a.txt";
                "--ncpus=" + ncpus + " --engine=asynchronous 2>&1 | tee -a /home/ubuntu/graphAnalytics_results.txt" +  END_OF_LINE
                + " - echo \"timestamp_end:$(date +%s)\" >> /home/ubuntu/graphAnalytics_results.txt" + END_OF_LINE
                + END_OF_LINE;

    }
    
    private String generatePrintTimestampStartCommand() {
        return " - echo \"timestamp_start:$(date +%s)\"";
    }
    
}
