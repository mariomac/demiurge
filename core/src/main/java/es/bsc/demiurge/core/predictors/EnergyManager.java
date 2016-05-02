package es.bsc.demiurge.core.predictors;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class EnergyManager {
    private String energyPredictionsFile;

    /*private ArrayList<Double> totalEnergy;
    private ArrayList<Double> greenEnergy;
    private ArrayList<Double> RES;*/

    public EnergyManager(String energyPredictionsFile) {
        this.energyPredictionsFile = energyPredictionsFile;

    }

    public double getWindowEnergy(ArrayList<Double> energyProfile, int start, int window){

        if (window > energyProfile.size()){
            throw new ArrayIndexOutOfBoundsException("The energy profile is not long enough. You need more samples");
        }

        int endLoop = window;
        double lowerValue = Double.MAX_VALUE;
        double sum = 0.0;

        for (int i = start; i < endLoop; i++){
            //System.out.println("i: " + i);
                sum += energyProfile.get(i);
        }

        //System.out.println(lowerValue);
        return sum;
    }

    public int getBestWindowEnergy(ArrayList<Double> energyProfile, int start, int end, int window){

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


    public ArrayList<Double> readTotalEnergyCsvFile() {

        ArrayList<Double> totalEnergy = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(this.energyPredictionsFile));
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

    public ArrayList<Double> readGreenEnergyCsvFile() {
        ArrayList<Double> greenEnergy = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(this.energyPredictionsFile));
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
                    greenEnergy.add(Double.parseDouble(cols[2]));
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return greenEnergy;


    }

    public ArrayList<Double> readRESCsvFile() {
        ArrayList<Double> resEnergy = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(this.energyPredictionsFile));
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


}
