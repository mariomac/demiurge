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

package es.bsc.clurge.vmm;

import es.bsc.clurge.Clurge;
import es.bsc.clurge.cloudmw.CloudMiddleware;
import es.bsc.clurge.cloudmw.CloudMiddlewareException;
import es.bsc.clurge.models.scheduling.*;
import es.bsc.clurge.ascetic.estimates.ListVmEstimates;
import es.bsc.clurge.ascetic.estimates.VmToBeEstimated;
import es.bsc.clurge.common.config.VmManagerConfiguration;

import es.bsc.clurge.sched.PeriodicSelfAdaptationRunnable;
import es.bsc.clurge.sched.SelfAdaptationManager;
import es.bsc.clurge.sched.SelfAdaptationOptions;
import es.bsc.clurge.utils.TimeUtils;

import es.bsc.clurge.db.PersistenceManager;
import es.bsc.clurge.models.images.ImageToUpload;
import es.bsc.clurge.models.images.ImageUploaded;
import es.bsc.clurge.models.vms.Vm;
import es.bsc.clurge.models.vms.VmDeployed;
import es.bsc.clurge.monit.Host;
import es.bsc.clurge.vmm.components.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Generic VM Manager.
 * For the moment, this VM Manager is used both in TUB and BSC testbeds. In the future, it might be a good
 * idea to create two different implementations.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class GenericVmManager implements VmManager {

	private static final String CONF_DEPLOY_VM_WITH_VOLUME ="deployVmWithVolume";
    // VMM components. The VMM delegates all the work to this subcomponents
    private final ImageManager imageManager;
    private final SchedulingAlgorithmsManager schedulingAlgorithmsManager;
    private final HostsManager hostsManager;
    private final SelfAdaptationOptsManager selfAdaptationOptsManager;
    private final VmPlacementManager vmPlacementManager;

    private CloudMiddleware cloudMiddleware;
    private SelfAdaptationManager selfAdaptationManager;

	private final PersistenceManager db;

    // Specific for the Ascetic project

    private static boolean periodicSelfAdaptationThreadRunning = false;

	private Logger log = LogManager.getLogger(GenericVmManager.class);

    public GenericVmManager() {
        db = Clurge.INSTANCE.getPersistenceManager();

        // Initialize all the VMM components
        imageManager = new ImageManager();
        schedulingAlgorithmsManager = new SchedulingAlgorithmsManager();
        hostsManager = new HostsManager();
        selfAdaptationOptsManager = new SelfAdaptationOptsManager(selfAdaptationManager);
        vmPlacementManager = new VmPlacementManager();

        // Start periodic self-adaptation thread if it is not already running.
        // This check would not be needed if only one instance of this class was created.
        if (!periodicSelfAdaptationThreadRunning) {
            periodicSelfAdaptationThreadRunning = true;
            startPeriodicSelfAdaptationThread();
        }

    }

	private final List<VmManagerListener> listeners = new ArrayList<>();
	@Override
	public void removeListener(VmManagerListener listener) {
		listeners.add(listener);
	}

	@Override
	public void addListener(VmManagerListener listener) {
		listeners.remove(listener);
	}

	//================================================================================
    // VM Methods
    //================================================================================

    /**
     * Returns a list of the VMs deployed.
     *
     * @return the list of VMs deployed.
     */
    @Override
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
     * Returns a specific VM deployed.
     *
     * @param vmId the ID of the VM
     * @return the VM
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
    public void deleteVm(String vmId) throws CloudMiddlewareException {
		long now = System.currentTimeMillis();
		log.debug("Destroying VM: " + vmId);
		VmDeployed vmToBeDeleted = getVm(vmId);

		cloudMiddleware.destroy(vmId);
		db.deleteVm(vmId);

		for(VmManagerListener listener : listeners) {
			try {
				listener.onVmDestruction(vmToBeDeleted);
			} catch(Exception e) {
				log.error(e.getMessage(),e);
			}
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
    @Override
    public List<String> deployVms(List<Vm> vms) throws CloudMiddlewareException {
		// Get current time to know how much each VM has to wait until it is deployed.
		Calendar calendarDeployRequestReceived = Calendar.getInstance();

		// HashMap (VmDescription,ID after deployment). Used to return the IDs in the same order that they are received
		Map<Vm, String> ids = new HashMap<>();

		DeploymentPlan deploymentPlan = Clurge.INSTANCE.getDeploymentScheduler().chooseBestDeploymentPlan(
				vms/*, VmManagerConfiguration.getInstance().deploymentEngine*/);

		// Loop through the VM assignments to hosts defined in the best deployment plan
		for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {
			Vm vmToDeploy = vmAssignmentToHost.getVm();
			Host hostForDeployment = vmAssignmentToHost.getHost();

			// Note: this is only valid for the Ascetic project
			// If the monitoring system is Zabbix, we need to make sure that the script that sets up the Zabbix
			// agents is executed.
			String originalVmInitScript = vmToDeploy.getInitScript();
			setAsceticInitScript(vmToDeploy);


			String vmId;
			if (Clurge.INSTANCE.getConfiguration().getBoolean(CONF_DEPLOY_VM_WITH_VOLUME,false)) {
				vmId = deployVmWithVolume(vmToDeploy, hostForDeployment, originalVmInitScript);
			}
			else {
				vmId = deployVm(vmToDeploy, hostForDeployment);
			}

			db.insertVm(vmId, vmToDeploy.getApplicationId(), vmToDeploy.getOvfId(), vmToDeploy.getSlaId());
			ids.put(vmToDeploy, vmId);

			log.debug("Deployment for " + vmId + " took " + TimeUtils.getDifferenceInSeconds(calendarDeployRequestReceived, Calendar.getInstance())
			 				+ " seconds");

			VmDeployed vmDeployed = getVm(vmId);
			for(VmManagerListener listener : listeners) {
				try {
					listener.onVmDeployment(vmDeployed);
				} catch(Exception e) {
					log.error(e.getMessage(),e);
				}
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

		queueDeployedVmsMessages(idsDeployedVms);
		return idsDeployedVms;
    }

    /**
     * Performs an action on a VM (reboot, suspend, etc.).
     *
     * @param vmId the Id of the VM
     * @param action the action to perform
     */
    @Override
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
		}
		VmDeployed vm = getVm(vmId);
		for(VmManagerListener l : listeners) {
			l.onVmAction(vm,action);
		}
    }

    /**
     * Migrates a VM to a specific host.
     *
     * @param vmId the ID of the VM
     * @param destinationHostName the host where the VM will be migrated to
     */
    @Override
    public void migrateVm(String vmId, String destinationHostName) throws CloudMiddlewareException {
		log.info("Migrating VM " + vmId + " to host " + destinationHostName);
		cloudMiddleware.migrate(vmId, destinationHostName);
		VmDeployed vm = getVm(vmId);
		for(VmManagerListener l : listeners) {
			l.onVmMigration(vm);
		}
	}

    /**
     * Checks whether a VM exists.
     *
     * @param vmId the ID of the VM
     * @return True if exists a VM with the input ID, false otherwise
     */
    @Override
    public boolean existsVm(String vmId) {
		return cloudMiddleware.existsVm(vmId);
    }


    //================================================================================
    // Images Methods
    //================================================================================

    /**
     * Returns all the VM images in the system.
     *
     * @return the VM images
     */
    @Override
    public List<ImageUploaded> getVmImages() {
        return imageManager.getVmImages();
    }

    /**
     * Creates an image in the system.
     *
     * @param imageToUpload the image to be created/uploaded in the system
     * @return the ID of the image
     */
    @Override
    public String createVmImage(ImageToUpload imageToUpload) throws CloudMiddlewareException {
        return imageManager.createVmImage(imageToUpload);
    }

    /**
     * Returns an image with the ID.
     *
     * @param imageId the ID of the image
     * @return the image
     */
    @Override
    public ImageUploaded getVmImage(String imageId) {
        return imageManager.getVmImage(imageId);
    }

    /**
     * Deletes a VM image.
     *
     * @param id the ID of the VM image
     */
    @Override
    public void deleteVmImage(String id) {
        imageManager.deleteVmImage(id);
    }

    /**
     * Returns the IDs of all the images in the system.
     *
     * @return the list of IDs
     */
    @Override
    public List<String> getVmImagesIds() {
        return imageManager.getVmImagesIds();
    }


    //================================================================================
    // Scheduling Algorithms Methods
    //================================================================================

    /**
     * Returns the scheduling algorithms that can be applied.
     *
     * @return the list of scheduling algorithms
     */
    @Override
    public List<SchedAlgorithmNameEnum> getAvailableSchedulingAlgorithms() {
        return schedulingAlgorithmsManager.getAvailableSchedulingAlgorithms();
    }

    /**
     * Returns the scheduling algorithm that is being used now.
     *
     * @return the scheduling algorithm being used
     */
    @Override
    public SchedAlgorithmNameEnum getCurrentSchedulingAlgorithm() {
        return schedulingAlgorithmsManager.getCurrentSchedulingAlgorithm();
    }

    /**
     * Changes the scheduling algorithm.
     *
     * @param schedulingAlg the scheduling algorithm to be used
     */
    @Override
    public void setSchedulingAlgorithm(SchedAlgorithmNameEnum schedulingAlg) {
        schedulingAlgorithmsManager.setSchedulingAlgorithm(schedulingAlg);
    }


    //================================================================================
    // VM Placement
    //================================================================================

    /**
     * Returns a list of the construction heuristics supported by the VM Manager.
     *
     * @return the list of construction heuristics
     */
    @Override
    public List<ConstructionHeuristic> getConstructionHeuristics() {
        return vmPlacementManager.getConstructionHeuristics();
    }

    /**
     * Returns a list of the local search algorithms supported by the VM Manager.
     *
     * @return the list of local search algorithms
     */
    @Override
    public List<LocalSearchAlgorithmOptionsUnset> getLocalSearchAlgorithms() {
        return vmPlacementManager.getLocalSearchAlgorithms();
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

        return vmPlacementManager.getRecommendedPlan(recommendedPlanRequest, assignVmsToCurrentHosts, vmsToDeploy);
    }

    /**
     * This function executes a deployment plan. This means that each of the VMs of the deployment plan are migrated
     * to the host specified if they were not already deployed there.
     *
     * @param deploymentPlan the deployment plan
     */
    @Override
    public void executeDeploymentPlan(VmPlacement[] deploymentPlan) throws CloudMiddlewareException {
        vmPlacementManager.executeDeploymentPlan(deploymentPlan);
    }


    //================================================================================
    // Self Adaptation
    //================================================================================

    /**
     * This function updates the configuration options for the self-adaptation capabilities of the VMM.
     *
     * @param selfAdaptationOptions the options
     */
    @Override
    public void saveSelfAdaptationOptions(SelfAdaptationOptions selfAdaptationOptions) {
        selfAdaptationOptsManager.saveSelfAdaptationOptions(selfAdaptationOptions);
    }

    /**
     * Returns the self-adaptation options for the self-adaptation capabilities of the VMM.
     *
     * @return the options
     */
    @Override
    public SelfAdaptationOptions getSelfAdaptationOptions() {
        return selfAdaptationOptsManager.getSelfAdaptationOptions();
    }


    //================================================================================
    // Hosts
    //================================================================================

    /**
     * Returns the hosts of the infrastructure.
     *
     * @return the list of hosts
     */
    @Override
    public List<Host> getHosts() {
        return hostsManager.getHosts();
    }

    /**
     * Returns a host by hostname.
     *
     * @param hostname the hostname
     * @return the host
     */
    @Override
    public Host getHost(String hostname) {
        return hostsManager.getHost(hostname);
    }

    /**
     * Simulates pressing the power button of a host
     * @param hostname the hostname
     */
    @Override
    public void pressHostPowerButton(String hostname) {
        hostsManager.pressHostPowerButton(hostname);
    }

    

    //================================================================================
    // Private Methods
    //================================================================================
    
    /**
     * Instantiates the hosts according to the monitoring software selected.
     *
     * @param hostnames the names of the hosts in the infrastructure
     */
    private void initializeHosts(String[] hostnames) {
		Clurge.INSTANCE.getMonitoringManager();
    }

    private void generateOpenStackHosts(String[] hostnames) {
        for (String hostname: hostnames) {
            hosts.add(HostFactory.getHost(hostname, HostType.OPENSTACK, cloudMiddleware));
        }
    }

    private void generateGangliaHosts(String[] hostnames) {
        for (String hostname: hostnames) {
            hosts.add(HostFactory.getHost(hostname, HostType.GANGLIA, null));
        }
    }

    private void generateZabbixHosts(String[] hostnames) {
        for (String hostname: hostnames) {
			log.debug("Generating zabbix host for host: " + hostname);
			try {
            	hosts.add(HostFactory.getHost(hostname, HostType.ZABBIX, null));
			} catch(Exception e) {
				log.error("Ignoring host due to the next error: " + e.getMessage(), e);
			}
        }
    }

    private void generateFakeHosts(String[] hostnames) {
        for (String hostname: hostnames) {
            hosts.add(HostFactory.getHost(hostname, HostType.FAKE, cloudMiddleware));
        }
    }


    private void startPeriodicSelfAdaptationThread() {
        Thread thread = new Thread(
                new PeriodicSelfAdaptationRunnable(selfAdaptationManager),
                "periodicSelfAdaptationThread");
        thread.start();
    }

	@Override
	public void executeOnDemandSelfAdaptation() throws CloudMiddlewareException {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					log.debug("Starting new Thread for self-adaptaion");
					selfAdaptationManager.applyOnDemandSelfAdaptation();
					log.debug("Self-adaptation thread ended");

				} catch (CloudMiddlewareException e) {
					log.error(e.getMessage(),e);
				}
			}
		},"onDemandSelfAdaptationThread").start();

	}

	@Override
	public String getVmsCost(List<String> vmIds) throws Exception {
		StringBuilder sb = new StringBuilder("[");
		boolean first = true;
		for(String vmid : vmIds) {
			VmDeployed vm = vmsManager.getVm(vmid);
			if(vm == null) {
				throw new Exception("VM '"+vmid+"' does not exist");
			}
			if(first) {
				first = false;
			} else {
				sb.append(',');
			}
			sb.append("{\"vmId\":\"").append(vmid)
					.append("\",\"cost\":")
					.append(pricingModeller.getVMFinalCharges(vmid,false))
					.append('}');
		}
		String retJson = sb.append(']').toString();

		log.debug("getVMscost returned: " + retJson);

		return retJson;
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


	// TODO: remove the next methods

	// this should be a plugin
	private void setAsceticInitScript(Vm vmToDeploy) {
		if (isUsingZabbix()) { // It would be more correct to check whether the VMM is running for the Ascetic project.
			Path zabbixAgentsScriptPath = FileSystems.getDefault().getPath(ASCETIC_ZABBIX_SCRIPT_PATH);
			if (Files.exists(zabbixAgentsScriptPath)) {
				vmToDeploy.setInitScript(ASCETIC_ZABBIX_SCRIPT_PATH);
			}
			else { // This is for when I perform tests locally and do not have access to the script (and
				// do not need it)
				vmToDeploy.setInitScript(null);
			}
		}
	}
}
