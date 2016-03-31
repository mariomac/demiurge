package es.bsc.demiurge.renewit.scheduler.clopla;

import es.bsc.demiurge.core.clopla.domain.ClusterState;
import es.bsc.demiurge.core.clopla.domain.ConstructionHeuristic;
import es.bsc.demiurge.core.clopla.domain.Host;
import es.bsc.demiurge.core.clopla.domain.Vm;
import es.bsc.demiurge.core.clopla.lib.Clopla;
import es.bsc.demiurge.core.clopla.lib.IClopla;
import es.bsc.demiurge.core.clopla.placement.config.VmPlacementConfig;
import es.bsc.demiurge.core.clopla.placement.config.localsearch.LateAcceptance;
import es.bsc.demiurge.core.models.vms.ExtraParameters;
import es.bsc.demiurge.renewit.manager.PerformanceVmManager;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class ScoreCalculatorPerfAwarePowerAwareTest {

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

        ExtraParameters extraParams = new ExtraParameters("data_serving", 10000);

        Vm vm1 = new Vm.Builder(1l, 10, 20,0).isDeployed(false).extraParameters(extraParams).build();
        Vm vm2 = new Vm.Builder(2l, 20, 30,30).isDeployed(false).extraParameters(extraParams).build();
        ArrayList<Vm> vmList = new ArrayList<Vm>();
        vmList.add(vm1);
        vmList.add(vm2);
        clusterState.setVms(vmList);


    }


    @Test
    public void testAllocationWithPerformaceModels(){

        ScoreCalculatorPerfAwarePowerAware calculateScore = new ScoreCalculatorPerfAwarePowerAware();
        calculateScore.calculateScore(this.clusterState);

    }

   // @Test
    public void test2AllocationWithPerformaceModels(){


        clusterState = new ClusterState();

        Host bscgrid30_fake = new Host(1l,  "bscgrid30", 32, 24 * 1024, 1500, false);
        Host bscgrid31_fake = new Host(2l, "bscgrid31", 24, 16 * 1024, 1500, false);

        ArrayList<Host> hostList = new ArrayList<Host>();
        hostList.add(bscgrid30_fake);
        hostList.add(bscgrid31_fake);

        clusterState.setHosts(hostList);

        ExtraParameters extraParams = new ExtraParameters("data_serving", 10000);

        Vm vm1 = new Vm.Builder(1l, 2, 1,1).isDeployed(false).extraParameters(extraParams).build();
        Vm vm2 = new Vm.Builder(2l, 0, 0,0).isDeployed(false).extraParameters(extraParams).build();
        Vm vm3 = new Vm.Builder(3l, 5, 10,50).isDeployed(true).extraParameters(extraParams).powerConsumption(130.0).build();
        vm3.setHost(bscgrid31_fake);
        ArrayList<Vm> vmList = new ArrayList<Vm>();
        vmList.add(vm1);
        //vmList.add(vm2);
        vmList.add(vm3);
        clusterState.setVms(vmList);


        ScoreCalculatorPerfAwarePowerAware calculateScore = new ScoreCalculatorPerfAwarePowerAware();
        calculateScore.calculateScore(this.clusterState);

        IClopla clopla = new Clopla();
        VmPlacementConfig vmPlacementConfig =
                new VmPlacementConfig.Builder(
                        "consolidation",
                        30,
                        ConstructionHeuristic.FIRST_FIT_DECREASING,
                        new LateAcceptance(400),
                        false).build();
        System.out.println(clopla.getBestSolution(hostList, vmList, vmPlacementConfig));

    }

    //@Test
    public void test3AllocationWithPerformaceModels() {


        clusterState = new ClusterState();

        Host bscgrid30_fake = new Host(1l,  "bscgrid30", 32, 24 * 1024, 1500, false);
        Host bscgrid31_fake = new Host(2l, "bscgrid31", 24, 16 * 1024, 1500, false);

        ArrayList<Host> hostList = new ArrayList<Host>();
        hostList.add(bscgrid30_fake);
        hostList.add(bscgrid31_fake);

        clusterState.setHosts(hostList);

        ExtraParameters extraParams = new ExtraParameters("data_serving", 10000);

        Vm vm1 = new Vm.Builder(1l, 0, 0,0).isDeployed(false).extraParameters(extraParams).build();
        Vm vm2 = new Vm.Builder(2l, 0, 0,0).isDeployed(false).extraParameters(extraParams).build();
        Vm vm3 = new Vm.Builder(3l, 5, 10,50).isDeployed(true).extraParameters(extraParams).powerConsumption(130.0).build();
        vm3.setHost(bscgrid31_fake);
        ArrayList<Vm> vmList = new ArrayList<Vm>();
        vmList.add(vm1);
        vmList.add(vm2);
        vmList.add(vm3);
        clusterState.setVms(vmList);


        PerformanceVmManager performanceVmManager = new PerformanceVmManager();
        //List<String> a = performanceVmManager.deployVms(vmList);
    }


}
