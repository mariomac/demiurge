package predictors;

import es.bsc.demiurge.core.db.VmManagerDb;
import es.bsc.demiurge.core.db.VmManagerDbFactory;
import es.bsc.demiurge.core.predictors.ArrivalsWorkloadPredictionManager;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class ArrivalsWorkloadPredictionManagerTest {
    private static String rFile = "/home/mcanuto/ProjectsEU/demiurge/core/src/main/resources/workloadPredictor.R";
    private static String inputFile = "/home/mcanuto/BSC/Projects/RenewIT/RES_2015.csv";
    private static int numForecast = 10;
    private static int maxInputSamples = 100;
    private static String outputFileStr = "/home/mcanuto/ProjectsEU/demiurge/core/src/main/resources/workloadPredictionOutTest.csv";
    private static String workloadProfileFile = "/home/mcanuto/ProjectsEU/demiurge/core/src/main/resources/workload.csv"; // it will be created
    private static VmManagerDb db = VmManagerDbFactory.getDb("FakeDB");
    private static String benchmark = "software_testing";
    private static ArrivalsWorkloadPredictionManager arrivalsWorkloadPredictionManager;
    private static File fileInput = new File(workloadProfileFile);
    private static File outputFile = new File(outputFileStr);

    private static String fileTempInputStr = "/tmp/predictBenchmarkInput.csv";
    private static File fileTempInput = new File(fileTempInputStr);

    private static String fileTempOutputStr = "/tmp/predictBenchmarkOutput.csv";
    private static File fileTempOutput = new File(fileTempOutputStr);

    @BeforeClass
    public static void populateDb(){

        System.out.println("cleaningDB");
        db.deleteVm("0");
        db.deleteVm("1");
        db.deleteVm("2");
        db.deleteVm("3");
        db.deleteVm("4");

        System.out.println("populating Db");
        arrivalsWorkloadPredictionManager = new ArrivalsWorkloadPredictionManager(30, db, rFile, workloadProfileFile, numForecast, maxInputSamples, outputFileStr);

        db.insertVm("0","","","",benchmark,1000.0, 50.0,10);
        db.insertVm("1","","","",benchmark,1000.0, 80.0,20);
        db.insertVm("2","","","",benchmark,1000.0, 90.0,5);
        db.insertVm("3","","","",benchmark,1000.0, 10.0,15);
        db.insertVm("4","","","",benchmark,1000.0, 70.0,25);
    }


    @Test
    public void getDbEstimatedPowerForBenchmarkTest(){
        // check if results are returned in correct order (time)
        List<Double> res =  db.getPastPowerForBenchmark(benchmark, maxInputSamples);
        assertTrue(res.get(0).equals(90.0));
        assertTrue(res.get(1).equals(50.0));
        assertTrue(res.get(2).equals(10.0));
        assertTrue(res.get(3).equals(80.0));
        assertTrue(res.get(4).equals(70.0));
    }

    @Test
    public void getEstimatedPowerForBenchmarkWriteFile(){
        long now = 30;
        double val = arrivalsWorkloadPredictionManager.getEstimatedPowerForBenchmark(benchmark, 30);
        assertEquals(val, 59.9999999992463, 0);
    }



}
