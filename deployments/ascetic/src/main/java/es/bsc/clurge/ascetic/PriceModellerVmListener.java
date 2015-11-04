package es.bsc.clurge.ascetic;

import es.bsc.clurge.ascetic.modellers.price.PricingModeller;
import es.bsc.clurge.models.vms.VmDeployed;
import es.bsc.clurge.vmm.VmAction;
import es.bsc.clurge.vmm.VmManagerListener;
import org.apache.log4j.LogManager;

/**
 * Created by mmacias on 3/11/15.
 */
public class PriceModellerVmListener implements VmManagerListener {

	private PricingModeller pricingModeller;

	public PriceModellerVmListener(PricingModeller pricingModeller) {
		this.pricingModeller = pricingModeller;
	}

	@Override
	public void onVmDeployment(final VmDeployed vm) {
		Thread thread = new Thread() {
			public void run(){
				//
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				pricingModeller.initializeVM(vm.getId(),  vm.getHostName(), vm.getApplicationId());
			}
		};
		thread.start();
	}

	@Override
	public void onVmDestruction(VmDeployed vm) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// indicating vm has been stopped
					pricingModeller.getVMFinalCharges(vm.getId(),true);
				} catch (Exception e) {
					LogManager.getLogger(PriceModellerVmListener.class).warn("Error closing pricing Modeler for VM " + vm.getId() + ": " + e.getMessage(), e);
				}
			}
		}).start();
	}

	@Override
	public void onVmMigration(VmDeployed vm) {

	}

	@Override
	public void onVmAction(VmDeployed vm, VmAction action) {

	}
}
