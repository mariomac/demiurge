package es.bsc.clurge.ascetic.vmm;

import es.bsc.clurge.Clurge;
import es.bsc.clurge.ascetic.modellers.price.PricingModeller;
import es.bsc.clurge.models.vms.VmDeployed;
import es.bsc.clurge.vmm.GenericVmManager;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by mmacias on 4/11/15.
 */
public class AsceticVmManager extends GenericVmManager {

	private PricingModeller pricingModeller;

	public AsceticVmManager(PricingModeller pricingModeller) {
		super();
		this.pricingModeller = pricingModeller;
		log = Logger.getLogger(AsceticVmManager.class);
	}

	public String getVmsCost(List<String> vmIds) throws Exception {
		StringBuilder sb = new StringBuilder("[");
		boolean first = true;
		for(String vmid : vmIds) {
			VmDeployed vm = Clurge.INSTANCE.getVmManager().getVm(vmid);
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
}
