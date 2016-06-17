package es.bsc.demiurge.core.predictors;

import es.bsc.demiurge.core.configuration.Config;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class EnergyManager {
    private final Logger logger = LogManager.getLogger(EnergyManager.class);
    private final static int MAX_GREEN_ENERGY = 21057;
    private String energyPredictionsFile;
    private String energyProfileFile = "";

    private final String TIME_LABEL = "Time";
    private final String TOTAL_ENERGY_LABEL = "Energia.total";
    private final String RENEWABLE_ENERGY_LABEL = "Energia.renovable";
    private final String RES_LABEL = "RES";

    /*
    private ArrayList<Double> totalEnergy;
    private ArrayList<Double> greenEnergy;
    private ArrayList<Double> RES;*/

    public EnergyManager(String energyPredictionsFile) {
        this.energyPredictionsFile = energyPredictionsFile;
        //this.energyProfileFile = energyProfileFile;
    }

    public double getWindowPredictionEnergy(long start, int window) {
        ArrayList<Double> energyProfile = null;
        try {
            energyProfile = readPredictedEnergyCsvFile();
        } catch (FileNotFoundException e) {
            return 0;
        }
        ArrayList<Long> timeProfile = readIndexPredictedEnergyCsvFile();

        if (window > energyProfile.size()) {
            logger.info("The energy profile is not long enough. You need more samples: returning 0!");
            window = energyProfile.size();
            //return 0;
        }
        /*
        long firstPredictionTime = 0;
        try{
            firstPredictionTime = timeProfile.get(0);
        }catch(NullPointerException e){
            logger.error(e.getMessage());
            return 0;
        }*/

        double sum = 0.0;

        for (int i = 0; i < energyProfile.size(); i++) {
            if (timeProfile.get(i) >= start && timeProfile.get(i) <= start+window) {
                sum += energyProfile.get(i);
            }
        }

        //System.out.println(lowerValue);

        return sum;

    }


    private ArrayList<Double> readPredictedEnergyCsvFile() throws FileNotFoundException {

        ArrayList<Double> totalEnergy = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(this.energyPredictionsFile));
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Green energy prediction file has not been generated yet");
        }
        String line;
        boolean firstLine = true;
        int counter = 0;
        try {
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] cols = line.split(",");
                //System.out.println("Coulmn 2= " + cols[2] + " , Column 3=" + cols[3]);

                //Ignore header
                if (firstLine) {
                    firstLine = false;
                } else {
                    double val = Double.parseDouble(cols[1]);
                    if (val > MAX_GREEN_ENERGY){
                        val = MAX_GREEN_ENERGY;
                    }
                    totalEnergy.add(val);
                }
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return totalEnergy;
    }
    private ArrayList<Long> readIndexPredictedEnergyCsvFile() {

        ArrayList<Long> totalEnergy = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(this.energyPredictionsFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line;
        boolean firstLine = true;
        int counter = 0;
        try {
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] cols = line.split(",");
                //System.out.println("Coulmn 2= " + cols[2] + " , Column 3=" + cols[3]);

                //Ignore header
                if (firstLine) {
                    firstLine = false;
                } else {
                    totalEnergy.add(Long.parseLong(cols[0]));
                }
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return totalEnergy;
    }


    public void setEnergyProfileFile(String energyProfileFile) {
        this.energyProfileFile = energyProfileFile;
    }


    public EnergyFileModel getEnergyUsageAtTime(long time){

        if (energyProfileFile == ""){
            energyProfileFile = Config.INSTANCE.energyProfilesFile;
        }

        ArrayList<Long> totalEnergy = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(energyProfileFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line;
        boolean firstLine = true;

        int indexTime=0;
        int indexTotalEnergy=0;
        int indexRenewableEnergy=0;
        int indexRES=0;
        EnergyFileModel energyFileModel;
        try {
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] cols = line.split(",");
                //System.out.println("Coulmn 2= " + cols[2] + " , Column 3=" + cols[3]);
                //Ignore header
                if (firstLine) {
                    // Get indexes
                    for (int i = 0; i < cols.length; i++){

                        switch (cols[i]) {
                            case TIME_LABEL: indexTime = i;
                                break;
                            case TOTAL_ENERGY_LABEL: indexTotalEnergy = i;
                                break;
                            case RENEWABLE_ENERGY_LABEL: indexRenewableEnergy = i;
                                break;
                            case RES_LABEL: indexRES = i;
                                break;
                            default: return null;
                        }
                    }
                    firstLine = false;
                } else {
                    if (Integer.parseInt(cols[indexTime]) == time){
                        energyFileModel = new EnergyFileModel(Long.parseLong(cols[indexTime]), Integer.parseInt(cols[indexTotalEnergy]), Integer.parseInt(cols[indexRenewableEnergy]), Double.parseDouble(cols[indexRES]));
                        return energyFileModel;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return null;




    }


/*
    public double getWindowEnergy(int start, int window) {

        int maxSamples = start + window + 2;
        ArrayList<Double> energyProfile = readGreenEnergyCsvFile(maxSamples);


        if (window > energyProfile.size()) {
            throw new ArrayIndexOutOfBoundsException("The energy profile is not long enough. You need more samples");
        }

        int endLoop = start + window;
        double sum = 0.0;

        for (int i = start; i <= endLoop; i++) {
            //System.out.println("i: " + i);
            sum += energyProfile.get(i);
        }

        //System.out.println(lowerValue);
        return sum;
    }


    public ArrayList<Double> readGreenEnergyCsvFile(int maxSamples) {
        ArrayList<Double> greenEnergy = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(this.energyProfileFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line;
        boolean firstLine = true;
        int counter = 0;
        try {
            //while ((line = br.readLine()) != null) {
            while ((line = br.readLine()) != null && counter < maxSamples) {
                // use comma as separator
                String[] cols = line.split(",");
                //System.out.println("Coulmn 2= " + cols[2] + " , Column 3=" + cols[3]);

                //Ignore header
                if (firstLine) {
                    firstLine = false;
                } else {
                    greenEnergy.add(Double.parseDouble(cols[2]));
                }
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return greenEnergy;
    }


    public ArrayList<Double> readTotalEnergyCsvFile() {

        ArrayList<Double> totalEnergy = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(this.energyProfileFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line;
        boolean firstLine = true;
        try {
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] cols = line.split(",");
                //System.out.println("Coulmn 2= " + cols[2] + " , Column 3=" + cols[3]);

                //Ignore header
                if (firstLine){
                    firstLine = false;
                }else{
                    totalEnergy.add(Double.parseDouble(cols[1]));
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return totalEnergy;


    }


    public ArrayList<Double> readRESCsvFile() {
        ArrayList<Double> resEnergy = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(this.energyProfileFile));
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
                    resEnergy.add(Double.parseDouble(cols[3]));
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resEnergy;

    }

    public int getBestWindowEnergy(int start, int end, int window){

        ArrayList<Double> energyProfile = readPredictedEnergyCsvFile();
        if (end > energyProfile.size()){
            throw new ArrayIndexOutOfBoundsException("The energy profile is not long enough. You need more samples");
        }

        int endLoop = end - window;
        double lowerValue = 0;
        int bestStart = 0;

        for (int i = start; i < endLoop; i++){
            //System.out.println("i: " + i);
            double sum = 0.0;
            for (int j = i; j < i+window; j++){
                //System.out.println("\tj: " + j);
                sum += energyProfile.get(j);
            }

            if (sum != 0.0 && sum > lowerValue){
                lowerValue = sum;
                bestStart = i;
            }

        }
        //System.out.println(lowerValue);
        return bestStart;
    }

    */

}
