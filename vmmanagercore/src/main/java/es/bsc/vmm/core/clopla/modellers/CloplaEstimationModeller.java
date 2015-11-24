package es.bsc.vmm.core.clopla.modellers;

import es.bsc.vmm.core.clopla.domain.Host;
import es.bsc.vmm.core.clopla.domain.Vm;
import es.bsc.vmm.core.manager.components.EstimatesManager;

import java.util.List;

/**
 * Created by mmacias on 24/11/15.
 */
public class CloplaEstimationModeller {
	private EstimatesManager estimatesManager;

	public CloplaEstimationModeller(EstimatesManager estimatesManager) {
		this.estimatesManager = estimatesManager;
	}

	public double getEstimation(String label, Host host, List<Vm> vmsDeployedInHost) {
		// make sure that it calls, in the next subclasses
		// pricingModeller:
		//          double result = 0.0;
		// 			for (Vm vm: vmsDeployedInHost) {
		//				result += pricingModeller.getVMChargesPrediction(vm.getNcpus(), vm.getRamMb(), vm.getDiskGb(), host.getHostname());
		// 			}
		//
		// energy Modeller:
		//			energyModeller.getHostPredictedAvgPower(
		//				host.getHostname(),
		//				CloplaConversor.cloplaVmsToVmmType(vms));
		//			}
		//

		return estimatesManager.getByLabel(label).getCloplaEstimation(host, vmsDeployedInHost);
	}
}
