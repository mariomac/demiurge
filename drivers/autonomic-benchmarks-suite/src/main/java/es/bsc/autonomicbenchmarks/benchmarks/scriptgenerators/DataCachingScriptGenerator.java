package es.bsc.autonomicbenchmarks.benchmarks.scriptgenerators;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class DataCachingScriptGenerator {

    private static final String END_OF_LINE = System.getProperty("line.separator");
    private static final String LOADER_PATH = "/home/ubuntu/memcached/memcached_client/";
    private static final String TWITTER_DATASET_PATH = "/home/ubuntu/memcached/twitter_dataset/";

    public String generateScript(int cpus, int ramMb, int time) {
        return "#cloud-config" + END_OF_LINE
                + "password: bsc" + END_OF_LINE
                + "chpasswd: { expire: False }" + END_OF_LINE
                + "ssh_pwauth: True" + END_OF_LINE
                + "runcmd:" + END_OF_LINE
                + memCachedStartCommand(cpus, ramMb) + END_OF_LINE
                + memCachedWarmUpCommand(ramMb) + END_OF_LINE
                + generatePrintTimestampStartCommand() + END_OF_LINE
                + memCachedBenchmarkCommand(time) + END_OF_LINE
                + END_OF_LINE;
    }

    private String memCachedStartCommand(int cpus, int ramMb) {
        return " - [ memcached, -d, -u, nobody, -t, " + cpus + ", -m, " + ramMb + ", -n, 550]";
    }

    private String memCachedWarmUpCommand(int ramMb) {
        return " - [ " + LOADER_PATH + "loader, -a, "
                + TWITTER_DATASET_PATH + "twitter_dataset_unscaled," + " -o, "
                + TWITTER_DATASET_PATH + "twitter_dataset_2x," + " -s, "
                + LOADER_PATH + "servers.txt, -w, 1, -S, 2, -D, "
                + ramMb + ", -j, -T, 5 ]";
    }
// time 130
    private String memCachedBenchmarkCommand(int time) {
        return " - [ " + LOADER_PATH + "loader, -a, "
                + TWITTER_DATASET_PATH + "twitter_dataset_2x," + " -s, "
                + LOADER_PATH + "servers.txt, -g, 0.8, -T, 90, -c, 200, -w, 8, -t, "+ time +"]" + END_OF_LINE
                + " - echo \"timestamp_end:$(date +%s)\"" ;
    }

    private String generatePrintTimestampStartCommand() {
        return " - echo \"timestamp_start:$(date +%s)\"";
    }


}
