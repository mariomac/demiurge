/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.bsc.demiurge.core.clopla.placement;

import es.bsc.demiurge.core.clopla.domain.ClusterState;
import es.bsc.demiurge.core.clopla.domain.ConstructionHeuristic;
import es.bsc.demiurge.core.clopla.domain.Host;
import es.bsc.demiurge.core.clopla.domain.Vm;
import es.bsc.demiurge.core.clopla.placement.VmPlacementProblem;
import es.bsc.demiurge.core.clopla.placement.config.VmPlacementConfig;
import es.bsc.demiurge.core.clopla.placement.config.localsearch.LateAcceptance;
import es.bsc.demiurge.core.clopla.placement.solver.VmPlacementSolverFactory;
import es.bsc.demiurge.core.configuration.Config;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.optaplanner.core.impl.score.director.simple.SimpleScoreCalculator;

/**
 *
 * @author raimon
 */
public class VmPlacementProblemTest extends TestCase {
    Map<String,Class<? extends SimpleScoreCalculator>> placementPolicies = null;
    
    @Override
    public void setUp() throws Exception {
        placementPolicies = new HashMap<>();
        placementPolicies.put("consolidation", es.bsc.demiurge.core.clopla.placement.scorecalculators.ScoreCalculatorConsolidation.class);
        placementPolicies.put("distribution", es.bsc.demiurge.core.clopla.placement.scorecalculators.ScoreCalculatorDistribution.class);
        /*
        <util:map id="placementPolicies" key-type="java.lang.String" value-type="java.lang.Class">
            <entry key="consolidation" value-type="java.lang.Class" value="es.bsc.demiurge.core.clopla.placement.scorecalculators.ScoreCalculatorConsolidation"/>
            <entry key="distribution" value-type="java.lang.Class" value="es.bsc.demiurge.core.clopla.placement.scorecalculators.ScoreCalculatorDistribution"/>
            <entry key="groupByApp" value-type="java.lang.Class" value="es.bsc.demiurge.core.clopla.placement.scorecalculators.ScoreCalculatorGroupByApp"/>
            <entry key="random" value-type="java.lang.Class" value="es.bsc.demiurge.core.clopla.placement.scorecalculators.ScoreCalculatorRandom"/>
            <entry key="energyAware" value-type="java.lang.Class" value="es.bsc.vmm.ascetic.scheduler.clopla.ScoreCalculatorAsceticEnergyAware"/>
            <entry key="costAware" value-type="java.lang.Class" value="es.bsc.vmm.ascetic.scheduler.clopla.ScoreCalculatorAsceticEnergyAware"/>
        </util:map>
        */
        super.setUp();
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testConsolidationScenario() {
        List<Host> hosts = new ArrayList<>();
        List<Vm> vms = new ArrayList<>();
        
        hosts.add( new Host(1L, "compute1", 4, 8, 200, false) );
        hosts.add( new Host(2L, "compute2", 4, 8, 200, false) );
        hosts.add( new Host(3L, "compute3", 4, 8, 200, false) );
        
        vms.add( new Vm.Builder(1L, 1, 2, 50).build() );
        vms.add( new Vm.Builder(2L, 1, 2, 50).build() );
        vms.add( new Vm.Builder(3L, 1, 2, 50).build() );
        
        VmPlacementConfig config =
            new VmPlacementConfig.Builder(
                "consolidation",
                5,
                ConstructionHeuristic.FIRST_FIT_DECREASING,
                new LateAcceptance(400),
                false
            ).build();
        
        Config.INSTANCE.setPlacementPolicies(placementPolicies);
        VmPlacementProblem problem = new VmPlacementProblem(hosts, vms, config);
        ClusterState solution = problem.getBestSolution();
        
        for(Vm vm : solution.getVms()){
            assertEquals("compute1", vm.getHost().getHostname());
        }
    }
    
    public void testDistributionScenario() {
        List<Host> hosts = new ArrayList<>();
        List<Vm> vms = new ArrayList<>();
        
        hosts.add( new Host(1L, "compute1", 4, 8, 200, false) );
        hosts.add( new Host(2L, "compute2", 4, 8, 200, false) );
        hosts.add( new Host(3L, "compute3", 4, 8, 200, false) );
        
        vms.add( new Vm.Builder(1L, 1, 2, 50).build() );
        vms.add( new Vm.Builder(2L, 1, 2, 50).build() );
        vms.add( new Vm.Builder(3L, 1, 2, 50).build() );
        
        VmPlacementConfig config =
            new VmPlacementConfig.Builder(
                "distribution",
                5,
                ConstructionHeuristic.FIRST_FIT_DECREASING,
                new LateAcceptance(400),
                false
            ).build();
        
        Config.INSTANCE.setPlacementPolicies(placementPolicies);
        VmPlacementProblem problem = new VmPlacementProblem(hosts, vms, config);
        ClusterState solution = problem.getBestSolution();
        
        assertEquals("compute1", solution.getVms().get(0).getHost().getHostname());
        assertEquals("compute2", solution.getVms().get(1).getHost().getHostname());
        assertEquals("compute3", solution.getVms().get(2).getHost().getHostname());
    }
    
    public void testHwPlatformMigration() {
        List<Host> hosts = new ArrayList<>();
        List<Vm> vms = new ArrayList<>();
        
        Host host1 = new Host(1L, "compute1", 4, 8, 200, "x86_64", "Intel", null, "SSD", false);
        hosts.add( host1 );
        Host host2 = new Host(2L, "compute2", 4, 8, 200, "x86_64", "Intel", null, "RAID", false);
        hosts.add( host2 );
        
        Vm vm1 = new Vm.Builder(1L, 1, 2, 50, "x86_64", "Intel", null, "RAID").build();
        vm1.setHost(host1);
        vms.add( vm1 );
        
        VmPlacementConfig config =
            new VmPlacementConfig.Builder(
                "consolidation",
                5,
                ConstructionHeuristic.FIRST_FIT_DECREASING,
                new LateAcceptance(400),
                false
            ).build();
        
        Config.INSTANCE.setPlacementPolicies(placementPolicies);
        VmPlacementProblem problem = new VmPlacementProblem(hosts, vms, config);
        ClusterState solution = problem.getBestSolution();
        
        assertEquals("compute2", solution.getVms().get(0).getHost().getHostname());
    }
}
