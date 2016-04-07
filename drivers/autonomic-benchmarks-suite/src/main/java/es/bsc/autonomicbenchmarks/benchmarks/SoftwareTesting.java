package es.bsc.autonomicbenchmarks.benchmarks;

import es.bsc.autonomicbenchmarks.benchmarks.scriptgenerators.SoftwareTestingScriptGenerator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class SoftwareTesting extends GenericBenchmark {

    private static Logger logger = LogManager.getLogger(SoftwareTesting.class);
    private static final SoftwareTestingScriptGenerator softwareTestingScriptGenerator = new SoftwareTestingScriptGenerator();

    public SoftwareTesting(int cpus, int ramGb, int diskGb, int runningTime, String benchmarkName) {
        super(cpus, ramGb, diskGb, runningTime, benchmarkName);
    }

    @Override
    public String getInitScript() {
        return softwareTestingScriptGenerator.generateScript(cpuValue, runningTime);
    }

    @Override
    public boolean stepConfiguredBenchmark() {
        return isBenchmarkRunning(getVmConsoleOutputLines());

    }


    @Override
    public boolean stepExecutedBenchmark() {
        if (vmExecutionIsFinished(getVmConsoleOutputLines())){
            List<String> output = getVmConsoleOutputLines();
            writeResultToCsv(getResults(output, cpuValue));
            return true;
        }else{
            return false;
        }
    }

    private static boolean vmExecutionIsFinished(List<String> vmConsoleOutputLines) {
        for (String line: vmConsoleOutputLines) {
            if (line.contains("ICov(%) | BCov(%)")) {
                return true;
            }
        }
        return false;
    }
    private static double getResults(List<String> vmConsoleOutputLines, int cpus) {
        String resultsLine;
        if (cpus == 1) {
            resultsLine = "| worker-1 | ";
        }
        else {
            resultsLine = "| Total (";
        }

        for (int i = vmConsoleOutputLines.size() - 1; i >= 0; --i) {
            if (vmConsoleOutputLines.get(i).contains(resultsLine)) {
                return Double.parseDouble(vmConsoleOutputLines.get(i).split("\\|")[2])/1000;
            }
        }
        //throw new RuntimeException("Error while getting code coverage from VM console output.");
        return -1;
    }


    @Override
    public void executeBenchmark() {
        // Software testing: this benchmark has been executed automatically within the init script
    }



}





