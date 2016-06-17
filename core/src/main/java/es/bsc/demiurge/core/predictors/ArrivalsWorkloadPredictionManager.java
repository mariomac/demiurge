package es.bsc.demiurge.core.predictors;

import es.bsc.demiurge.core.db.VmManagerDb;
import es.bsc.demiurge.core.models.vms.Vm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static es.bsc.demiurge.core.manager.GenericVmManager.DEMIURGE_START_TIME;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 *
 * 1. get from db power of past benchmark
 * 2. save these values into inputFile
 * 3. apply time series (R script) -> will write outputfile
 * 4. read output file
 *
 */
public class ArrivalsWorkloadPredictionManager implements Runnable {
    private final Logger logger = LogManager.getLogger(ArrivalsWorkloadPredictionManager.class);

    private final BlockingQueue<Vm> workloadNewArrivals;

    private static int indexPower = 0;
    private static int indexPowerTotal = 0;
    private VmManagerDb db;
    private String rScript;
    private String workloadProfilesFile;
    private int windowForecast;
    private int maxInputSamples;
    private String outputFile;
    private long startTime;

    private String tempFileInput = "/tmp/predictBenchmarkInput.csv"; // for predictions of single benchmark
    private String tempFileOut = "/tmp/predictBenchmarkOutput.csv"; // for predictions of single benchmark

    public ArrivalsWorkloadPredictionManager(long startTime, VmManagerDb db, String rScript, String workloadProfilesFile, int windowForecast, int maxInputSamples, String outputFile) {
        this.db = db;
        this.workloadProfilesFile = workloadProfilesFile;
        this.rScript = rScript;
        this.windowForecast = windowForecast;
        this.maxInputSamples = maxInputSamples;
        this.outputFile = outputFile;
        this.startTime = startTime;
        this.workloadNewArrivals = new LinkedBlockingQueue<>();
        //writeHeader(workloadProfilesFile);
    }

    @Override
    public void run() {
        logger.info("Demiurge start time:" + DEMIURGE_START_TIME);

        while (true) {

            // Wait 10 seconds before checking again
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long now = System.currentTimeMillis() / 1000 - DEMIURGE_START_TIME;

            //logger.info("Arrivals prediction: " + now);
            getEstimatedPowerArrivals(now);

        }
    }

