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

package es.bsc.vmm.core.manager;

import es.bsc.vmm.core.cloudmiddleware.CloudMiddleware;
import es.bsc.vmm.core.cloudmiddleware.CloudMiddlewareException;
import es.bsc.vmm.core.configuration.VmManagerConfiguration;
import es.bsc.vmm.core.db.VmManagerDbFactory;
import es.bsc.vmm.core.drivers.Estimator;
import es.bsc.vmm.core.drivers.Monitoring;
import es.bsc.vmm.core.drivers.VmAction;
import es.bsc.vmm.core.models.estimates.ListVmEstimates;
import es.bsc.vmm.core.models.estimates.VmToBeEstimated;
import es.bsc.vmm.core.models.images.ImageToUpload;
import es.bsc.vmm.core.models.images.ImageUploaded;
import es.bsc.vmm.core.models.vms.Vm;
import es.bsc.vmm.core.models.vms.VmDeployed;
import es.bsc.vmm.core.monitoring.hosts.Host;
import es.bsc.vmm.core.monitoring.hosts.HostFactory;
import es.bsc.vmm.core.selfadaptation.PeriodicSelfAdaptationRunnable;
import es.bsc.vmm.core.selfadaptation.SelfAdaptationManager;
import es.bsc.vmm.core.selfadaptation.options.SelfAdaptationOptions;
import es.bsc.vmm.core.db.VmManagerDb;
import es.bsc.vmm.core.manager.components.*;
import es.bsc.vmm.core.models.scheduling.*;

import es.bsc.vmm.core.vmplacement.CloplaConversor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic VM Manager.
 * For the moment, this VM Manager is used both in TUB and BSC testbeds. In the future, it might be a good
 * idea to create two different implementations.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class GenericVmManager implements VmManager {

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
	private VmManagerDb db;

    private List<Host> hosts = new ArrayList<>();

    // Specific for the Ascetic project

    private static boolean periodicSelfAdaptationThreadRunning = false;

    private static final VmManagerConfiguration conf = VmManagerConfiguration.INSTANCE;
	private Logger log = LogManager.getLogger(GenericVmManager.class);
	private HostFactory hostFactory;
    /**
     * Constructs a VmManager with the name of the database to be used.
     *
     */
    public GenericVmManager() {
        VmManagerConfiguration cfg = VmManagerConfiguration.INSTANCE;
        db = VmManagerDbFactory.getDb(cfg.dbName);

        this.cloudMiddleware = cfg.getCloudMiddleware();

        initializeHosts(conf.getMonitoring(), conf.hosts);

        selfAdaptationManager = new SelfAdaptationManager(this, conf.dbName);

		hostFactory = cfg.getHostFactory();

        // Initialize all the VMM components
        imageManager = new ImageManager(cloudMiddleware);
        schedulingAlgorithmsManager = new SchedulingAlgorithmsManager(db, cfg.getSchedulingAlgorithmsRepository());
        hostsManager = new HostsManager(hosts);
		estimatesManager = new EstimatesManager(this, cfg.getEstimators(), cfg.getSchedulingAlgorithmsRepository());

        vmsManager = new VmsManager(hostsManager, cloudMiddleware, db, selfAdaptationManager, estimatesManager,
				cfg.getSchedulingAlgorithmsRepository(), cfg.getVmmListeners());
        selfAdaptationOptsManager = new SelfAdaptationOptsManager(selfAdaptationManager);
        vmPlacementManager = new VmPlacementManager(vmsManager, hostsManager, schedulingAlgorithmsManager,estimatesManager);

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
        return vmsManager.getAllVms();
    }

    /**
     * Returns a specific VM deployed.
     *
     * @param vmId the ID of the VM
     * @return the VM
     */
    @Override
    public VmDeployed getVm(String vmId) throws CloudMiddlewareException {
        return vmsManager.getVm(vmId);
    }

    /**
     * Returns all the VMs deployed that belong to a specific application.
     *
     * @param appId the ID of the application
     * @return the list of VMs
     */
    @Override
    public List<VmDeployed> getVmsOfApp(String appId) {
        return vmsManager.getVmsOfApp(appId);
    }

    /**
     * Deletes all the VMs that belong to a specific application.
     *
     * @param appId the ID of the application
     */
    @Override
    public void deleteVmsOfApp(String appId) {
        vmsManager.deleteVmsOfApp(appId);
    }

    /**
     * Deletes a VM and applies self-adaptation if it is enabled.
     *
     * @param vmId the ID of the VM
     */
    @Override
    public void deleteVm(String vmId) throws CloudMiddlewareException {
        vmsManager.deleteVm(vmId);
    }

    /**
     * Deploys a list of VMs and returns its IDs.
     *
     * @param vms the VMs to deploy
     * @return the IDs of the VMs deployed in the same order that they were received
     */
    @Override
    public List<String> deployVms(List<Vm> vms) throws CloudMiddlewareException {
        return vmsManager.deployVms(vms);
    }

    /**
     * Performs an action on a VM (reboot, suspend, etc.).
     *
     * @param vmId the Id of the VM
     * @param action the action to perform
     */
    @Override
    public void performActionOnVm(String vmId, String action) throws CloudMiddlewareException {
        vmsManager.performActionOnVm(vmId, VmAction.fromCamelCase(action));
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
    public List<String> getAvailableSchedulingAlgorithms() {
        return schedulingAlgorithmsManager.getAvailableSchedulingAlgorithms();
    }

    /**
     * Returns the scheduling algorithm that is being used now.
     *
     * @return the scheduling algorithm being used
     */
    @Override
    public String getCurrentSchedulingAlgorithm() {
        return schedulingAlgorithmsManager.getCurrentSchedulingAlgorithm();
    }

    /**
     * Changes the scheduling algorithm.
     *
     * @param schedulingAlg the scheduling algorithm to be used
     */
    @Override
    public void setSchedulingAlgorithm(String schedulingAlg) {
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
    private void initializeHosts(Monitoring monitoring, String[] hostnames) {
		HostFactory hf = VmManagerConfiguration.INSTANCE.getHostFactory();

		for(String hostname : hostnames) {
			hosts.add(hf.getHost(hostname));
		}

    }
/*
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
*/
    private void startPeriodicSelfAdaptationThread() {
        Thread thread = new Thread(
                new PeriodicSelfAdaptationRunnable(selfAdaptationManager),
                "periodicSelfAdaptationThread");
        thread.start();
    }

	/*
    private OpenStackCredentials getOpenStackCredentials() {
        return new OpenStackCredentials(conf.openStackIP,
                conf.keyStonePort,
                conf.keyStoneTenant,
                conf.keyStoneUser,
                conf.keyStonePassword,
                conf.glancePort,
                conf.keyStoneTenantId);
    }
    */

	@Override
	public HostsManager getHostsManager() {
		return hostsManager;
	}

	@Override
	public VmManagerDb getDB() {
		return db;
	}

	@Override
	public VmsManager getVmsManager() {
		return vmsManager;
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
	public String getVmsEstimates(List<String> vmIds) throws Exception {
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
			sb.append("{\"vmId\":\"").append(vmid);
			for(Estimator estimator : estimatesManager) {
				sb.append("\",\"");
				sb.append(estimator.getLabel());
				sb.append("\":");
				Map<String,Object> options = new HashMap<>();
				options.put("undeployed",false); // hack for ascetic pricing modeler
				sb.append(estimator.getCurrentEstimation(vmid,options));
			}
			sb.append('}');
		}
		String retJson = sb.append(']').toString();

		log.debug("getVMscost returned: " + retJson);

		return retJson;
	}
}
