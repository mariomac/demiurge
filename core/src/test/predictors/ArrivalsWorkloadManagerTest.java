package predictors;

import es.bsc.demiurge.core.predictors.ArrivalsWorkloadManager;
import org.junit.Test;

import static es.bsc.demiurge.core.utils.FileSystem.deleteFile;
import static es.bsc.demiurge.core.utils.FileSystem.writeStringToFile;
import static es.bsc.demiurge.core.utils.FileSystem.writeToFile;
import static org.junit.Assert.assertTrue;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */


public class ArrivalsWorkloadManagerTest {
    String predictionFile = "/tmp/predictionWorkloadOut_test.csv";

    @Test
    public void getWindowWorkloadTest(){
        String header = "timestamp,value";
        writeStringToFile(predictionFile, header, false);
        writeToFile(predictionFile, 155, 5, true);
        writeToFile(predictionFile, 190, 10, true);
        writeToFile(predictionFile, 225, 15, true);

        ArrivalsWorkloadManager energyManager = new ArrivalsWorkloadManager(predictionFile);
        double result = energyManager.getWindowPredictionWorkload(180, 40);
        System.out.println(result);
        assertTrue(result == 25);
        deleteFile(predictionFile);

    }
}
