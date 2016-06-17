package es.bsc.demiurge.core.predictors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class ArrivalsWorkloadManager {
    private final Logger logger = LogManager.getLogger(ArrivalsWorkloadManager.class);

    private String workloadPredictionsFile;

    public ArrivalsWorkloadManager(String workloadPredictionsFile) {
        this.workloadPredictionsFile = workloadPredictionsFile;
    }

    public double getWindowPredictionWorkload(long start, int window){


        ArrayList<Double> workloadProfile = null;
        try {
            workloadProfile = readWorkloadProfileFile();
        } catch (FileNotFoundException e) {
            return 0;
        }
        ArrayList<Long> timeProfile = readIndexWorkloadProfileFile();


        if (workloadProfile.size() == 0){
            logger.debug("The energy profile is not long enough. You need more samples: returning MAX!");
            return 0;
        }

        int endLoop = window;
        double lowerValue = Double.MAX_VALUE;
        double sum = 0.0;

        long end = start + window;
        Integer indexStart;
        Integer indexEnd;

        Long[] timeArray = timeProfile.toArray(new Long[timeProfile.size()]);
        if (start < timeProfile.get(0)){
            indexStart = 0;
        }else{
            indexStart = nearInclusive(timeArray, start);
        }
        if (end > timeProfile.get(timeProfile.size()-1)){
            indexEnd = timeProfile.size()-1;
        }else if(end < timeProfile.get(0)){
            indexEnd = 0;
        } else{
            indexEnd = nearInclusive(timeArray, end);
        }

        for (int i = indexStart; i <= indexEnd; i++){
            //System.out.println("i: " + i);
            sum += workloadProfile.get(i);
        }

        //System.out.println(lowerValue);
        return sum;
    }

    private ArrayList<Long> readIndexWorkloadProfileFile() {

        ArrayList<Long> totalWorkload = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(this.workloadPredictionsFile));
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
                    totalWorkload.add(Long.parseLong(cols[0]));
                }
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return totalWorkload;

    }

    private ArrayList<Double> readWorkloadProfileFile() throws FileNotFoundException {


        ArrayList<Double> totalWorkload = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(this.workloadPredictionsFile));
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Workload prediction file has not been generated yet");
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
                    totalWorkload.add(Double.parseDouble(cols[1]));
                }
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return totalWorkload;
    }


    /**
     * Find the index of the array nearest to the value. The values array can
     * contain only unique values. If it doesn't the first occurence of a value
     * in the values array is the one used, subsequent duplicate are ignored. If
     * the value falls outside the bounds of the array, <b>null</b> is returned
     *
     * @param array Values to search through for the nearest point
     * @param value THe value to search for the nearest neighbor in the array
     * @return The index of the array value nearest the value. null if the value
     *      is larger or smaller than any values in the array.
     */
    public static Integer nearInclusive(final Long[] array, final long value) {
        Integer i = null;
        int idx = binarySearch(array, value);
        if (idx < 0) {
            idx = -(idx) - 1;
            if (idx == 0 || idx >= array.length) {
                // Do nothing. This point is outside the array bounds return value will be null
            }
            else {
                // Find nearest point
                double d0 = Math.abs(array[idx - 1] - value);
                double d1 = Math.abs(array[idx] - value);
                i = (d0 <= d1) ? idx - 1 : idx;
            }
        }
        else {
            i = idx;
        }
        return i;
    }

    /**
     * Searches the specified array of doubles for the specified value using
     * the binary search algorithm.  The array <strong>must</strong> be sorted
     * (as by the <tt>sort</tt> method, above) prior to making this call.  If
     * it is not sorted, the results are undefined.  If the array contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found. The array can be sorted from low values to high or
     * from high values to low.
     *
     * @param a the array to be searched.
     * @param key the value to be searched for.
     * @return index of the search key, if it is contained in the list;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the list: the index of the first
     *         element greater than the key, or <tt>list.size()</tt>, if all
     *         elements in the list are less than the specified key.  Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     */
    public static int binarySearch(Long[] a, long key) {
        int index = -1;
        if (a[0] < a[1]) {
            index = Arrays.binarySearch(a, key);
        }
        else {
            index = binarySearch(a, key, 0, a.length - 1);
        }
        return index;
    }

    private static int binarySearch(Long[] a, long key, int low, int high) {
        while (low <= high) {
            int mid = (low + high) / 2;
            double midVal = a[mid];

            int cmp;
            if (midVal > key) {
                cmp = -1; // Neither val is NaN, thisVal is smaller
            }
            else if (midVal < key) {
                cmp = 1; // Neither val is NaN, thisVal is larger
            }
            else {
                long midBits = Double.doubleToLongBits(midVal);
                long keyBits = Double.doubleToLongBits(key);
                cmp = (midBits == keyBits ? 0 : (midBits < keyBits ? -1 : 1)); // (0.0, -0.0) or (NaN, !NaN)
            }

            if (cmp < 0) {
                low = mid + 1;
            }
            else if (cmp > 0) {
                high = mid - 1;
            }
            else {
                return mid; // key found
            }
        }
        return -(low + 1); // key not found.
    }
}




