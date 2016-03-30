package es.bsc.demiurge.renewit.scheduler.clopla;

import es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.Modeller;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.CloudSuiteBenchmark;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.VmSize;
import es.bsc.demiurge.core.clopla.domain.ClusterState;
import es.bsc.demiurge.core.clopla.domain.Host;
import es.bsc.demiurge.core.clopla.domain.Vm;
import es.bsc.demiurge.core.clopla.placement.scorecalculators.ScoreCalculatorCommon;
import es.bsc.demiurge.core.configuration.Config;
import es.bsc.demiurge.renewit.manager.PerformanceVmManager;
import es.bsc.demiurge.renewit.modellers.PowerModeller;
import es.bsc.demiurge.renewit.utils.CloudsuiteUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.optaplanner.core.api.score.buildin.hardsoftdouble.HardSoftDoubleScore;
import org.optaplanner.core.impl.score.director.simple.SimpleScoreCalculator;

import java.util.List;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class ScoreCalculatorPerformance implements SimpleScoreCalculator<ClusterState> {
    private Logger logger = LogManager.getLogger(ScoreCalculatorPerformance.class);

    private PerformanceVmManager performanceVmManager;
    private PowerModeller powerModeller;
    private Modeller performanceModeller;

    public ScoreCalculatorPerformance() {
        // TODO: Uncomment - comment
        performanceVmManager = (PerformanceVmManager) Config.INSTANCE.getVmManager();
        powerModeller = new PowerModeller();

        performanceModeller = performanceVmManager.getPerformanceDriverCore().getModeller();
    }

    @Override
    public HardSoftDoubleScore calculateScore(ClusterState solution) {

        double hardScore = -1;
        double softScore = 0;

        for(Host h : solution.getHosts()) {
            //List<Vm> vms = solution.getVms();
            // Calculate cpus, mem, disk for performance required
            List<Vm> vms_in_host = solution.getVmsDeployedInHost(h);

            for (Vm vm : vms_in_host){

                if (!vm.isDeployed()){
                    VmSize vmSize = getVmSizes(vm, h);
                    vm.setNcpus(vmSize.getCpus());
                    vm.setRamMb(vmSize.getRamGb()*1024);
                    vm.setDiskGb(vmSize.getDiskGb());

                    CloudSuiteBenchmark benchmark = performanceModeller.getBenchmarkFromName(vm.getExtraParameters().getBenchmark());
                    double newVMPowerEstimation = performanceModeller.getBenchmarkAvgPower(benchmark, h.getHostname(), vmSize);
                    // Set power estimation
                    vm.setPowerEstimation(newVMPowerEstimation);


                    logger.info(vm.getExtraParameters().getBenchmark()+" with perf. " + vm.getExtraParameters().getPerformance()+ " - " +h.getHostname() + ": "+ vmSize.getCpus() + " CPUs, " + vmSize.getRamGb() + " GB RAM, " + vmSize.getDiskGb() +" GB Disk" + " -> Power: " + newVMPowerEstimation);
                }


            }

            softScore -= powerModeller.getCloplaHostPowerConsumption(h, vms_in_host);
        }

        //Hard score at the end because we have to set vms cpu, ram, disk in the loop before
        hardScore = calculateHardScore(solution);

        logger.info(HardSoftDoubleScore.valueOf(hardScore,softScore));
        return HardSoftDoubleScore.valueOf(hardScore,softScore);
    }

    private int calculateHardScore(ClusterState solution) {
        return (int) (ScoreCalculatorCommon.getClusterOverCapacityScore(solution)
                + ScoreCalculatorCommon.getClusterPenaltyScoreForFixedVms(solution));
    }


    private VmSize getVmSizes(Vm vm, Host h){
        return performanceModeller.getMinVmSizesWithAtLeastPerformance(vm.getExtraParameters().getPerformance(), performanceModeller.getBenchmarkFromName(vm.getExtraParameters().getBenchmark()), CloudsuiteUtils.convertClusterHostToPerformanceHost(h));

    }
}



