/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.vmm.core.manager.components;

import es.bsc.vmm.core.cloudmiddleware.CloudMiddleware;
import es.bsc.vmm.core.cloudmiddleware.CloudMiddlewareException;
import es.bsc.vmm.core.configuration.VmManagerConfiguration;
import es.bsc.vmm.core.drivers.VmAction;
import es.bsc.vmm.core.drivers.VmmListener;
import es.bsc.vmm.core.logging.VMMLogger;
import es.bsc.vmm.core.manager.DeploymentEngine;
import es.bsc.vmm.core.models.scheduling.*;
import es.bsc.vmm.core.models.vms.Vm;
import es.bsc.vmm.core.monitoring.hosts.Host;
import es.bsc.vmm.core.scheduler.Scheduler;
import es.bsc.vmm.core.scheduler.SchedulingAlgorithmsRepository;
import es.bsc.vmm.core.utils.TimeUtils;
import es.bsc.vmm.core.db.VmManagerDb;
import es.bsc.vmm.core.models.vms.VmDeployed;
import es.bsc.vmm.core.selfadaptation.AfterVmDeleteSelfAdaptationRunnable;
import es.bsc.vmm.core.selfadaptation.AfterVmsDeploymentSelfAdaptationRunnable;
import es.bsc.vmm.core.selfadaptation.SelfAdaptationManager;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VmsManager {

	private final Logger log = LogManager.getLogger(VmsManager.class);
    private final HostsManager hostsManager;
    private final CloudMiddleware cloudMiddleware;
    private final VmManagerDb db;
    private final SelfAdaptationManager selfAdaptationManager;
    private final Scheduler scheduler;
    private final EstimatesManager estimatorsManager;
	private final List<VmmListener> listeners;

//    private static final String ASCETIC_ZABBIX_SCRIPT_PATH = "/DFS/ascetic/vm-scripts/zabbix_agents.sh";

    public VmsManager(HostsManager hostsManager, CloudMiddleware cloudMiddleware, VmManagerDb db, 
                      SelfAdaptationManager selfAdaptationManager,
					  EstimatesManager estimatorsManager,
					  SchedulingAlgorithmsRepository schedulingAlgorithmsRepository,
					  List<VmmListener> listeners
					  ) {
		this.listeners = listeners;

        this.hostsManager = hostsManager;
        this.cloudMiddleware = cloudMiddleware;
        this.db = db;
        this.selfAdaptationManager = selfAdaptationManager;
        this.estimatorsManager = estimatorsManager;
        scheduler = new Scheduler(db.getCurrentSchedulingAlg(), getAllVms(), estimatorsManager, schedulingAlgorithmsRepository);
    }
    
    /**
     * Returns a list of the VMs deployed.
     *
     * @return the list of VMs deployed.
     */
    public List<VmDeployed> getAllVms() {
        List<VmDeployed> result = new ArrayList<>();
        for (String vmId: cloudMiddleware.getAllVMsIds()) {
			try {
            	result.add(getVm(vmId));
			} catch(CloudMiddlewareException ex) {
				log.warn("Ignoring this exception, which should never happen: " + ex.getMessage(), ex);
			}
        }
        return result;
    }

    /**
     * Returns the VMs that have been scheduled (have been assigned a host), but have not been deployed yet.
     *
     * @return the list of VMs that have been scheduled, but not deployed
     */
    public List<VmDeployed> getScheduledNonDeployedVms() throws CloudMiddlewareException {
        // It might seem confusing that this function returns a list of VmDeployed instead of Vm.
        // The reason is that the Vm class does not have a host assigned whereas VmDeployed does.
        // This is a temporary solution. I need to create a new 'ScheduledNonDeployedVm' class separated from
        // Vm and VmDeployed.

        List<VmDeployed> result = new ArrayList<>();
        for (String vmId: cloudMiddleware.getScheduledNonDeployedVmsIds()) {
            result.add(getVm(vmId));
        }
        return result;
    }

    /**
     * Returns a specific VM deployed.
     *
     * @param vmId the ID of the VM
     * @return the VM
     */
    public VmDeployed getVm(String vmId) throws CloudMiddlewareException {
        // We need to set the state information of the VM that is not managed by the cloud middleware here.
        // Currently, we need to set the Application ID, the OVF ID, and the SLA ID.
        // The OVF and the SLA IDs are specific for Ascetic.
        VmDeployed vm = cloudMiddleware.getVM(vmId);
        if (vm != null) {
            vm.setApplicationId(db.getAppIdOfVm(vm.getId()));
            vm.setOvfId(db.getOvfIdOfVm(vm.getId()));
            vm.setSlaId(db.getSlaIdOfVm(vm.getId()));
        }
        return vm;
    }

    /**
     * Returns all the VMs deployed that belong to a specific application.
     *
     * @param appId the ID of the application
     * @return the list of VMs
     */
    public List<VmDeployed> getVmsOfApp(String appId) {
        List<VmDeployed> result = new ArrayList<>();
        for (String vmId: db.getVmsOfApp(appId)) {
			try {
				result.add(getVm(vmId));
			} catch (CloudMiddlewareException e) {
				log.error(e.getMessage(),e);
			}
		}
        return result;
    }

    /**
     * Deletes all the VMs that belong to a specific application.
     *
     * @param appId the ID of the application
     */
    public void deleteVmsOfApp(String appId) {
        for (VmDeployed vmDeployed: getVmsOfApp(appId)) {
			try {
            	deleteVm(vmDeployed.getId());
			} catch(CloudMiddlewareException ex) {
				log.error(ex.getMessage(), ex);

			}
        }
    }

    /**
     * Deletes a VM and applies self-adaptation if it is enabled.
     *
     * @param vmId the ID of the VM
     */
    public void deleteVm(final String vmId) throws CloudMiddlewareException {
		long now = System.currentTimeMillis();
		log.debug("Destroying VM: " + vmId);
        VmDeployed vmToBeDeleted = getVm(vmId);

        cloudMiddleware.destroy(vmId);
        db.deleteVm(vmId);

		for(VmmListener l : listeners) {
			l.onVmDestruction(vmToBeDeleted);
		}

		log.debug(vmId + " destruction took " + (System.currentTimeMillis()/1000.0) + " seconds");
		performAfterVmDeleteSelfAdaptation();

	}

    /**
     * Deploys a list of VMs and returns its IDs.
     *
     * @param vms the VMs to deploy
     * @return the IDs of the VMs deployed in the same order that they were received
     */
    public List<String> deployVms(List<Vm> vms) throws CloudMiddlewareException {
        // Get current time to know how much each VM has to wait until it is deployed.
        Calendar calendarDeployRequestReceived = Calendar.getInstance();

        // HashMap (VmDescription,ID after deployment). Used to return the IDs in the same order that they are received
        Map<Vm, String> ids = new HashMap<>();

        DeploymentPlan deploymentPlan = chooseBestDeploymentPlan(
                vms, VmManagerConfiguration.INSTANCE.deploymentEngine);

        // Loop through the VM assignments to hosts defined in the best deployment plan
        for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {
            Vm vmToDeploy = vmAssignmentToHost.getVm();
            Host hostForDeployment = vmAssignmentToHost.getHost();

            // Note: this is only valid for the Ascetic project
            // If the monitoring system is Zabbix, we need to make sure that the script that sets up the Zabbix
            // agents is executed.
            String originalVmInitScript = vmToDeploy.getInitScript();
//			AFAIK this is not anymore needed for ascetic y2
//            setAsceticInitScript(vmToDeploy);


            String vmId;
            if (VmManagerConfiguration.INSTANCE.deployVmWithVolume) {
                vmId = deployVmWithVolume(vmToDeploy, hostForDeployment, originalVmInitScript);
            }
            else {
                vmId = deployVm(vmToDeploy, hostForDeployment);
            }

            db.insertVm(vmId, vmToDeploy.getApplicationId(), vmToDeploy.getOvfId(), vmToDeploy.getSlaId());
            ids.put(vmToDeploy, vmId);

            VMMLogger.logVmDeploymentWaitingTime(vmId,
                    TimeUtils.getDifferenceInSeconds(calendarDeployRequestReceived, Calendar.getInstance()));

			VmDeployed vmDeployed = getVm(vmId);
			for(VmmListener vml : listeners) {
				vml.onVmDeployment(vmDeployed);
			}

            if (vmToDeploy.needsFloatingIp()) {
                cloudMiddleware.assignFloatingIp(vmId);
            }
        }

        performAfterVmsDeploymentSelfAdaptation();

        // Return the IDs of the VMs deployed in the same order that they were received
        List<String> idsDeployedVms = new ArrayList<>();
        for (Vm vm: vms) {
            idsDeployedVms.add(ids.get(vm));
        }

        return idsDeployedVms;
    }

    /**
     * Performs an action on a VM (reboot, suspend, etc.).
     *
     * @param vmId the Id of the VM
     * @param action the action to perform
     */
    public void performActionOnVm(String vmId, VmAction action) throws CloudMiddlewareException {
        switch (action) {
			case REBOOT_HARD:
                cloudMiddleware.rebootHardVm(vmId);
                break;
			case REBOOT_SOFT:
                cloudMiddleware.rebootSoftVm(vmId);
                break;
			case START:
                cloudMiddleware.startVm(vmId);
                break;
			case STOP:
                cloudMiddleware.stopVm(vmId);
                break;
			case SUSPEND:
                cloudMiddleware.suspendVm(vmId);
                break;
			case RESUME:
                cloudMiddleware.resumeVm(vmId);
                break;
            default:
                throw new IllegalArgumentException("The action selected is not supported.");
        }
		VmDeployed vm = getVm(vmId);
		for(VmmListener l : listeners) {
			l.onVmAction(vm, action);
		}
    }

    /**
     * Migrates a VM to a specific host.
     *
     * @param vmId the ID of the VM
     * @param destinationHostName the host where the VM will be migrated to
     */
    public void migrateVm(String vmId, String destinationHostName) throws CloudMiddlewareException {
        VMMLogger.logMigration(vmId, destinationHostName);
        cloudMiddleware.migrate(vmId, destinationHostName);
		VmDeployed vm = getVm(vmId);
		for(VmmListener l : listeners) {
			l.onVmMigration(vm);
		}

    }

    /**
     * Checks whether a VM exists.
     *
     * @param vmId the ID of the VM
     * @return True if exists a VM with the input ID, false otherwise
     */
    public boolean existsVm(String vmId) {
        return cloudMiddleware.existsVm(vmId);
    }


    private String deployVm(Vm vm, Host host) throws CloudMiddlewareException {
        // If the host is not on, turn it on and wait
        if (!host.isOn()) {
            hostsManager.pressHostPowerButton(host.getHostname());
            while (!host.isOn()) { // Find a better way to do this
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return cloudMiddleware.deploy(vm, host.getHostname());
    }

    private String deployVmWithVolume(Vm vm, Host host, String isoPath) throws CloudMiddlewareException {
        // If the host is not on, turn it on and wait
        if (!host.isOn()) {
            hostsManager.pressHostPowerButton(host.getHostname());
            while (!host.isOn()) { // Find a better way to do this
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return cloudMiddleware.deployWithVolume(vm, host.getHostname(), isoPath);
    }

    private void performAfterVmDeleteSelfAdaptation() {
        // Execute self adaptation in a separate thread, because we need to give an answer without
        // waiting for the self-adaptation to finish
        Thread thread = new Thread(
                new AfterVmDeleteSelfAdaptationRunnable(selfAdaptationManager),
                "afterVmDeleteSelfAdaptationThread");
        thread.start();
    }

    private void performAfterVmsDeploymentSelfAdaptation() {
        Logger.getLogger(VmsManager.class).warn("**** AFTER VMs DEPLOYMENT SELF-ADAPTATION IS DISABLED ***");
        Thread thread = new Thread(
                new AfterVmsDeploymentSelfAdaptationRunnable(selfAdaptationManager),
                "afterVmsDeploymentSelfAdaptationThread");
        thread.start();
    }

    private DeploymentPlan chooseBestDeploymentPlan(List<Vm> vms, DeploymentEngine deploymentEngine) throws CloudMiddlewareException {
        switch (deploymentEngine) {
            case LEGACY:
                // The scheduling algorithm could have been changed. Therefore, we need to set it again.
                // This is a quick fix. I need to find a way of telling the system to update properly the
                // scheduling algorithm when using the legacy deployment engine. This does not occur when using
                // the optaplanner deployment engine.
                String currentSchedulingAlg = db.getCurrentSchedulingAlg();
                scheduler.setSchedAlgorithm(currentSchedulingAlg);
                return scheduler.chooseBestDeploymentPlan(vms, hostsManager.getHosts());
            case OPTAPLANNER:
                if (repeatedNameInVmList(vms)) {
                    throw new IllegalArgumentException("There was an error while choosing a deployment plan.");
                }

                RecommendedPlan recommendedPlan = selfAdaptationManager.getRecommendedPlanForDeployment(vms);

                // Construct deployment plan from recommended plan with only the VMs that we want to deploy,
                // we do not need here the ones that are already deployed even though they appear in the plan
                List<VmAssignmentToHost> vmAssignmentToHosts = new ArrayList<>();
                for (Vm vm: vms) {
                    // TODO: analyze if this works when some VMs have a preferred host and others do not
                    if (vm.getPreferredHost() != null && !vm.getPreferredHost().equals("")) {
                        vmAssignmentToHosts.add(new VmAssignmentToHost(
                                vm, hostsManager.getHost(vm.getPreferredHost())));
                    }
                    else {
                        VmPlacement vmPlacement = findVmPlacementByVmId(
                                recommendedPlan.getVMPlacements(), vm.getName());
                        Host host = hostsManager.getHost(vmPlacement.getHostname());
                        vmAssignmentToHosts.add(new VmAssignmentToHost(vm, host));
                    }
                }
                return new DeploymentPlan(vmAssignmentToHosts);
            default:
                throw new IllegalArgumentException("The deployment engine selected is not supported.");
        }
    }

//		AFAIK this is not needed for ascetic y2
//    private void setAsceticInitScript(Vm vmToDeploy) {
//        if (isUsingZabbix()) { // It would be more correct to check whether the VMM is running for the Ascetic project.
//            Path zabbixAgentsScriptPath = FileSystems.getDefault().getPath(ASCETIC_ZABBIX_SCRIPT_PATH);
//            if (Files.exists(zabbixAgentsScriptPath)) {
//                vmToDeploy.setInitScript(ASCETIC_ZABBIX_SCRIPT_PATH);
//            }
//            else { // This is for when I perform tests locally and do not have access to the script (and
//                // do not need it)
//                vmToDeploy.setInitScript(null);
//            }
//        }
//    }

    private boolean repeatedNameInVmList(List<Vm> vms) {
        for (int i = 0; i < vms.size(); ++i) {
            for (int j = i + 1; j < vms.size(); ++j) {
                if (vms.get(i).getName().equals(vms.get(j).getName())) {
                    return false;
                }
            }
        }
        return false;
    }

    private VmPlacement findVmPlacementByVmId(VmPlacement[] vmPlacements, String vmId) {
        for (VmPlacement vmPlacement: vmPlacements) {
            if (vmId.equals(vmPlacement.getVmId())) {
                return vmPlacement;
            }
        }
        return null;
    }



    
}