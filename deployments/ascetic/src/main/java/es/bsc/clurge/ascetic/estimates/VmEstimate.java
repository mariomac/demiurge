package es.bsc.clurge.ascetic.estimates;

import es.bsc.clurge.Clurge;
import es.bsc.clurge.estimates.Estimator;
import es.bsc.clurge.models.scheduling.DeploymentPlan;
import es.bsc.clurge.models.vms.Vm;
import es.bsc.clurge.models.vms.VmDeployed;
import es.bsc.clurge.monit.Host;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mmacias on 5/11/15.
 */
public class VmEstimate {
	private Vm vm;
	private Map<Class<? extends Estimator>, Object> estimations = new HashMap<>();

	private VmEstimate(Vm vm) {
		this.vm = vm;
	}

	public static VmEstimate generate(Vm vm, Host host, List<VmDeployed> vmsDeployed, DeploymentPlan deploymentPlan) {
		VmEstimate vme = new VmEstimate(vm);

		for(Map.Entry<Class<? extends Estimator>, Estimator> e : Clurge.INSTANCE.getEstimators().entrySet()) {
			vme.estimations.put(
					e.getKey(),
					e.getValue().getVmEstimation(vm, host, vmsDeployed, deploymentPlan));
		}

		return vme;
	}
}
