package es.bsc.demiurge.core.manager.components;

import es.bsc.autonomicbenchmarks.benchmarks.GenericBenchmark;
import es.bsc.autonomicbenchmarks.controllers.QueueBenchmarkManager;
import es.bsc.autonomicbenchmarks.models.VmAutonomic;
import es.bsc.demiurge.cloudsuiteperformancedriver.core.PerformanceDriverCore;
import es.bsc.demiurge.core.cloudmiddleware.CloudMiddleware;
import es.bsc.demiurge.core.cloudmiddleware.CloudMiddlewareException;
import es.bsc.demiurge.core.configuration.Config;
import es.bsc.demiurge.core.db.VmManagerDb;
import es.bsc.demiurge.core.drivers.VmmListener;
import es.bsc.demiurge.core.models.scheduling.DeploymentPlan;
import es.bsc.demiurge.core.models.scheduling.VmAssignmentToHost;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.models.vms.VmDeployed;
import es.bsc.demiurge.core.monitoring.hosts.Host;
import es.bsc.demiurge.core.predictors.ArrivalsWorkloadManager;
import es.bsc.demiurge.core.predictors.EnergyManager;
import es.bsc.demiurge.core.selfadaptation.SelfAdaptationManager;
import es.bsc.demiurge.core.utils.TimeUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static es.bsc.autonomicbenchmarks.utils.Utils.getBenchmark;
import static es.bsc.demiurge.core.manager.GenericVmManager.DEMIURGE_START_TIME;
import static es.bsc.demiurge.core.utils.FileSystem.writeToFile;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class VmsManagerGreen extends VmsManager {
    private final Logger log = LogManager.getLogger(VmsManagerGreen.class);
    // Specific for RenewIT
    private QueueBenchmarkManager queueBenchmarkManager;
    private ScheduledExecutorService scheduledExecutorService;
    private ScheduledExecutorService scheduledDestroyService;
    protected SortedMap<Long, ArrayList<Vm>> postponedDeployment;
    private ArrayList shiftableBenchmarks;
    private EnergyManager energyManager;
    private ArrivalsWorkloadManager arrivalsWorkloadManager;


    public VmsManagerGreen(HostsManager hostsManager, CloudMiddleware cloudMiddleware, VmManagerDb db, SelfAdaptationManager selfAdaptationManager, EstimatesManager estimatorsManager, List<VmmListener> listeners, QueueBenchmarkManager queueBenchmarkManager, ScheduledExecutorService scheduledExecutorService, ScheduledExecutorService scheduledDestroyService, EnergyManager energyManager, ArrivalsWorkloadManager arrivalsWorkloadManager, SortedMap<Long, ArrayList<Vm>> postponedDeployment) {
        super(hostsManager, cloudMiddleware, db, selfAdaptationManager, estimatorsManager, listeners);

        this.queueBenchmarkManager = queueBenchmarkManager;
        this.scheduledExecutorService  = scheduledExecutorService;
        this.scheduledDestroyService = scheduledDestroyService;
        this.energyManager = energyManager;
        this.arrivalsWorkloadManager = arrivalsWorkloadManager;
        this.postponedDeployment = postponedDeployment;


        shiftableBenchmarks = new ArrayList();
        shiftableBenchmarks.add("data_analytics");
        shiftableBenchmarks.add("graph_analytics");


        setupMaxPowerDC();

    }

    public VmsManagerGreen(HostsManager hostsManager, CloudMiddleware cloudMiddleware, VmManagerDb db, SelfAdaptationManager selfAdaptationManager, EstimatesManager estimatorsManager, List<VmmListener> listeners, QueueBenchmarkManager queueBenchmarkManager, ScheduledExecutorService scheduledExecutorService, ScheduledExecutorService scheduledDestroyService, EnergyManager energyManager) {
        super(hostsManager, cloudMiddleware, db, selfAdaptationManager, estimatorsManager, listeners);
        this.queueBenchmarkManager = queueBenchmarkManager;
        this.scheduledExecutorService  = scheduledExecutorService;
        this.scheduledDestroyService = scheduledDestroyService;
        this.energyManager = energyManager;
        shiftableBenchmarks = new ArrayList();
        shiftableBenchmarks.add("data_analytics");
        shiftableBenchmarks.add("graph_analytics");

        setupMaxPowerDC();

    }


    private void setupMaxPowerDC() {
        MAX_DC_POWER_SUPPORTED = 0;
        for (Host h : super.hostsManager.getHosts()){
            PerformanceDriverCore performanceDriverCore = new PerformanceDriverCore();
            MAX_DC_POWER_SUPPORTED += performanceDriverCore.getModeller().getMaxPower(h.getType());
        }
    }

    /**
     * Deploys a list of VMs and returns its IDs.
     *
     * @param vms the VMs to deploy
     * @return the IDs of the VMs deployed in the same order that they were received
     */
    @Override
    public List<String> deployVms(List<Vm> vms) throws CloudMiddlewareException {

        // Get current time to know how much each VM has to wait until it is deployed.
        Calendar calendarDeployRequestReceived = Calendar.getInstance();

        // HashMap (VmDescription,ID after deployment). Used to return the IDs in the same order that they are received
        Map<Vm, String> ids = new HashMap<>();

        long t = System.currentTimeMillis()/1000 - DEMIURGE_START_TIME;

        DeploymentPlan deploymentPlan = chooseBestDeploymentPlan(vms);

        // set timeRequest for all new VMs
        for (Vm v : vms){
            // predict vm power for arrivals estimation
            //v.setPowerEstimated(arrivalsWorkloadPredictionManager.getEstimatedPowerForBenchmark(v.getExtraParameters().getBenchmarkStr(),t));
            String temIp = UUID.randomUUID().toString();

            if (v.getTempId() == null){
                v.setTempId(temIp);
            }

            if (v.getTimeRequest() == 0){
                v.setTimeRequest(t);
                if (v.getExtraParameters().getBenchmark() != null){
                    db.insertVmIntoArrivals(temIp, "","","",v.getExtraParameters().getBenchmarkStr(), v.getExtraParameters().getPerformance(),v.getPowerEstimated(),v.getTimeRequest());
                }

            }

            // Add to arrivals queue
            //arrivalsWorkloadPredictionManager.addBenchmarkToQueue(v);

            // remove for posponed deployment if it was there
            if (v.isWasPostponed() && v.getTimeForDeploy()!= 0){
                // We need to copy the object because it will be delete by reference
                removePostponedDeploymentFromMap(v.getTempId(), v.getTimeForDeploy());
                v.setWasPostponed(false);
            }
        }

        // Get cluster consumption afetr optaplanner decision. Discard deployment if power required > MAX POWER
        double optaPlannerPowerEstimation = deploymentPlan.getPredictedClusterConsumption();
        if (optaPlannerPowerEstimation > MAX_DC_POWER_SUPPORTED){
            String fname = "VMactions.csv";
            String s2 = "rejected, "  + vms.get(0).getName();
            writeToFile(fname, System.currentTimeMillis()/1000, s2);
            throw new CloudMiddlewareException("Deployment rejected - Power exceeds the maximum supported by the DC");
        }


        /**
        This works only for 1 VM at time (no multiple vms in the same json request)
        **/

        // Loop through the VM assignments to hosts defined in the best deployment plan
        for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {

            Vm vmToDeploy = vmAssignmentToHost.getVm();
            // Check if it's better to postpone deployment

            if (Config.INSTANCE.enablePredictions && shiftableBenchmarks.contains(vmToDeploy.getExtraParameters().getBenchmarkStr())){
                int duration = (int)vmToDeploy.getExtraParameters().getPerformance();
                int deadline = vmToDeploy.getExtraParameters().getRunningTime() + (int) vmToDeploy.getTimeRequest();

                if (deadline - duration > 0){

                    long now = System.currentTimeMillis()/1000 - DEMIURGE_START_TIME;

                    long deploymentTime = findBestDeploymentTime(now, deadline, duration, optaPlannerPowerEstimation);

                    if (deploymentTime > 0){
                        vmToDeploy.setWasPostponed(true);
                        vmToDeploy.setTimeForDeploy(deploymentTime);

                        postponeDeployment(vmToDeploy);

                        long depTimeInTimestamp = deploymentTime;

                        String fname = "VMactions.csv";
                        long tOut = vms.get(0).getTimeForDeploy() + now + DEMIURGE_START_TIME;
                        String s2 = "postponed, "  + vms.get(0).getName() + ", " + tOut + ", " + now+ ", " + deadline + ", " + duration + ", " + deploymentTime;
                        writeToFile(fname, System.currentTimeMillis()/1000, s2);
                        throw new CloudMiddlewareException("Vm deployment has been postponed in time " + depTimeInTimestamp + "\n");
                    }

                }

            }

            Host hostForDeployment = vmAssignmentToHost.getHost();

            // Note: this is only valid for the Ascetic project
            // If the monitoring system is Zabbix, we need to make sure that the script that sets up the Zabbix
            // agents is executed.
            String originalVmInitScript = vmToDeploy.getInitScript();
//			AFAIK this is not anymore needed for ascetic y2
//            setAsceticInitScript(vmToDeploy);

            log.debug("Deploying VM " + vmToDeploy.getName() + " in host " + hostForDeployment.getHostname());

            GenericBenchmark benchmarkToRun = null;

            // To run benchmark automatically we need to pass an init script to the vm
            if ((Config.INSTANCE.getVmManager().getCurrentSchedulingAlgorithm().startsWith(Config.INSTANCE.PERF_POWER_ALGORITHM_PREFIX)) && (Config.INSTANCE.runBenchmarkAutomatically)) {
                int runningTime = vmToDeploy.getExtraParameters().getRunningTime();
                if (runningTime == 0){
                    runningTime = MAX_RUNNING_TIME;
                }
                benchmarkToRun = getBenchmark(vmToDeploy.getExtraParameters().getBenchmarkStr(), vmToDeploy.getCpus(), vmToDeploy.getRamMb()/1024, vmToDeploy.getDiskGb(), runningTime);
                String initScript = benchmarkToRun.getInitScript();
                vmToDeploy.setInitScriptStr(initScript);
            }


            String vmId;
            if (Config.INSTANCE.deployVmWithVolume) {
                vmId = deployVmWithVolume(vmToDeploy, hostForDeployment, originalVmInitScript);
            }
            else {
                vmId = deployVm(vmToDeploy, hostForDeployment);
            }

            if (vmToDeploy.getExtraParameters() != null) {
                db.insertVm(vmId, vmToDeploy.getApplicationId(), vmToDeploy.getOvfId(), vmToDeploy.getSlaId(), vmToDeploy.getExtraParameters().getBenchmarkStr(), vmToDeploy.getExtraParameters().getPerformance(), vmToDeploy.getPowerEstimated(), vmToDeploy.getTimeRequest(), vmToDeploy.getTempId());
            }else{
                db.insertVm(vmId, vmToDeploy.getApplicationId(), vmToDeploy.getOvfId(), vmToDeploy.getSlaId());
            }
            ids.put(vmToDeploy, vmId);

             if (Config.INSTANCE.vmAutoDestroy){
                 int timeDestroy;
                 if (shiftableBenchmarks.contains(vmToDeploy.getExtraParameters().getBenchmarkStr())){
                     //timeDestroy = (int)vmToDeploy.getTimeRequest() + (int)vmToDeploy.getExtraParameters().getPerformance() - (int) vmToDeploy.getTimeForDeploy();
                     timeDestroy = (int)vmToDeploy.getExtraParameters().getPerformance();
                 }else{
                     //timeDestroy = (int)vmToDeploy.getTimeRequest() + vmToDeploy.getExtraParameters().getRunningTime() - (int) vmToDeploy.getTimeForDeploy();
                     timeDestroy = vmToDeploy.getExtraParameters().getRunningTime();
                 }

                 destroyVMAfterTime(vmId, timeDestroy);

             }



            log.debug("[VMM] The Deployment of the VM with ID=" + vmId + " took " + TimeUtils.getDifferenceInSeconds(calendarDeployRequestReceived, Calendar.getInstance()) + " seconds");

            VmDeployed vmDeployed = getVm(vmId);
            for(VmmListener vml : listeners) {
                vml.onVmDeployment(vmDeployed);
            }

            if (vmToDeploy.needsFloatingIp()) {
                cloudMiddleware.assignFloatingIp(vmId);
            }

            // Running benchmarks automatically within the VM
            if ((Config.INSTANCE.getVmManager().getCurrentSchedulingAlgorithm().startsWith(Config.INSTANCE.PERF_POWER_ALGORITHM_PREFIX)) && (Config.INSTANCE.runBenchmarkAutomatically && benchmarkToRun != null)){
                log.debug("Running benchmark within VM " + vmDeployed.getId());
                VmAutonomic vmAutonomic = new VmAutonomic(vmDeployed.getId(), vmDeployed.getName(), vmDeployed.getIpAddress(), vmDeployed.getCpus(), vmDeployed.getRamMb()*1024, vmDeployed.getDiskGb(), hostForDeployment.getHostname());
                benchmarkToRun.runBenchmark(vmAutonomic);

                // IF RUNNING TIME != 0
                queueBenchmarkManager.addBenchmarkToQueue(benchmarkToRun);

            }


        }

        super.performAfterVmsDeploymentSelfAdaptation();

        // Return the IDs of the VMs deployed in the same order that they were received
        List<String> idsDeployedVms = new ArrayList<>();
        for (Vm vm: vms) {
            idsDeployedVms.add(ids.get(vm));
        }

        return idsDeployedVms;
    }

    private void destroyVMAfterTime(String vmId, int runningTime) {

        final String id = vmId;
        final int t = runningTime;
        scheduledDestroyService.schedule(
                new Callable() {
                    public String call() throws Exception {
                         deleteVm(id);
                        return "";
                    }
                },
                t,
                TimeUnit.SECONDS);
        log.info("Destroy vm " + vmId + " scheduled at time " + t);

    }

    public long findBestDeploymentTime(long now, int deadline, int duration, double optaPlannerPowerEstimation) {
        double bestAllocationScore = 0d;
        long deploymentTime = 0;
        //double end = now + deadline - duration;
        double end =  deadline - duration;
        long delayForDeployment = 0;
        // for each window get the predictions and calculate score
        for (long i = now; i < end; i++){

            double futureGreenEnergy = energyManager.getWindowPredictionEnergy(i, duration);
            double futureArrivals = arrivalsWorkloadManager.getWindowPredictionWorkload(i, duration);
            double movedVmsConsumption = calculateMovedVMConsumption(i, duration);

            double currentScore = futureGreenEnergy - futureArrivals - movedVmsConsumption - optaPlannerPowerEstimation;
            if (currentScore > bestAllocationScore){
                bestAllocationScore = currentScore;
                //deploymentTime = i;
                deploymentTime = delayForDeployment;
            }
            delayForDeployment++;
        }

        log.info("\nBest allocation time based on Green Energy: \n" +
                "Deployment Time in : " + deploymentTime + "\n" +
                "Green Score: " + bestAllocationScore);
        return deploymentTime;
    }

    public double findBestDeploymentScore(long now, int deadline, int duration, double optaPlannerPowerEstimation) {
        double bestAllocationScore = 0d;
        long deploymentTime = now;
        //double end = now + deadline - duration;
        double end =  deadline - duration;
        long delayForDeployment = 0;
        // for each window get the predictions and calculate score
        /*
        for (long i = now; i < end; i++){

            double futureGreenEnergy = energyManager.getWindowPredictionEnergy(i, duration);
            double futureArrivals = arrivalsWorkloadManager.getWindowPredictionWorkload(i, duration);
            double movedVmsConsumption = calculateMovedVMConsumption(i, duration);

            double currentScore = futureGreenEnergy - futureArrivals - movedVmsConsumption - optaPlannerPowerEstimation;
            if (currentScore > bestAllocationScore){
                bestAllocationScore = currentScore;
                //deploymentTime = i;
                deploymentTime = delayForDeployment;
            }
            delayForDeployment++;
        }*/
        double futureGreenEnergy = energyManager.getWindowPredictionEnergy(now, duration);
        double movedVmsConsumption = calculateMovedVMConsumption(now, duration);
        double currentScore = futureGreenEnergy - movedVmsConsumption - optaPlannerPowerEstimation;
        return currentScore;
    }


    private double calculateMovedVMConsumption(long i, int duration) {
        double totalPower = 0d;
        //check all the postponed deployments between i and i+duration
        long start = i;
        long end = i + duration;

        // in submap end is not included
        SortedMap<Long, ArrayList<Vm>> submap = postponedDeployment.subMap(start, end);
        for(Map.Entry<Long,ArrayList<Vm>> entry : submap.entrySet()) {
            Long key = entry.getKey();
            ArrayList<Vm> vms = entry.getValue();

            for (Vm v : vms){
                totalPower += (v.getPowerEstimated()*v.getExtraParameters().getPerformance());
            }
        }

        //add "end" element
        ArrayList<Vm> endVms = postponedDeployment.get(end);
        if (endVms != null){
            for (Vm v : endVms){
                totalPower += v.getPowerEstimated();
            }
        }
        return totalPower;
    }

    /**
     * Deletes a VM and applies self-adaptation if it is enabled.
     *
     * @param vmId the ID of the VM
     */
    @Override
    public void deleteVm(final String vmId) throws CloudMiddlewareException {
        super.deleteVm(vmId);
        // RENEWIT: remove benchmark from queue in BenchmarkController
        queueBenchmarkManager.removeBenchmarkFromQueue(vmId);
    }


    private void postponeDeployment(Vm vmToDeploy) {

        //schedule future deployment
        final ArrayList<Vm> vmsPost = new ArrayList<Vm>();

        vmsPost.add(vmToDeploy);

        scheduledExecutorService.schedule(
                new Callable() {
                    public List<String> call() throws Exception {
                        return deployVms(vmsPost);
                    }
                },
                vmsPost.get(0).getTimeForDeploy(),
                TimeUnit.SECONDS);

        // add to Map
        ArrayList<Vm> vmsCopy= new ArrayList<>();
        Vm vmCopy = new Vm(vmToDeploy);
        vmsCopy.add(vmCopy);


        ArrayList<Vm> postVms = postponedDeployment.get(vmsCopy.get(0).getTimeForDeploy());

        if (postVms == null) {
            postponedDeployment.put(vmsCopy.get(0).getTimeForDeploy(), vmsCopy);
        }else{
            postVms.add(vmCopy);
        }


    }

    private void removePostponedDeploymentFromMap(String vmId, long timeForDeploy){

        ArrayList<Vm> newvmList = postponedDeployment.get(timeForDeploy);
        Iterator<Vm> itr = newvmList.iterator();
        while (itr.hasNext()) {
            Vm v = itr.next();
            if (v.getTempId().equals(vmId)) {
                itr.remove();
            }
        }

        if (postponedDeployment.get(timeForDeploy).size() == 0){
            postponedDeployment.remove(timeForDeploy);
        }else{
            postponedDeployment.put(timeForDeploy, newvmList);
        }
    }

    public EnergyManager getEnergyManager() {
        return energyManager;
    }

    public  boolean isVmShiftable(String benchmark){
        return shiftableBenchmarks.contains(benchmark);
    }
}
