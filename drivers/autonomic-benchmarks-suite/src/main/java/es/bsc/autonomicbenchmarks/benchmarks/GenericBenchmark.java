package es.bsc.autonomicbenchmarks.benchmarks;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import es.bsc.autonomicbenchmarks.configuration.Conf;
import es.bsc.autonomicbenchmarks.models.VmAutonomic;
import es.bsc.autonomicbenchmarks.utils.CommandExecutor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static es.bsc.autonomicbenchmarks.utils.Utils.getCpCommand;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public abstract class GenericBenchmark {
    private static Logger logger = LogManager.getLogger(GenericBenchmark.class);
    private static final Conf conf = Conf.INSTANCE;
    protected static  String END_OF_LINE;
    protected static String instancesPath;
    protected String resultsPath;
    protected static String resultsFile;

    protected String imageID;
    protected Integer cpuValue;
    protected Integer ramGbValue;
    protected Integer diskGbValue;
    protected Integer runningTime;
    protected String ipAddress;
    protected String vmId;
    protected String benchmarkName;
    protected VmAutonomic vm;
    protected long timestampStartVM;
    protected long timestampStartBenchmark;
    protected long timestampEndBenchmark;
    public GenericBenchmark(int cpus, int ramGb, int diskGb, int runningTime, String benchmarkName) {
        this.cpuValue = cpus;
        this.ramGbValue = ramGb;
        this.diskGbValue = diskGb;
        this.runningTime = runningTime;
        this.benchmarkName = benchmarkName;
        try {

            END_OF_LINE = System.getProperty("line.separator");
            instancesPath = conf.instancesPath;
            resultsPath = conf.resultsPath;
            resultsFile = conf.resultsPath + "results.csv";

        } catch (Throwable t) {
            logger.error("Failure during configuration of Generic Benchmark", t);
            throw t;
        }

    }

    public List<String> getVmConsoleOutputLines() {

        String fname = resultsPath + vmId + "_" + cpuValue + "_" + ramGbValue + "_" + diskGbValue + ".log";

        CommandExecutor.executeCommand(getCpCommand(
                instancesPath + vmId + "/console.log",
                fname));

        File resultsFile = new File(fname);
        try {
            return Files.readLines(resultsFile, Charsets.UTF_8);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


    protected void writeResultToCsv(double result) {

        String toWrite = timestampStartVM + "," + timestampStartBenchmark + "," + timestampEndBenchmark + "," + vm.getName() + "," + benchmarkName + "," + cpuValue + "," + ramGbValue + "," + diskGbValue + "," + vm.getHostName() +","+ result + END_OF_LINE;
        try (FileWriter fileWriter = new FileWriter(resultsFile, true);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
             PrintWriter out = new PrintWriter(bufferedWriter)) {
            out.println(toWrite);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    protected static boolean hasBenchmarkTerminated(List<String> vmConsoleOutputLines) {
        for (String line : vmConsoleOutputLines) {
            if (line.startsWith("timestamp_end:")) {
               // benchmarkEndTimestamp = Long.parseLong(line.split(":")[1]);
                return true;
            }
        }
        return false;
    }

    protected static boolean isBenchmarkRunning(List<String> vmConsoleOutputLines) {
        for (String line : vmConsoleOutputLines) {
            if (line.startsWith("timestamp_start:")) {
                return true;
            }
            /*
            if (line.contains("Loading graph")) {
                return true;
            }*/
        }
        return false;
    }

    public String getVmId() {
        return vmId;
    }

    public void runBenchmark(VmAutonomic vm) {
        this.vm = vm;
        this.vmId = vm.getId();
        this.ipAddress = vm.getIpAddress();
        this.timestampStartVM = System.currentTimeMillis() / 1000;

        executeBenchmark();
    }

    public boolean stepConfigured(){
        if (stepConfiguredBenchmark()){
            this.timestampStartBenchmark = System.currentTimeMillis() / 1000;
            return true;
        }else{
            return false;
        }
    }

    /**
     * Benchmark ends depending on 2 conditions:
     *  - runningTime has expired
     *  - benchmark has terminated
     * @return true if one of the 2 condition is satisfied -> destroy VM
     */
    public boolean stepExecuted(){
        return stepExecutedBenchmark();

    }




    /**
     * Benchmark execution method
     */

    public abstract void executeBenchmark();

    /**
     * @return init script to pass during deployment
     */

    public abstract String getInitScript();


    /**
     * Thi is needed in order to wait the benchmark to be properly configured before executing the benchmark
     *
     * @return true when the benchmark has been configured
     */
    protected abstract boolean stepConfiguredBenchmark();

    /**
     * This is needed to wait until the execution is finished
     *
     * @return true when the benchmark has been executed
     */

    protected abstract boolean stepExecutedBenchmark();

    @Override
    public String toString() {
        return benchmarkName + "{ " +
                "\n\timageId: " + imageID +
                "\n\tCPUs: " + cpuValue +
                "\n\tRAM: " + ramGbValue +
                "\n\tDisk: " + diskGbValue +
                "\n\tExecution time: " + runningTime +
                "\n}";
    }

}
