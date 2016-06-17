package es.bsc.demiurge.core.predictors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class TimeSeriesArrivals {

    // at the same index you find time and power of a sample
    private ArrayList<Long> timeArray;
    private ArrayList<Double> powerArray;

    public TimeSeriesArrivals() {
        this.timeArray = new ArrayList<>();
        this.powerArray = new ArrayList<>();
    }

    public void addValue(Long time, Double power){
        this.timeArray.add(time);
        this.powerArray.add(power);
    }

    public ArrayList<Long> getTimeArray() {
        return timeArray;
    }

    public ArrayList<Double> getPowerArray() {
        return powerArray;
    }

    public void printTimeSeriesArrivals(){
        System.out.println("\tTime, Power");
        for (int i = 0; i < timeArray.size(); i++){
            System.out.println("\t" + timeArray.get(i) +", " + powerArray.get(i));
        }
    }

    /**
     * Return the smallest interval that guarantee the list can be splitted in N parts and each of them
     * will have at least 1 power value of past arrivals
     *
     * @return smallestDiff
     */
    public Integer getIntervalWithAtLeastOneValue(){
        Integer smallestDiff = 0;
        for (int i = 0; i < timeArray.size()-1; i++){

            int diff = timeArray.get(i).intValue() - timeArray.get(i+1).intValue();
            if (diff > smallestDiff){
                smallestDiff = diff;
            }
        }
        return smallestDiff;

    }

    /**
     * This method returns an array where each point corresponds to the sum of the power of the arrivals
     * predicted within the range "interval"
     * @param interval
     * @return
     */
    public List<Double> getSumValuesSplittedInterval(Integer interval){
        int size = timeArray.size();
        List<Double> resultList;

        if (size == 1) {
            resultList = new ArrayList<>();
            resultList.add(powerArray.get(0));
        } else {
            resultList = new ArrayList<>(Collections.nCopies(size + 2, 0d));
            Long min = timeArray.get(size-1);
            for (int i = 0; i < size; i++) {
                double sum = 0;
                Long timeInput = timeArray.get(i);
                Double powerInput = powerArray.get(i);
                Long indexd = Long.valueOf(0);

                if (interval == 1) {
                    indexd = timeInput - min;
                } else {
                    indexd = timeInput / interval;
                }

                int index = indexd.intValue();
                //System.out.println("Index: " + index);
                sum += powerInput;

                if (resultList.get(index) != 0) {
                    sum += resultList.get(index);
                }
                resultList.remove(index);
                resultList.add(index, sum);

            }

            //remove last elements == 0
            int s = resultList.size() - 1;
            while (resultList.get(s) == 0) {
                resultList.remove(s);
                s -= 1;
            }

            /*

            if (resultList.get(0) == 0) {
                Collections.rotate(resultList.subList(0, size + 1), -1);
                resultList.remove(size);
            }*/
        }
        return resultList;

    }


    public Integer getIntervalSamples() {
        // timeArray has DESC ordered items
        int numItems = timeArray.size();

        if (numItems == 0){
            return 0;
        }else{
            return timeArray.get(0).intValue()/numItems;
        }


    }

    public List<Double> getSumValuesForIntervalSamples(Integer interval) {

        int size = timeArray.size();
        List<Double> resultList;

        if (size == 1) {
            resultList = new ArrayList<>();
            resultList.add(powerArray.get(0));
        } else {
            resultList = new ArrayList<>(Collections.nCopies(size*10, 0d));
            for (int i = 0; i < size; i++) {
                double sum = 0;

                Long timeElement = timeArray.get(i);
                Double powerInput = powerArray.get(i);

                int index = (int) (timeElement / interval);
                sum = powerInput;
                try{
                    if (resultList.get(index) != 0) {
                        sum += resultList.get(index);
                    }
                }catch (IndexOutOfBoundsException e){
                    List<Double> resultList2 = new ArrayList<>();
                    resultList2.add(30d);
                    resultList2.add(30d);
                    resultList2.add(30d);
                    return resultList;
                }

                resultList.remove(index);
                resultList.add(index, sum);

            }
            //remove last elements == 0
            int s = resultList.size() - 1;
            while (resultList.get(s) == 0) {
                resultList.remove(s);
                s -= 1;
            }

        }
        return resultList;
    }
}
