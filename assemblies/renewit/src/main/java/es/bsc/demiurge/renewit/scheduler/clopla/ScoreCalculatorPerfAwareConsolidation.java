package es.bsc.demiurge.renewit.scheduler.clopla;

import es.bsc.demiurge.cloudsuiteperformancedriver.models.VmSize;
import es.bsc.demiurge.core.clopla.domain.ClusterState;
import es.bsc.demiurge.core.clopla.domain.Host;
import es.bsc.demiurge.core.clopla.domain.Vm;
import es.bsc.demiurge.core.clopla.placement.config.VmPlacementConfig;
import es.bsc.demiurge.core.clopla.placement.scorecalculators.ScoreCalculatorCommon;
import es.bsc.demiurge.core.configuration.Config;
import es.bsc.demiurge.renewit.manager.PerformanceVmManager;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.director.simple.SimpleScoreCalculator;

import java.util.List;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class ScoreCalculatorPerfAwareConsolidation implements SimpleScoreCalculator<ClusterState> {

    private Logger logger = LogManager.getLogger(ScoreCalculatorPerfAwareConsolidation.class);
    private PerformanceVmManager performanceVmManager;

    public ScoreCalculatorPerfAwareConsolidation() {

        performanceVmManager = (PerformanceVmManager) Config.INSTANCE.getVmManager();
    }
    @Override
    public Score calculateScore(ClusterState solution) {

        //  Calculate cpus, mem, disk for performance required. it depends on the host where the vm is deployed
        for(Host h : solution.getHosts()) {
            //logger.info("Host: " + h.getHostname());

            List<Vm> vms_in_host = solution.getVmsDeployedInHost(h);

            for (Vm vm : vms_in_host){

                if (!vm.isDeployed()){
                    // Calculate cpus, mem, disk for performance required
                    VmSize vmSize = performanceVmManager.getVmSizesClopla(vm, h);
                    vm.setNcpus(vmSize.getCpus());
                    vm.setRamMb(vmSize.getRamGb()*1024);
                    vm.setDiskGb(vmSize.getDiskGb());

                    //logger.info(vm.getExtraParameters().getBenchmark()+" with perf. " + vm.getExtraParameters().getPerformance()+ " - " +h.getHostname() + ": "+ vmSize.getCpus() + " CPUs, " + vmSize.getRamGb() + " GB RAM, " + vmSize.getDiskGb() +" GB Disk" );
                }
            }

        }

        int softScore = 10 * (solution.countOffHosts() + solution.countIdleHosts())
                - (VmPlacementConfig.initialClusterState.get().countVmMigrationsNeeded(solution));

        int hardScore = calculateHardScore(solution);
        //System.out.println("---- " + HardSoftScore.valueOf(hardScore,softScore) + " ----");
        return HardSoftScore.valueOf(hardScore,softScore);

    }


    private int calculateHardScore(ClusterState solution) {
        if((ScoreCalculatorCommon.getClusterOverCapacityScore(solution)
                + ScoreCalculatorCommon.getClusterPenaltyScoreForFixedVms(solution)) != 0){
            return -1;
        }else{
            return 0;
        }
    }
}
