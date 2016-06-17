package es.bsc.demiurge.renewit.manager;

import es.bsc.autonomicbenchmarks.controllers.BenchmarkController;
import es.bsc.autonomicbenchmarks.controllers.QueueBenchmarkManager;
import es.bsc.demiurge.cloudsuiteperformancedriver.core.PerformanceDriverCore;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.CloudSuiteBenchmark;
import es.bsc.demiurge.cloudsuiteperformancedriver.models.VmSize;
import es.bsc.demiurge.core.VmmGlobalListener;
import es.bsc.demiurge.core.cloudmiddleware.CloudMiddlewareException;
import es.bsc.demiurge.core.configuration.Config;
import es.bsc.demiurge.core.manager.GenericVmManager;
import es.bsc.demiurge.core.manager.components.*;
import es.bsc.demiurge.core.models.scheduling.RecommendedPlan;
import es.bsc.demiurge.core.models.scheduling.RecommendedPlanRequest;
import es.bsc.demiurge.core.models.scheduling.VmPlacement;
import es.bsc.demiurge.core.models.vms.ListVmsDeployed;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.models.vms.VmDeployed;
import es.bsc.demiurge.core.monitoring.hosts.Host;
import es.bsc.demiurge.core.monitoring.hosts.HostFactory;
import es.bsc.demiurge.core.predictors.*;
import es.bsc.demiurge.core.selfadaptation.SelfAdaptationManager;
import es.bsc.demiurge.core.utils.FileSystem;
import es.bsc.demiurge.renewit.utils.CloudsuiteUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.Executors;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class PerformanceVmManager extends GenericVmManager {
    private Logger logger = LogManager.getLogger(PerformanceVmManager.class);
    private PerformanceDriverCore performanceDriverCore = new PerformanceDriverCore();

    public PerformanceVmManager() {
        super();

        // For Vms that have to be deployed after a time
        scheduledExecutorService = Executors.newScheduledThreadPool(30);//Runtime.getRuntime().availableProcessors()/2);
        scheduledDestroyService = Executors.newScheduledThreadPool(30);//Runtime.getRuntime().availableProcessors()/2);

    }

    @Override
    public void doInitActions() {
        this.cloudMiddleware = conf.getCloudMiddleware();

        selfAdaptationManager = new SelfAdaptationManager(this, GenericVmManager.conf.dbName);

        // Initialize all the VMM components
        imageManager = new ImageManager(cloudMiddleware);

        // Instantiates the hosts according to the monitoring software selected.
        HostFactory hf = Config.INSTANCE.getHostFactory();

        List<Host> hosts = new ArrayList<>();

        for(String hostname : Config.INSTANCE.hosts) {
            hosts.add(hf.getHost(hostname));
        }

        hostsManager = new HostsManager(hosts);

        // initializes other subcomponents
        estimatesManager = new EstimatesManager(this, conf.getEstimators());

        // Renewit: Clousdsuite BENCHMARK MANAGER
        queueBenchmarkManager = new QueueBenchmarkManager();
        benchmarkController = new BenchmarkController(queueBenchmarkManager);


        //Start new thread that takes care of the cloudsuite benchmark run inside VMs
        startBenchmarkControllerThread();

        String energyOutputFile = Config.INSTANCE.DEFAULT_ENERGY_PREDICTION_FILE; // it will be created
        // if predictions are enabled
        if (Config.INSTANCE.enablePredictions) {


            String workloadFile = "/tmp/workload2.csv"; // it will be created
            String workloadOutputFile = "/tmp/predictionWorkloadOut2.csv";  // it will be created

            // Clean db and old files
            db.deleteAllPerformanceOfVM();
            db.deleteAllVms();
            FileSystem.deleteFile(workloadFile);
            FileSystem.deleteFile(workloadOutputFile);
            FileSystem.deleteFile(energyOutputFile);


            /********** Green Energy predictions **********/
            String rFile = Config.INSTANCE.ENERGY_PREDICTOR_R_FILE;

            String rFilePath = null;
            int numForecast = 500;
            int maxInputSamples = 1000;

            //URL url = getClass().getResource(rFile);
            //rFilePath = url.getPath();
            rFilePath = FileSystem.getFilePath(rFile);

            if (rFilePath != null) {
                //type = green, total, RES
                energyPredictionManager = new EnergyPredictionManager(DEMIURGE_START_TIME, rFilePath, Config.INSTANCE.energyProfilesFile, "green", numForecast, maxInputSamples, energyOutputFile);
                startEnergyPredictionManagerThread();
            }else{
                logger.error("ENERGY PREDICTION NOT POSSIBLE: File '"+ rFile + "'does not exists");
            }

            /********** Arrivals Workload predictions **********/
            rFile = Config.INSTANCE.DEFAULT_WORKLOAD_PREDICTOR_R_FILE;
            rFilePath = FileSystem.getFilePath(rFile);


            arrivalsWorkloadPredictionManager = new ArrivalsWorkloadPredictionManager(DEMIURGE_START_TIME, super.getDB(), rFilePath, workloadFile, numForecast, maxInputSamples, workloadOutputFile);

            //Start new threads that takes care of the predictions
            startWorkloadPredicionManagerThread();

            /********** Post-poned VM deployments predictions **********/
            postponedDeployment =  Collections.synchronizedSortedMap(new TreeMap<Long, ArrayList<Vm>>());


            EnergyManager energyManager = new EnergyManager(energyOutputFile);
            ArrivalsWorkloadManager arrivalsWorkloadManager = new ArrivalsWorkloadManager(workloadOutputFile);
            vmsManager = new VmsManagerGreen(hostsManager, cloudMiddleware, db, selfAdaptationManager, estimatesManager, conf.getVmmListeners(), queueBenchmarkManager, scheduledExecutorService, scheduledDestroyService, energyManager, arrivalsWorkloadManager, postponedDeployment);

        }else {

            EnergyManager energyManager = new EnergyManager(energyOutputFile);

            vmsManager = new VmsManagerGreen(hostsManager, cloudMiddleware, db, selfAdaptationManager, estimatesManager, conf.getVmmListeners(), queueBenchmarkManager, scheduledExecutorService, scheduledDestroyService, energyManager);
        }
        selfAdaptationOptsManager = new SelfAdaptationOptsManager(selfAdaptationManager);
        vmPlacementManager = new VmPlacementManager(vmsManager, hostsManager,estimatesManager);

        // Start periodic self-adaptation thread if it is not already running.
        // This check would not be needed if only one instance of this class was created.
        if (!periodicSelfAdaptationThreadRunning) {
            periodicSelfAdaptationThreadRunning = true;
            startPeriodicSelfAdaptationThread();
        }

        for(VmmGlobalListener l : conf.getVmmGlobalListeners()) {
            l.onVmmStart();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                logger.debug("Notifying vmm global listeners on shutdown hook");
                for(VmmGlobalListener l : conf.getVmmGlobalListeners()) {
                    l.onVmmStop();
                }
            }
        }));
    }

    @Override
    public VmsManager getVmsManager() {
        return vmsManager;
    }
    private void startEnergyPredictionManagerThread() {
        logger.debug("RENEWIT: Starting energy prediction Thread");
        Thread thread = new Thread(energyPredictionManager);
        thread.start();
    }

    private void startWorkloadPredicionManagerThread() {
        logger.debug("RENEWIT: Starting workload prediction Thread");
        Thread thread = new Thread(arrivalsWorkloadPredictionManager);
        thread.start();
    }

    public PerformanceDriverCore getPerformanceDriverCore() {
        return performanceDriverCore;
    }



    /**
     * Deploys a list of VMs and returns its IDs.
     *
     * @param vms the VMs to deploy
     * @return the IDs of the VMs deployed in the same order that they were received
     */
    @Override
    public List<String> deployVms(List<Vm> vms) throws CloudMiddlewareException {
        // This is using VmsManagerGreen deployVms method!
        return vmsManager.deployVms(vms);
    }


    /**
     * This function calculates a deployment plan based on a request. It uses the VM placement library.
     *
     * @param recommendedPlanRequest the request
     * @param assignVmsToCurrentHosts indicates whether the hosts should be set in the VM instances
     * @param vmsToDeploy list of VMs that need to be deployed
     * @return the recommended plan
     */
    @Override
    public RecommendedPlan getRecommendedPlan(RecommendedPlanRequest recommendedPlanRequest,
                                              boolean assignVmsToCurrentHosts,
                                              List<Vm> vmsToDeploy) throws CloudMiddlewareException {

        if (Config.INSTANCE.getVmManager().getCurrentSchedulingAlgorithm().startsWith(Config.INSTANCE.PERF_POWER_ALGORITHM_PREFIX)) {

            RecommendedPlan recommendedPlan = super.vmPlacementManager.getRecommendedPlanDiscardHostNoPerformance(super.getDB().getCurrentSchedulingAlg(), recommendedPlanRequest, assignVmsToCurrentHosts, vmsToDeploy, performanceDriverCore);

            /*logger.info("Recommended plan: ");
            for (Map.Entry entry : recommendedPlan.getPlan().entrySet()) {
                logger.info(entry.getKey() + " -> " + entry.getValue());
            }*/
            // Convert from clopla VM to VMM VM
            // When using performance models, CPU, Ram and disk are not set. They must be set depending on the host chosen by optaplanner.

            VmsManager vmsManager = super.getVmsManager();

            for (Vm vm : vmsToDeploy){
                VmPlacement vmPlacement = vmsManager.findVmPlacementByVmId(
                        recommendedPlan.getVMPlacements(), vm.getName());

                Host host = super.getHostsManager().getHost(vmPlacement.getHostname());
                VmSize vmSize = getVmSizesVMM(vm, host);

                vm.setCpus(vmSize.getCpus());
                vm.setRamMb(vmSize.getRamGb()*1024);
                vm.setDiskGb(vmSize.getDiskGb());

                vm.setPowerEstimated(getPowerEstimantion(vm.getExtraParameters().getBenchmark(), host, vmSize) - host.getIdlePower());
                logger.info(VmPlacementToString(vm, host));

            }

            return recommendedPlan;

        }else{
            return super.vmPlacementManager.getRecommendedPlan(super.getDB().getCurrentSchedulingAlg(),recommendedPlanRequest, assignVmsToCurrentHosts, vmsToDeploy);
        }

    }

    public double getPowerEstimantion(CloudSuiteBenchmark benchmark, Host h, VmSize vmSize){

        return performanceDriverCore.getModeller().getBenchmarkAvgPower(benchmark, h.getType(), vmSize);

    }

    public VmSize getVmSizesVMM(Vm vm, Host h){
        return performanceDriverCore.getModeller().getMinVmSizesWithAtLeastPerformance(vm.getExtraParameters().getPerformance(), vm.getExtraParameters().getBenchmark(), CloudsuiteUtils.convertVMMHostToPerformanceHost(h));

    }
    public VmSize getVmSizesClopla(es.bsc.demiurge.core.clopla.domain.Vm vm, es.bsc.demiurge.core.clopla.domain.Host h){
        return performanceDriverCore.getModeller().getMinVmSizesWithAtLeastPerformance(vm.getExtraParameters().getPerformance(), vm.getExtraParameters().getBenchmark(), CloudsuiteUtils.convertClusterHostToPerformanceHost(h));

    }

    @Override
    public double getClusterConsumption() {
        List<Host> hosts = this.getHosts();
        ListVmsDeployed vms = new ListVmsDeployed(this.getAllVms());
        double pow = 0;

        HashMap <String, Integer> hmap = new HashMap<>();

        for (Host h : hosts){
            hmap.put(h.getHostname(), 0);
            /*if (h.isOn()) {
                pow += performanceDriverCore.getModeller().getIdlePowerHost(h.getType());
            }*/
        }
        for (VmDeployed vm : vms.getVms()){

            VmSize vmSize = new VmSize(vm.getCpus(), vm.getRamMb()*1024, vm.getDiskGb());
            String hostName = vm.getHostName();
            Host host = getHost(hostName);
            CloudSuiteBenchmark benchmark = vm.getExtraParameters().getBenchmark();

            double vmPowerEstimation = performanceDriverCore.getModeller().getBenchmarkAvgPower(benchmark, host.getType(), vmSize);

            if (vmPowerEstimation > host.getMaxPower()){
                vmPowerEstimation =  host.getMaxPower();
            }
            pow += vmPowerEstimation - host.getIdlePower();

            if (hmap.get(hostName) == 0){
                pow += host.getIdlePower() +  5;
                hmap.put(hostName, 1);
            }

        }

        return pow;

    }

    @Override
    public double predictClusterConsumption(List<Vm> vms)  {
        double bestDeploymentScore = 0;
        VmsManagerGreen vmm = (VmsManagerGreen) super.getVmsManager();
        double vmPrediction = 0;
        try {
            vmPrediction = vmm.predictClusterConsumption(vms);
        } catch (CloudMiddlewareException e) {
            return -Integer.MAX_VALUE;
        }

        Vm vm = vms.get(0);
        if (vmm.isVmShiftable(vm.getExtraParameters().getBenchmarkStr()) && (vm.getExtraParameters().getRunningTime() > vm.getExtraParameters().getPerformance())){

            int duration = (int)vm.getExtraParameters().getPerformance();

            long now = System.currentTimeMillis() / 1000 - DEMIURGE_START_TIME;
            long deadline = vm.getExtraParameters().getRunningTime() + now;
            bestDeploymentScore =vmm.findBestDeploymentScore(now, (int) deadline, duration, vmPrediction);
        }else{
             bestDeploymentScore = (long) vmPrediction;
        }

        return  bestDeploymentScore;
    }


    private String VmPlacementToString(Vm vm, Host host){
        return "Vm:{ " +
                "\n\tname: " + vm.getName() +
                "\n\timageId: " + vm.getImage() +
                "\n\tCPUs: " + vm.getCpus() +
                "\n\tRAM: " + vm.getRamMb() +
                "\n\tDisk: " + vm.getDiskGb() +
                "\n\tHost: " + host.getHostname() +
                "\n\tBenchmark: " + vm.getExtraParameters().getBenchmark() +
                "\n\tPerformance: " + vm.getExtraParameters().getPerformance() +
                "\n\tRunning Time: " + vm.getExtraParameters().getRunningTime() +
                "\n}";
    }

    @Override
    public EnergyFileModel getEnergyUsageAtTime() {

        long time = System.currentTimeMillis() / 1000 - DEMIURGE_START_TIME;
        VmsManagerGreen vmsManagerGreen = (VmsManagerGreen) this.vmsManager;
        EnergyManager energyManager = vmsManagerGreen.getEnergyManager();

        return energyManager.getEnergyUsageAtTime(time);
    }

}
