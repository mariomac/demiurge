package es.bsc.autonomicbenchmarks.benchmarks;

import es.bsc.autonomicbenchmarks.benchmarks.scriptgenerators.DataCachingScriptGenerator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 **/

public class DataCaching extends GenericBenchmark {

    private static Logger logger = LogManager.getLogger(DataCaching.class);
    private static final DataCachingScriptGenerator dataCachingScriptGenerator = new DataCachingScriptGenerator();

    public DataCaching(int cpus, int ramGb, int diskGb, int runningTime, String benchmarkName) {
        super(cpus, ramGb, diskGb, runningTime, benchmarkName);
    }


    private static double getAvgRequestsPerSecond(List<String> vmConsoleOutputLines) {
        for (int i = vmConsoleOutputLines.size() - 1; i >= 0; --i) {
            if (vmConsoleOutputLines.get(i).startsWith("  timeDiff,     rps,")) {
                String outputLineWithRps = vmConsoleOutputLines.get(i + 1);
                return Double.parseDouble(outputLineWithRps.split(",")[1]);
            }
        }
        //throw new RuntimeException("Error while getting rps from console output.");
        return -1;
    }

    @Override
    public void executeBenchmark() {
        // Data caching: this benchmark has been executed automatically within the init script
    }

    @Override
    public String getInitScript() {
        if (runningTime == 0){
            this.runningTime = 300;
        }
        return dataCachingScriptGenerator.generateScript(this.cpuValue, this.ramGbValue*1024, runningTime);
    }

    @Override
    protected boolean stepConfiguredBenchmark() {
        return isBenchmarkRunning(getVmConsoleOutputLines());
    }

    @Override
    protected boolean stepExecutedBenchmark() {
        if (hasBenchmarkTerminated(getVmConsoleOutputLines())){
            List<String> output = getVmConsoleOutputLines();
            writeResultToCsv(getAvgRequestsPerSecond(output));
            return true;
        }else{
            return false;
        }
    }
}
