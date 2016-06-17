package predictors;

import es.bsc.demiurge.core.db.VmManagerDb;
import es.bsc.demiurge.core.db.VmManagerDbFactory;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.predictors.ArrivalsWorkloadPredictionManager;
import es.bsc.demiurge.core.predictors.TimeSeriesArrivals;
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
    private static String outputFileStr = "/tmp/workloadPredictionOutTest.csv";
    private static String workloadProfileFile = "/tmp/workloadTest.csv"; // it will be created
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
    public static void initialize(){

        cleanDb();
        arrivalsWorkloadPredictionManager = new ArrivalsWorkloadPredictionManager(30, db, rFile, workloadProfileFile, numForecast, maxInputSamples, outputFileStr);
        populateDb();
    }

    public static void populateDb(){
        System.out.println("populating Db");
        db.insertVm("0","","","",benchmark,1000.0, 10.0,5, "0");
        db.insertVmIntoArrivals("0","","","",benchmark,1000.0, 10.0,5);

        db.insertVm("1","","","",benchmark,1000.0, 20.0,10,  "1");
        db.insertVmIntoArrivals("1","","","",benchmark,1000.0, 20.0,10);

        db.insertVm("2","","","",benchmark,1000.0, 30.0,15,  "2");
        db.insertVmIntoArrivals("2","","","",benchmark,1000.0, 30.0,15);

        db.insertVm("3","","","",benchmark,1000.0, 40.0,20,  "3");
        db.insertVmIntoArrivals("3","","","",benchmark,1000.0, 40.0,20);

        db.insertVm("4","","","",benchmark,1000.0, 50.0,25,  "4");
        db.insertVmIntoArrivals("4","","","",benchmark,1000.0, 50.0,25);
    }

    public static void cleanDb(){
        System.out.println("cleaningDB");
        db.deleteVm("0");
        db.deleteVm("1");
        db.deleteVm("2");
        db.deleteVm("3");
        db.deleteVm("4");
    }

    @Test
    public void getDbEstimatedPowerForBenchmarkTest(){
        // check if results are returned in correct order (time)
        List<Double> res =  db.getPastPowerForBenchmark(benchmark, maxInputSamples);
        System.out.println(res.size());
        assertTrue(res.get(0).equals(10.0));
        assertTrue(res.get(4).equals(50.0));
    }

    @Test
    public void getEstimatedPowerForBenchmarkWriteFile(){
        long now = 30;
        double val = arrivalsWorkloadPredictionManager.getEstimatedPowerForBenchmark(benchmark, 30);
        assertEquals(val, 60, 0);
    }

    @Test
    public void getEstimatedPowerIntervalWithAtLeastOneValue(){

        int limit = 20;
        TimeSeriesArrivals res = db.getPastPower(limit);
        //res.printTimeSeriesArrivals();
        //System.out.println(res.getIntervalWithAtLeastOneValue());
        assertTrue(res.getIntervalWithAtLeastOneValue().equals(5));

        List<Double> vals = res.getSumValuesSplittedInterval(res.getIntervalWithAtLeastOneValue());
        assertTrue(vals.get(0).equals(0.0));
        assertTrue(vals.get(5).equals(50.0));

    }

    @Test
    public void getEstimatedPowerArrivals(){

        Vm vm = new Vm("A","b",2,3,4,5,"","");
        vm.setPowerEstimated(60);

        List<Double> a = arrivalsWorkloadPredictionManager.getEstimatedPowerArrivals(25);
        assertTrue(a.get(0) == 60);
        assertTrue(a.get(a.size()-1) == 150.0);
    }

}
