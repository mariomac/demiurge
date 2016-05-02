package es.bsc.demiurge.core.predictors;

import java.util.ArrayList;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class ArrivalsWorkloadManager {
    private String workloadPredictionsFile;

    public ArrivalsWorkloadManager(String workloadPredictionsFile) {
        this.workloadPredictionsFile = workloadPredictionsFile;
    }








    public double getWindowWorkload(ArrayList<Integer> workloadProfile, int start, int window){

        if (window > workloadProfile.size()){
            throw new ArrayIndexOutOfBoundsException("The energy profile is not long enough. You need more samples");
        }

        int endLoop = window;
        double lowerValue = Double.MAX_VALUE;
        double sum = 0.0;

        for (int i = start; i < endLoop; i++){
            //System.out.println("i: " + i);
            sum += workloadProfile.get(i);
        }

        //System.out.println(lowerValue);
        return sum;
    }
}
