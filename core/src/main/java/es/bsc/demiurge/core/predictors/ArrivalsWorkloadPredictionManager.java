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

import static es.bsc.demiurge.core.utils.FileSystem.deleteFile;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class ArrivalsWorkloadPredictionManager implements Runnable {
    private final Logger logger = LogManager.getLogger(ArrivalsWorkloadPredictionManager.class);

    final BlockingQueue<Vm> workloadNewArrivals;

    private static int indexPower = 0;
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
        writeHeader(workloadProfilesFile);
    }

    @Override
    public void run() {
        logger.info("Workload prediction start time:" + startTime);

        while (true) {

            // Wait 10 seconds before checking again
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //get past power


            // add power from queue of last vms


            // predict next values


            // empty queue



        }
    }


    public double getEstimatedPowerForBenchmark(String benchmark){
        double defaultPower = 30.00;
        writeHeader(tempFileInput);
        List<Double> pastPower = db.getPastPowerForBenchmark(benchmark, maxInputSamples);

        if (pastPower.size() == 0){
            deleteFile(tempFileInput);
            return defaultPower;
        }

        for (Double p : pastPower){
            writePastPower(indexPower, p, tempFileInput);
            indexPower += 1;
        }

        long now = System.currentTimeMillis() / 1000 - startTime;
        predictValues(now, tempFileInput, tempFileOut, 1);

        ArrayList<Double> res = readOutputCsvFile(tempFileOut);

        try{
            defaultPower = res.get(0);

        }catch (IndexOutOfBoundsException e){
            logger.error("Error in predicting benchmark power from past arrivals");
            logger.error(e.getMessage());
        }finally {
            deleteFile(tempFileInput);
            deleteFile(tempFileOut);
            return defaultPower;
        }

    }

    public double getEstimatedPowerForBenchmark(String benchmark, long now){

        double defaultPower = 30.00;
        writeHeader(tempFileInput);
        List<Double> pastPower = db.getPastPowerForBenchmark(benchmark, maxInputSamples);

        if (pastPower.size() == 0){
            deleteFile(tempFileInput);
            return defaultPower;
        }

        for (Double p : pastPower){
            writePastPower(indexPower, p, tempFileInput);
            indexPower += 1;
        }
        predictValues(now, tempFileInput, tempFileOut, 1);

        ArrayList<Double> res = readOutputCsvFile(tempFileOut);
        try{
            defaultPower = res.get(0);

        }catch (IndexOutOfBoundsException e){
            logger.error("Error in predicting benchmark power from past arrivals");
            logger.error(e.getMessage());
        }finally {
            deleteFile(tempFileInput);
            deleteFile(tempFileOut);
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
        return workloadNewArrivals.offer(b);
    }

    public void removeBenchmarkFromQueue(Vm b){
        // Remove Benchmark from both the queues
        workloadNewArrivals.remove(b);
    }


    public void predictValues(long now, String inputFile, String outputFile, int valuesToPredict){
        //log.info("Predicting");
        String RscriptPath = this.rScript;

        try {
            String line;
            Process p = Runtime.getRuntime().exec(new String[]{RscriptPath, inputFile, Integer.toString(valuesToPredict), Long.toString(now), Integer.toString(this.maxInputSamples), outputFile});
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


    public ArrayList<Double> readOutputCsvFile(String file) {
        ArrayList<Double> resEnergy = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
}
