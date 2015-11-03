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

package es.bsc.clurge.core.vmm.components;

import es.bsc.clurge.core.cloudmiddleware.CloudMiddleware;
import es.bsc.clurge.core.cloudmiddleware.CloudMiddlewareException;
import es.bsc.clurge.core.configuration.VmManagerConfiguration;
import es.bsc.clurge.core.db.VmManagerDb;
import es.bsc.clurge.core.logging.VMMLogger;
import es.bsc.clurge.core.vmm.DeploymentEngine;
import es.bsc.clurge.core.message_queue.MessageQueue;
import es.bsc.clurge.core.modellers.energy.EnergyModeller;
import es.bsc.clurge.core.modellers.energy.ascetic.AsceticEnergyModellerAdapter;
import es.bsc.clurge.core.modellers.price.PricingModeller;
import es.bsc.clurge.core.modellers.price.ascetic.AsceticPricingModellerAdapter;
import es.bsc.clurge.common.models.vms.Vm;
import es.bsc.clurge.common.models.vms.VmDeployed;
import es.bsc.clurge.core.monitoring.hosts.Host;
import es.bsc.clurge.core.monitoring.zabbix.ZabbixConnector;
import es.bsc.clurge.core.scheduler.Scheduler;
import es.bsc.clurge.core.selfadaptation.AfterVmDeleteSelfAdaptationRunnable;
import es.bsc.clurge.core.selfadaptation.AfterVmsDeploymentSelfAdaptationRunnable;
import es.bsc.clurge.core.selfadaptation.SelfAdaptationManager;
import es.bsc.clurge.core.utils.TimeUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VmsManager {

	private final Logger log = LogManager.getLogger(VmsManager.class);
    private final HostsManager hostsManager;
    private final CloudMiddleware cloudMiddleware;
    private final VmManagerDb db;
    private final SelfAdaptationManager selfAdaptationManager;
    private final Scheduler scheduler;
    private final PricingModeller pricingModeller;
    private final EnergyModeller energyModeller;

    private static final String ASCETIC_ZABBIX_SCRIPT_PATH = "/DFS/ascetic/vm-scripts/zabbix_agents.sh";

    public VmsManager(HostsManager hostsManager, CloudMiddleware cloudMiddleware, VmManagerDb db, 
                      SelfAdaptationManager selfAdaptationManager, 
                      EnergyModeller energyModeller, PricingModeller pricingModeller) {
        this.hostsManager = hostsManager;
        this.cloudMiddleware = cloudMiddleware;
        this.db = db;
        this.selfAdaptationManager = selfAdaptationManager;
        this.pricingModeller = pricingModeller;
        this.energyModeller = energyModeller;
        scheduler = new Scheduler(db.getCurrentSchedulingAlg(), getAllVms(), energyModeller, pricingModeller);
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
     * Performs an action on a VM (reboot, suspend, etc.).
     *
     * @param vmId the Id of the VM
     * @param action the action to perform
     */
    public void performActionOnVm(String vmId, String action) throws CloudMiddlewareException {
        switch (action) {
            case "rebootHard":
                cloudMiddleware.rebootHardVm(vmId);
                break;
            case "rebootSoft":
                cloudMiddleware.rebootSoftVm(vmId);
                break;
            case "start":
                cloudMiddleware.startVm(vmId);
                break;
            case "stop":
                cloudMiddleware.stopVm(vmId);
                break;
            case "suspend":
                cloudMiddleware.suspendVm(vmId);
                break;
            case "resume":
                cloudMiddleware.resumeVm(vmId);
                break;
            default:
                throw new IllegalArgumentException("The action selected is not supported.");
        }
        MessageQueue.publishMessageVmChangedState(getVm(vmId), action);
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
		if(isUsingZabbix()) {
			ZabbixConnector.migrateVmInZabbix(vmId, getVm(vmId).getIpAddress());
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
                SchedAlgorithmNameEnum currentSchedulingAlg = db.getCurrentSchedulingAlg();
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

    private void queueDeployedVmsMessages(List<String> deployedVmsIds) throws CloudMiddlewareException {
        for (String idDeployedVm: deployedVmsIds) {
            MessageQueue.publishMessageVmDeployed(getVm(idDeployedVm));
        }
    }

    private void initializeVmBilling(final String vmId, final String hostname, final String appId) {
        Thread thread = new Thread() {
            public void run(){
				//
				try {
					log.debug("Waiting 10 seconds before initializing VM billing. VM ID = " + vmId + "; Hostname = " + hostname);
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				pricingModeller.initializeVM(vmId,  hostname, appId);
            }
        };
        thread.start();
    }
    
}
