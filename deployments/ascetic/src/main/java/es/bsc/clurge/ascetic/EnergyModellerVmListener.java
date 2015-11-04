package es.bsc.clurge.ascetic;

import es.bsc.clurge.models.vms.VmDeployed;
import es.bsc.clurge.vmm.VmManagerListener;

/**
 * Created by mmacias on 3/11/15.
 */
public class EnergyModellerVmListener implements VmManagerListener{
	@Override
	public void onVmDeployment(VmDeployed vm) {
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

	@Override
	public void onVmDestruction(VmDeployed vm) {

	}

	@Override
	public void onVmMigration(VmDeployed vm) {

	}
}