/*    private VmSize getVmSizes(Vm vm, Host h){
        return performanceModeller.getMinVmSizesWithAtLeastPerformance(vm.getExtraParameters().getPerformance(), performanceModeller.getBenchmarkFromName(vm.getExtraParameters().getBenchmark()), CloudsuiteUtils.convertClusterHostToPerformanceHost(h));

    }*/





/*
    public HardSoftDoubleScore calculateScore(ClusterState solution) {
        //double hardScore = calculateHardScore(solution);
        int hardScore = 0;
        double softScore = 0;

        for(Host h : solution.getHosts()) {
            int cpuPowerUsage = 0;
            double memoryUsage = 0;
            double diskUsage = 0;
            double energyUsage = 0;
            boolean used = false;

            for (Vm vm : solution.getVms()){
                // Consider only vm to be deployed
                if (!vm.isDeployed()){

                    // Step 1: Check if host support the performance required: PF =< MaxPF-TH
                    es.bsc.demiurge.cloudsuiteperformancedriver.models.Host hostPerf = CloudsuiteUtils.convertClusterHostToPerformanceHost(h);
                    CloudSuiteBenchmark benchmark = performanceModeller.getBenchmarkFromName(vm.getExtraParameters().getBenchmark());

                    boolean hostOk = performanceModeller.hostSupportPerformance(hostPerf, benchmark, vm.getExtraParameters().getPerformance());

                    // Step 2: Obtain Vm sizes for required performance
                    if (hostOk){
                        VmSize vmSize = getVmSizes(vm, h);
                        //logger.info(vmSize.toString());
                        cpuPowerUsage += vmSize.getCpus();
                        memoryUsage += vmSize.getRamGb();
                        diskUsage += vmSize.getDiskGb();
                        double expectedAvgPower = performanceModeller.getBenchmarkAvgPower(benchmark, h.getHostname(), vmSize);

                        energyUsage +=  expectedAvgPower;
                        used = true;

                    }else{
                        //logger.info("Host \"" + hostPerf.getHostname() + "\" discarded: performance not guaranteed");
                        used = false;
                    }

                }else{
                    energyUsage +=  vm.getPowerConsumption();
                }
            }

            // Hard constraints
            int cpuPowerAvailable = h.getNcpus() - cpuPowerUsage;
            if (cpuPowerAvailable < 0) {
                hardScore += cpuPowerAvailable;
            }
            double memoryAvailable = h.getRamMb()*1024 - memoryUsage;
            if (memoryAvailable < 0) {
                hardScore += memoryAvailable;
            }
            double diskAvailable = h.getDiskGb() - diskUsage;
            if (diskAvailable < 0) {
                hardScore += diskAvailable;
            }

            // Soft constraints
            if (used) {
                softScore -= energyUsage;
                //logger.info(h.getHostname() + " - soft: "+ softScore + " hard: " + hardScore);
            }


        }

        HardSoftDoubleScore a = HardSoftDoubleScore.valueOf(hardScore, softScore);
        //System.out.println(a.toString());

        return HardSoftDoubleScore.valueOf(hardScore,softScore);
    }

*/
/*    private int calculateHardScore(ClusterState solution) {
        return (int) (ScoreCalculatorCommon.getClusterOverCapacityScore(solution)
                + ScoreCalculatorCommon.getClusterPenaltyScoreForFixedVms(solution));
    }*//*


    private VmSize getVmSizes(Vm vm, Host h){
        return performanceModeller.getMinVmSizesWithAtLeastPerformance(vm.getExtraParameters().getPerformance(), performanceModeller.getBenchmarkFromName(vm.getExtraParameters().getBenchmark()), CloudsuiteUtils.convertClusterHostToPerformanceHost(h));

    }

}*/
