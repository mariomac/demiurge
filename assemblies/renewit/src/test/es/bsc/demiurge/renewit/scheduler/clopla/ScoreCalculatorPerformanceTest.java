package es.bsc.demiurge.renewit.scheduler.clopla;

import es.bsc.demiurge.core.clopla.domain.ClusterState;
import es.bsc.demiurge.core.clopla.domain.Host;
import es.bsc.demiurge.core.clopla.domain.Vm;
import es.bsc.demiurge.core.models.vms.ExtraParameters;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class ScoreCalculatorPerformanceTest {

    ClusterState clusterState;
    @Before
    public void setUpFkeCluster(){

        clusterState = new ClusterState();

        Host bscgrid30_fake = new Host(1l,  "bscgrid30", 32, 24 * 1024, 1500, false);
        Host bscgrid31_fake = new Host(2l, "bscgrid31", 24, 16 * 1024, 1500, false);

        ArrayList<Host> hostList = new ArrayList<Host>();
        hostList.add(bscgrid30_fake);
        hostList.add(bscgrid31_fake);

        clusterState.setHosts(hostList);

        ExtraParameters extraParams = new ExtraParameters("data_serving", 18000);

        Vm vm1 = new Vm.Builder(1l, 0, 0,0).isDeployed(false).extraParameters(extraParams).build();
        Vm vm2 = new Vm.Builder(1l, 0, 0,0).isDeployed(false).extraParameters(extraParams).build();
        ArrayList<Vm> vmList = new ArrayList<Vm>();
        vmList.add(vm1);
        vmList.add(vm2);
        clusterState.setVms(vmList);

    }


    @Test
    public void testAllocationWithPerformaceModels(){

        ScoreCalculatorPerformance calculateScore = new ScoreCalculatorPerformance();
        calculateScore.calculateScore(this.clusterState);




    }





}
