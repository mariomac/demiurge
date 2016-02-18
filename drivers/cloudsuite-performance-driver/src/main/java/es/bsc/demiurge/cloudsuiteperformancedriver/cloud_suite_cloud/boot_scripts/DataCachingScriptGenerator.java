package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.boot_scripts;

import es.bsc.demiurge.cloudsuiteperformancedriver.models.VmSize;

import java.util.HashMap;
import java.util.Map;

public class DataCachingScriptGenerator {

    private static final String END_OF_LINE = System.getProperty("line.separator");
    private static final String LOADER_PATH = "/home/ubuntu/memcached/memcached_client/";
    private static final String TWITTER_DATASET_PATH = "/home/ubuntu/memcached/twitter_dataset/";

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
                + memCachedStartCommand(vmSize.getCpus(), vmSize.getRamGb()) + END_OF_LINE
                + memCachedWarmUpCommand(vmSize.getRamGb()) + END_OF_LINE
                + generatePrintTimestampStartCommand() + END_OF_LINE
                + generateExecuteBenchmarkManyTimesCommand() + END_OF_LINE;
    }

    private static String generateExecuteBenchmarkManyTimesCommand() {
        // We want the VM to run for a long time.
        // We do not want the benchmark to limit the kind of workloads we can create.
        return " - for i in `seq 1000`; do " + memCachedBenchmarkCommand() + ";sleep 15; done" + END_OF_LINE;
    }

    private static String memCachedStartCommand(int cpus, int ramMb) {
        return " - [ memcached, -d, -u, nobody, -t, " + cpus + ", -m, " + ramMb + ", -n, 550]";
    }

    private static String memCachedWarmUpCommand(int ramMb) {
        return " - [ " + LOADER_PATH + "loader, -a, "
                + TWITTER_DATASET_PATH + "twitter_dataset_unscaled," + " -o, "
                + TWITTER_DATASET_PATH + "twitter_dataset_2x," + " -s, "
                + LOADER_PATH + "servers.txt, -w, 1, -S, 2, -D, "
                + ramMb + ", -j, -T, 5 ]";
    }

    private static String memCachedBenchmarkCommand() {
        return LOADER_PATH + "loader -a "
                + TWITTER_DATASET_PATH + "twitter_dataset_2x " + " -s "
                + LOADER_PATH + "servers.txt -g 0.8 -T 90 -c 200 -w 8 -t 130";
    }

    private static String generatePrintTimestampStartCommand() {
        return " - echo \"timestamp_start:$(date +%s)\"";
    }

}
