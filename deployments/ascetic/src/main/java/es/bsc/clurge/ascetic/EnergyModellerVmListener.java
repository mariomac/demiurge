package es.bsc.clurge.ascetic;

import es.bsc.clurge.ascetic.modellers.energy.EnergyModeller;
import es.bsc.clurge.ascetic.modellers.energy.ascetic.AsceticEnergyModellerAdapter;
import es.bsc.clurge.models.vms.VmDeployed;
import es.bsc.clurge.vmm.VmAction;
import es.bsc.clurge.vmm.VmManagerListener;

/**
 * Created by mmacias on 3/11/15.
 */
public class EnergyModellerVmListener implements VmManagerListener{
	private EnergyModeller energyModeller;

	public EnergyModellerVmListener(EnergyModeller energyModeller) {
		this.energyModeller = energyModeller;
	}

	@Override
	public void onVmAction(VmDeployed vm, VmAction action) {

	}

	@Override
	public void onVmDeployment(VmDeployed vm) {
		/**
		 * The first call sets static host information. The second
		 * writes extra profiling data for VMs. The second also
		 * writes this data to the EM's database (including the static information.
		 */
		((AsceticEnergyModellerAdapter) energyModeller).setStaticVMInformation(vm.getId(), vm);
		((AsceticEnergyModellerAdapter) energyModeller).initializeVmInEnergyModellerSystem(
				vm.getId(),
				vm.getApplicationId(),
				vm.getImage());
	}

	@Override
	public void onVmDestruction(VmDeployed vm) {

	}

	@Override
	public void onVmMigration(VmDeployed vm) {

	}
}
