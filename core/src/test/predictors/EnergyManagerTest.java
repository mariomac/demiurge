package  predictors;

import es.bsc.demiurge.core.predictors.EnergyManager;
import es.bsc.demiurge.core.predictors.EnergyTypes;
import org.junit.Test;

import static es.bsc.demiurge.core.utils.FileSystem.deleteFile;
import static es.bsc.demiurge.core.utils.FileSystem.writeStringToFile;
import static es.bsc.demiurge.core.utils.FileSystem.writeToFile;
import static org.junit.Assert.assertTrue;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class EnergyManagerTest {

    String predictionFile = "/tmp/predictionEnergyOut_test.csv";

    String rFile = "/home/mcanuto/ProjectsEU/demiurge/core/src/main/resources/energyPredictor.R";
    String energyFile = "/home/mcanuto/BSC/Projects/RenewIT/RES_2015.csv";
    int numForecast = 10;
    int maxInputSamples = 100;
    String type = EnergyTypes.GREEN;
/*
    @Test
    public void getWindowEnergyTest(){

        EnergyManager energyManager = new EnergyManager(energyFile,  predictionFile);
        double result = energyManager.getWindowEnergy(100, 140);
        assertTrue(result == 688061.0);
        //System.out.println(result);

    }
*/

    @Test
    public void getWindowEnergyPredictionTest(){
        String header = "timestamp,value";
        writeStringToFile(predictionFile, header, false);
        writeToFile(predictionFile, 1, 5, false);
        writeToFile(predictionFile, 2, 10, true);
        writeToFile(predictionFile, 3, 15, true);

        EnergyManager energyManager = new EnergyManager(predictionFile);
        double result = energyManager.getWindowPredictionEnergy(2, 1);
        assertTrue(result == 25);
        deleteFile(predictionFile);
    }


}
