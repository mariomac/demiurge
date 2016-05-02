package  predictors;

import es.bsc.demiurge.core.predictors.EnergyPredictionManager;
import es.bsc.demiurge.core.predictors.EnergyTypes;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class EnergyPredictionManagerTest {


    String rFile = "/home/mcanuto/ProjectsEU/demiurge/core/src/main/resources/energyPredictor.R";
    String energyFile = "/home/mcanuto/BSC/Projects/RenewIT/RES_2015.csv";
    int numForecast = 10;
    int maxInputSamples = 100;
    String outputFile = "/home/mcanuto/ProjectsEU/demiurge/core/src/main/resources/predictionOutTest.csv";

    String type = EnergyTypes.GREEN;
    EnergyPredictionManager energyPredictionManager = new EnergyPredictionManager(0,rFile, energyFile, type, numForecast, maxInputSamples, outputFile);


    @Test
    public void predictValuesTest(){

        this.energyPredictionManager.predictValues(150);
        File file = new File(outputFile);
        assertTrue(file.exists());

        file.delete();

/*
        BufferedReader br = null;
        String out = "";
        try {
            br = new BufferedReader(new FileReader(outputFile));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            out = sb.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }  finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        System.out.println(out);
*/



    }


}
