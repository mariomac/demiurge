package es.bsc.demiurge.core.predictors;


import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class EnergyPredictionManager implements Runnable {
    private final Logger logger = LogManager.getLogger(EnergyPredictionManager.class);
    private String rScript;
    private String energyProfilesFile;
    private String type;
    private int windowForecast;
    private int maxInputSamples;
    private String outputFile;
    private long startTime;


    /*private ArrayList<Double> totalEnergy;
    private ArrayList<Double> greenEnergy;
    private ArrayList<Double> RES;*/

    public EnergyPredictionManager(long startTime, String rScript, String energyProfilesFile, String type, int windowForecast, int maxInputSamples, String outputFile) {
        this.energyProfilesFile = energyProfilesFile;
        this.rScript = rScript;
        this.type = type;
        this.windowForecast = windowForecast;
        this.maxInputSamples = maxInputSamples;
        this.outputFile = outputFile;
        this.startTime = startTime;
    }

    @Override
    public void run() {
        //logger.info("Energy start time:" + DEMIURGE_START_TIME);

        while (true) {

            long now = System.currentTimeMillis() / 1000 - startTime;

            logger.info("--------- Current time: " + now +" ---------");
            predictValues(now);

            // Wait 10 seconds before checking again
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }


    public void predictValues(long now){
        //log.info("Predicting");
        String fileOutput = this.outputFile;
        String RscriptPath = this.rScript;

        try {
            String line;

            Process p = Runtime.getRuntime().exec(new String[]{RscriptPath, this.energyProfilesFile, type, Integer.toString(this.windowForecast), Long.toString(now), Integer.toString(this.maxInputSamples), fileOutput});
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


    public String getEnergyPredictionFile() {
        return outputFile;
    }
}
