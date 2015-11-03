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

package es.bsc.clurge.core.vmm;

import es.bsc.clurge.cloudmw.CloudMiddleware;
import es.bsc.clurge.cloudmw.CloudMiddlewareException;
import es.bsc.clurge.core.Clurge;

import es.bsc.clurge.core.config.VmManagerConfiguration;

import es.bsc.clurge.core.sched.SelfAdaptationManager;
import es.bsc.clurge.core.vmm.components.*;

import es.bsc.clurge.db.PersistenceManager;
import es.bsc.clurge.models.scheduling.DeploymentPlan;
import es.bsc.clurge.models.scheduling.VmAssignmentToHost;
import es.bsc.clurge.models.vms.Vm;
import es.bsc.clurge.models.vms.VmDeployed;
import es.bsc.clurge.monit.Host;
import es.bsc.clurge.vmm.VmManager;
import org.apache.activemq.memory.buffer.MessageQueue;
import org.apache.activemq.util.TimeUtils;
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

	// TO DO: REMOVE VMS MANAGER

    // VMM components. The VMM delegates all the work to this subcomponents
    private final ImageManager imageManager;
    private final SchedulingAlgorithmsManager schedulingAlgorithmsManager;
    private final HostsManager hostsManager;
    private final VmsManager vmsManager;
    private final SelfAdaptationOptsManager selfAdaptationOptsManager;
    private final VmPlacementManager vmPlacementManager;
    private final EstimatesManager estimatesManager;
    
    private CloudMiddleware cloudMiddleware;
    private SelfAdaptationManager selfAdaptationManager;

	private final PersistenceManager db;

    private List<Host> hosts = new ArrayList<>();

    public static EnergyModeller energyModeller;
    public static PricingModeller pricingModeller;

    // Specific for the Ascetic project
    private static final String[] ASCETIC_DEFAULT_SEC_GROUPS = {"vmm_allow_all", "default"};

    private static boolean periodicSelfAdaptationThreadRunning = false;

	private Logger log = LogManager.getLogger(GenericVmManager.class);

    public GenericVmManager() {
        db = Clurge.INSTANCE.getPersistenceManager();

		VmManagerConfiguration conf = Clurge.INSTANCE.getConfiguration();

		selectMiddleware(conf.middleware);
        initializeHosts(conf.monitoring, conf.hosts);
        selectModellers(conf.project);

        // Initialize all the VMM components
        imageManager = new ImageManager(cloudMiddleware);
        schedulingAlgorithmsManager = new SchedulingAlgorithmsManager(db);
        hostsManager = new HostsManager(hosts);
        vmsManager = new VmsManager(hostsManager, cloudMiddleware, db, selfAdaptationManager,
                energyModeller, pricingModeller);
        selfAdaptationOptsManager = new SelfAdaptationOptsManager(selfAdaptationManager);
        vmPlacementManager = new VmPlacementManager(vmsManager, hostsManager, schedulingAlgorithmsManager,
                energyModeller, pricingModeller);
        estimatesManager = new EstimatesManager(vmsManager, hostsManager, db, energyModeller, pricingModeller);
        
        // Start periodic self-adaptation thread if it is not already running.
        // This check would not be needed if only one instance of this class was created.
        if (!periodicSelfAdaptationThreadRunning) {
            periodicSelfAdaptationThreadRunning = true;
            startPeriodicSelfAdaptationThread();
        }

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

		// TODO: PUT THIS AS A "ondeleteListener" PLUGIN
		// If the monitoring system is Zabbix, then we need to delete the VM from Zabbix
		if (isUsingZabbix()) {
			try {
				ZabbixConnector.deleteVmFromZabbix(vmId, vmToBeDeleted.getHostName());
			} catch(Exception e) {
				log.error(e.getMessage(),e);
			}
		}

		// todo: set this as a "ondeleteListener"
		log.debug(vmId + " destruction took " + (System.currentTimeMillis()/1000.0) + " seconds");
		MessageQueue.publishMessageVmDestroyed(vmToBeDeleted);
		performAfterVmDeleteSelfAdaptation();

		// TODO: SET THIS AS A "ONDELETELISTENER"
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// indicating vm has been stopped
					pricingModeller.getVMFinalCharges(vmId,true);
				} catch (Exception e) {
					log.warn("Error closing pricing Modeler for VM " + vmId + ": " + e.getMessage());
				}
			}
		}).start();

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
			if (VmManagerConfiguration.getInstance().deployVmWithVolume) {
				vmId = deployVmWithVolume(vmToDeploy, hostForDeployment, originalVmInitScript);
			}
			else {
				vmId = deployVm(vmToDeploy, hostForDeployment);
			}

			db.insertVm(vmId, vmToDeploy.getApplicationId(), vmToDeploy.getOvfId(), vmToDeploy.getSlaId());
			ids.put(vmToDeploy, vmId);

			VMMLogger.logVmDeploymentWaitingTime(vmId,
					TimeUtils.getDifferenceInSeconds(calendarDeployRequestReceived, Calendar.getInstance()));

			// If the monitoring system is Zabbix, then we need to call the Zabbix wrapper to initialize
			// the Zabbix agents. To register the VM we agreed to use the name <vmId>_<hostWhereTheVmIsDeployed>
			if (isUsingZabbix()) {
				ZabbixConnector.registerVmInZabbix(vmId, getVm(vmId).getHostName(), getVm(vmId).getIpAddress());
			}

			if (energyModeller instanceof AsceticEnergyModellerAdapter) {
				/**
				 * The first call sets static host information. The second
				 * writes extra profiling data for VMs. The second also
				 * writes this data to the EM's database (including the static information.
				 */
				((AsceticEnergyModellerAdapter) energyModeller).setStaticVMInformation(vmId, vmToDeploy);
				((AsceticEnergyModellerAdapter) energyModeller).initializeVmInEnergyModellerSystem(
						vmId,
						vmToDeploy.getApplicationId(),
						vmToDeploy.getImage());
			}

			if (pricingModeller instanceof AsceticPricingModellerAdapter) {
				try {
					initializeVmBilling(vmId, hostForDeployment.getHostname(), vmToDeploy.getApplicationId());
				} catch(Exception e) {
					log.error("Error when initializing vm billing: " + e.getMessage(), e);
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
    public void performActionOnVm(String vmId, String action) throws CloudMiddlewareException {
        vmsManager.performActionOnVm(vmId, action);
    }

    /**
     * Migrates a VM to a specific host.
     *
     * @param vmId the ID of the VM
     * @param destinationHostName the host where the VM will be migrated to
     */
    @Override
    public void migrateVm(String vmId, String destinationHostName) throws CloudMiddlewareException {
        vmsManager.migrateVm(vmId, destinationHostName);
    }

    /**
     * Checks whether a VM exists.
     *
     * @param vmId the ID of the VM
     * @return True if exists a VM with the input ID, false otherwise
     */
    @Override
    public boolean existsVm(String vmId) {
        return vmsManager.existsVm(vmId);
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
    // VM price and energy estimates
    //================================================================================

    /**
     * Returns price and energy estimates for a list of VMs.
     *
     * @param vmsToBeEstimated the VMs
     * @return a list with price and energy estimates for each VM
     */
    @Override
    public ListVmEstimates getVmEstimates(List<VmToBeEstimated> vmsToBeEstimated) {
        return estimatesManager.getVmEstimates(vmsToBeEstimated);
    }


    //================================================================================
    // Private Methods
    //================================================================================
    
    /**
     * Instantiates the hosts according to the monitoring software selected.
     *
     * @param monitoring the monitoring software (Ganglia, Zabbix, etc.)
     * @param hostnames the names of the hosts in the infrastructure
     */
    private void initializeHosts(VmManagerConfiguration.Monitoring monitoring, String[] hostnames) {
        switch (monitoring) {
            case OPENSTACK:
                generateOpenStackHosts(hostnames);
                break;
            case GANGLIA:
                generateGangliaHosts(hostnames);
                break;
            case ZABBIX:
                generateZabbixHosts(hostnames);
                break;
            case FAKE:
                generateFakeHosts(hostnames);
                break;
            default:
                break;
        }
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

    /**
     * Instantiates the cloud middleware.
     *
     * @param middleware the cloud middleware to be used (OpenStack, CloudStack, etc.)
     */
    private void selectMiddleware(VmManagerConfiguration.Middleware middleware) {
        switch (middleware) {
            case OPENSTACK:
                String[] securityGroups = {};
                if (usingZabbix()) { // I should check whether the VMM is configured for the Ascetic project
                    securityGroups = ASCETIC_DEFAULT_SEC_GROUPS;
                }
                cloudMiddleware = new OpenStackJclouds(getOpenStackCredentials(), securityGroups, conf.hosts);
                break;
            case FAKE:
                cloudMiddleware = new FakeCloudMiddleware(new ArrayList<HostFake>());
                break;
            default:
                throw new IllegalArgumentException("The cloud middleware selected is not supported");
        }
    }

    private void selectModellers(String project) {
        switch (project) {
            case "ascetic":
                energyModeller = new AsceticEnergyModellerAdapter();
                pricingModeller = new AsceticPricingModellerAdapter(
                        AsceticEnergyModellerAdapter.getEnergyModeller());
                break;
            default:
                energyModeller = new DummyEnergyModeller();
                pricingModeller = new DummyPricingModeller();
                break;
        }
    }

    private boolean usingZabbix() {
        return VmManagerConfiguration.getInstance().monitoring.equals(VmManagerConfiguration.Monitoring.ZABBIX);
    }

    private void startPeriodicSelfAdaptationThread() {
        Thread thread = new Thread(
                new PeriodicSelfAdaptationRunnable(selfAdaptationManager),
                "periodicSelfAdaptationThread");
        thread.start();
    }

    private OpenStackCredentials getOpenStackCredentials() {
        return new OpenStackCredentials(conf.openStackIP,
                conf.keyStonePort,
                conf.keyStoneTenant,
                conf.keyStoneUser,
                conf.keyStonePassword,
                conf.glancePort,
                conf.keyStoneTenantId);
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


	// TODO: remove the next methods
	private boolean isUsingZabbix() {
		return VmManagerConfiguration.getInstance().monitoring.equals(VmManagerConfiguration.Monitoring.ZABBIX);
	}

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