    /**
     *
     * @param now
     * @param inputFile
     * @param outputFile
     * @param valuesToPredict
     */
    public void predictValues(long now, String inputFile, String outputFile, int valuesToPredict, int interval){
        //log.info("Predicting");
        String RscriptPath = this.rScript;

        try {
            String line;
            Process p = Runtime.getRuntime().exec(new String[]{RscriptPath, inputFile, Integer.toString(valuesToPredict), Long.toString(now), Integer.toString(this.maxInputSamples), outputFile, Integer.toString(interval)});
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            while ((line = in.readLine()) != null) {
                //System.out.println(line);
            }
            in.close();

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    public List<Double> getEstimatedPowerArrivals(long now){

        //now = now - DEMIURGE_START_TIME;
        double defaultPower = 0.0;
        double vmPower = 0.0;

        TimeSeriesArrivals pastPower = db.getPastPower(200);

        //Normalize power to now
        for (int i = 0; i < pastPower.getTimeArray().size() ; i++){
            Long time = pastPower.getTimeArray().get(i);// - DEMIURGE_START_TIME;
            pastPower.getTimeArray().set(i, time);
        }


        //System.out.println("Number of VM requests in the past:" + pastPower.getTimeArray().size());

        if (pastPower.getTimeArray().size() == 0 && this.workloadNewArrivals.size() == 0){
            //deleteFile(workloadProfilesFile);
            return null;
        }

        writeHeader(workloadProfilesFile);
        /*
        // This divides the array in a way that there is at least a value inside the interval
        // Get granularity
        Integer interval = pastPower.getIntervalWithAtLeastOneValue();
        if (interval == 0){
            interval = 1;
        }
        List<Double> vals = pastPower.getSumValuesSplittedInterval(interval);
*/

        //This divides the array in equal parts MAX_TIME/NUM_SAMPLE. It can happen
        //that in some intervals there are no values. (better for timeseries).

        Integer interval = pastPower.getIntervalSamples();
        if (interval == 0){
            interval = 1;
        }
        List<Double> vals = pastPower.getSumValuesForIntervalSamples(interval);

        // Write file with past samples grouped (summed) by interval

        for (int i = 1; i <= vals.size(); i++){
            indexPowerTotal = i*interval;
            writePastPower(indexPowerTotal, vals.get(i-1), workloadProfilesFile);
        }
/*
        //Append arrivals in queue - check if we actually use this
        if (this.workloadNewArrivals.size() > 0){
            Iterator<Vm> iteratorExecuting= this.workloadNewArrivals.iterator();
            while (iteratorExecuting.hasNext()) {
                Vm vm = iteratorExecuting.next(); // must be called before you can call i.remove()
                vmPower += vm.getPowerEstimated();
                iteratorExecuting.remove();
            }
            writePastPower((int)now, vmPower, workloadProfilesFile);

        }
*/

        predictValues(now, workloadProfilesFile, outputFile, windowForecast, interval);

        ArrayList<Double> res = readOutputCsvFile(outputFile);

        try{
            defaultPower = res.get(0);
            return res;
        }catch (IndexOutOfBoundsException e){
            logger.error("Error in predicting benchmark power from past arrivals");
            logger.error(e.getMessage());
            return null;
        }catch (NullPointerException e){
            logger.warn("Not enough past samples of VM arrivals to predict the future");
            return null;
        }finally {
            //deleteFile(workloadProfilesFile);
            //deleteFile(outputFile);

            }

    }

    public double getEstimatedPowerForBenchmark(String benchmark, long now){

        double defaultPower = 30.00;
        writeHeader(tempFileInput);
        List<Double> pastPower = db.getPastPowerForBenchmark(benchmark, maxInputSamples);

        if (pastPower.size() == 0){
            //deleteFile(tempFileInput);
            return defaultPower;
        }

        for (Double p : pastPower){
            writePastPower(indexPower, p, tempFileInput);
            indexPower += 1;
        }

        predictValues(now, tempFileInput, tempFileOut, 1, 1);

        ArrayList<Double> res = readOutputCsvFile(tempFileOut);
        try{
            defaultPower = res.get(0);

        }catch (IndexOutOfBoundsException e){
            logger.error("Error in predicting benchmark power from past arrivals");
            logger.error(e.getMessage());
        }finally {
            //deleteFile(tempFileInput);
            //deleteFile(tempFileOut);
            return defaultPower;
        }

    }


    private void writePastPower(int timestamp, double totalPower, String inputFile){

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(inputFile, true)));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(timestamp + "," + totalPower);
        writer.append(timestamp + "," + totalPower + "\n");
        writer.close();
    }


    private void writeHeader(String inputFile){
        //System.out.println("header");
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(inputFile, false)));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        long timestamp = System.currentTimeMillis() / 1000;

        writer.append("time,power\n");
        writer.close();
    }

    public boolean addBenchmarkToQueue(Vm b){
        logger.debug("Adding vm to arrivals benchmark quque");
        return workloadNewArrivals.offer(b);
    }

    public void removeBenchmarkFromQueue(Vm b){
        // Remove Benchmark from both the queues
        workloadNewArrivals.remove(b);
    }




    public ArrayList<Double> readOutputCsvFile(String file) {
        ArrayList<Double> resEnergy = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.getMessage();
            return null;
        }
        String line;
        boolean firstLine = true;
        try {
            while ((line = br.readLine()) != null) {
                String[] cols = line.split(",");
                //Ignore header
                if (firstLine){
                    firstLine = false;
                }else{
                    resEnergy.add(Double.parseDouble(cols[1]));
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resEnergy;
    }

    public boolean checkEmptyQueue() {
        if (this.workloadNewArrivals.size() == 0){
            return true;
        }
        else{
            return false;
        }
    }

    public String getArrivalsPredictionFile() {
        return outputFile;
    }
}
