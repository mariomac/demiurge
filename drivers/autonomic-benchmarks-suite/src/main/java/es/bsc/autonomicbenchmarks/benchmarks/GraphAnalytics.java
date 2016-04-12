package es.bsc.autonomicbenchmarks.benchmarks;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import es.bsc.autonomicbenchmarks.benchmarks.scriptgenerators.GraphAnalyticsScriptGenerator;
import es.bsc.autonomicbenchmarks.utils.JschManager;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 **/

public class GraphAnalytics extends GenericBenchmark {

    private static Logger logger = LogManager.getLogger(GraphAnalytics.class);

    private static final GraphAnalyticsScriptGenerator graphAnalyticsScriptGenerator = new GraphAnalyticsScriptGenerator();
    private static final String vmUsername = "ubuntu";
    private static final String vmUserPassword = "bsc";
    private static final String fileInsideVM = "/home/ubuntu/graphAnalytics_results.txt";
    private static String consoleFileName;

    public GraphAnalytics(int cpus, int ramGb, int diskGb, int runningTime, String benchmarkName) {
        super(cpus, ramGb, diskGb, runningTime, benchmarkName);
    }

    @Override
    public void executeBenchmark() {
        // Graph Analytics: this benchmark has been executed automatically within the init script
    }

    @Override
    public String getInitScript() {
        return graphAnalyticsScriptGenerator.generateScript(cpuValue);
    }

    @Override
    protected boolean stepConfiguredBenchmark() {
        return isBenchmarkRunning(getVmConsoleOutputLines());
    }

    @Override
    protected boolean stepExecutedBenchmark() {

        long currentTimestamp = System.currentTimeMillis() / 1000;
        if (currentTimestamp - timestampStartBenchmark >= this.runningTime){
            List<String> output = getVmConsoleOutputLines();
            writeResultToCsv(getExecutionTime(output));
            return true;
        }else{
            return false;
        }

        /* jsch manager version
        String vmIP = this.ipAddress;
        JschManager jmanager = null;
        try {
            jmanager = new JschManager(, vmUsername, vmUserPassword);
        } catch (JSchException e) {
            logger.error("Error in connecting (IP: " + vmIP + ") to the VM " + vmId);
            e.printStackTrace();
        }


        logger.info("Waiting for the end");
        while (!getVmOutputJSCHLineContainingWord(resultsPath, jmanager, "timestamp_end:", cpuValue, ramGbValue, diskGbValue)) {
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                logger.info("Cleaning iptables");
                e.printStackTrace();
            }
        }
        jmanager.close();

        logger.info("Benchmark terminated");
        */

    }


    private static List<String> getVmConsoleOutputLinesEndExperiment(String vmId, int cpus, int ramGb, int diskGb) {

        File resultsFile = new File(consoleFileName);
        try {
            return Files.readLines(resultsFile, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


    private static boolean getVmOutputJSCHLineContainingWord(String resultsPath, JschManager jmanager, String wordToFind, int cpus, int ramGb, int diskGb) {

        String s = jmanager.sendCommand("cat " + fileInsideVM);
        String lines[] = s.split("\\r?\\n");

        for (String line : lines){
            logger.debug(line);
            if (line.startsWith(wordToFind)) {

                //  Copy benchmark log
                try {
                    consoleFileName = resultsPath + cpus + "_" + ramGb + "_" + diskGb + ".log";
                    File f = new File(consoleFileName);
                    if(f.exists() && !f.isDirectory()) {
                        Long t = System.currentTimeMillis() / 1000;
                        consoleFileName = resultsPath + cpus + "_" + ramGb + "_" + diskGb + "_" + t.toString() +".log";
                    }

                    FileUtils.writeStringToFile(new File(consoleFileName), s);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
        return false;
    }


    private static double getExecutionTime(List<String> vmConsoleOutputLines) {
        for (String line: vmConsoleOutputLines) {
            if (line.startsWith("real_time")) {
                return Double.parseDouble(line.split(" ")[1]);
            }
        }
        //throw new RuntimeException("Error while reading execution time.");
        return -1;
    }

}
