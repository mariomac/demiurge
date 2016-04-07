package es.bsc.autonomicbenchmarks.benchmarks;

import es.bsc.autonomicbenchmarks.benchmarks.scriptgenerators.DataAnalyticsScriptGenerator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;

/**
 *
 * Mauro Canuto
 *
 * Wikipedia files used in this benchmark:
 * - Input: enwiki-latest-pages-articles21.xml-p013325003p015724999 (2.2G)
 * - Training: enwiki-latest-pages-articles10.xml-p000925001p001325000 (1.1G)
 *
 **/


public class DataAnalytics extends GenericBenchmark {

    private static Logger logger = LogManager.getLogger(DataAnalytics.class);

    private static final DataAnalyticsScriptGenerator dataAnalyticsScriptGenerator = new DataAnalyticsScriptGenerator();

    public DataAnalytics(int cpus, int ramGb, int diskGb, int runningTime, String benchmarkName) {
        super(cpus, ramGb, diskGb, runningTime, benchmarkName);
    }


    private static boolean vmExecutionIsFinished(List<String> vmConsoleOutputLines) {
        for (String line: vmConsoleOutputLines) {
            if (line.contains("Correctly classified:")) {
                return true;
            }
        }
        return false;
    }

    private static double getExecutionMinutes(List<String> vmConsoleOutputLines) {
        for (int i = vmConsoleOutputLines.size() - 1; i >= 0; --i) {
            if (vmConsoleOutputLines.get(i).contains("Program took")) {
                return Double.parseDouble(vmConsoleOutputLines.get(i).split("Minutes: ")[1].split("\\)")[0]);
            }
        }
        return -1;
        //throw new RuntimeException("Error while getting map% achieved.");
    }


    @Override
    public void executeBenchmark() {
        // Data analytics: this benchmark has been executed automatically within the init script
    }

    @Override
    public String getInitScript() {
        return dataAnalyticsScriptGenerator.generateScript(this.ramGbValue);
    }

    @Override
    protected boolean stepConfiguredBenchmark() {
        return isBenchmarkRunning(getVmConsoleOutputLines());
    }

    @Override
    protected boolean stepExecutedBenchmark() {

        long currentTimestamp = System.currentTimeMillis() / 1000;
        if (currentTimestamp - timestampStartBenchmark >= this.runningTime){
        //if (vmExecutionIsFinished(getVmConsoleOutputLines())){
            List<String> output = getVmConsoleOutputLines();
            writeResultToCsv(getExecutionMinutes(output));
            return true;
        }else{
            return false;
        }
    }

    @Override
    public String toString() {
        return "Running Data Analytics:{ " +
                "\n\timageId: " + imageID +
                "\n\tCPUs: " + cpuValue +
                "\n\tRAM: " + ramGbValue +
                "\n\tDisk: " + diskGbValue +
                "\n\tExecution time: " + runningTime +
                "\n}";
    }

}
