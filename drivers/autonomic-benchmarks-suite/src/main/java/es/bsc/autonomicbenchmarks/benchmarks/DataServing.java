package es.bsc.autonomicbenchmarks.benchmarks;

import es.bsc.autonomicbenchmarks.benchmarks.scriptgenerators.DataServingScriptGenerator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class DataServing extends GenericBenchmark {

    private static Logger logger = LogManager.getLogger(DataServing.class);
    private static final DataServingScriptGenerator dataServingScriptGenerator = new DataServingScriptGenerator();

    public DataServing(int cpus, int ramGb, int diskGb, int runningTime, String benchmarkName) {
        super(cpus, ramGb, diskGb, runningTime, benchmarkName);
    }

    @Override
    public void executeBenchmark() {
        // Data serving: this benchmark has been executed automatically within the init script
    }

    @Override
    public String getInitScript() {
        return dataServingScriptGenerator.generateScript(cpuValue);
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
            writeResultToCsv(getAvgOpsSec(output));
            return true;
        }else{
            return false;
        }
    }

    private static double getAvgOpsSec(List<String> vmConsoleOutputLines) {
        double sumAvgOpsSec = 0.0;
        int samples = 0;
        for (String line: vmConsoleOutputLines) {
            if (line.contains("READ AverageLatency(us)=")) {
                sumAvgOpsSec += Double.parseDouble(line.split(";")[1].split("current")[0]);
                ++samples;
            }
        }
        if (samples == 0){
            return -1;
        }else{
            return sumAvgOpsSec/samples;
        }

    }
}
