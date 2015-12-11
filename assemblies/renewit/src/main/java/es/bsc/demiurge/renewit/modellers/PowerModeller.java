package es.bsc.demiurge.renewit.modellers;

import es.bsc.demiurge.core.clopla.domain.Host;
import es.bsc.demiurge.core.clopla.domain.Vm;
import es.bsc.demiurge.core.drivers.Estimator;
import es.bsc.demiurge.core.models.scheduling.DeploymentPlan;
import es.bsc.demiurge.core.models.scheduling.VmAssignmentToHost;
import es.bsc.demiurge.core.models.vms.VmDeployed;

import java.util.List;
import java.util.Map;

/**
 * Dummy energy modeller. To be implemented by Mauro's model
 *
 * @author Mario Macias (http://github.com/mariomac), Mauro Canuto (mauro.canuto@bsc.es)
 */
public class PowerModeller implements Estimator {

	private double DUMMY_POWER_PER_CPU_CORE = 30;
	private double DUMMY_POWER_PER_IDLE_HOST = 50;

	@Override
	public String getLabel() {
		return "power";
	}

	/**
	 * By the moment, calculates dummy power
	 * @param vma
	 * @param vmsDeployed
	 * @param deploymentPlan
	 * @return
	 */
	@Override
	public double getDeploymentEstimation(VmAssignmentToHost vma, List<VmDeployed> vmsDeployed, DeploymentPlan deploymentPlan) {
		double pow = 0;

		return pow;
	}

	@Override
	public double getCurrentEstimation(String vmId, Map options) {
		throw new AssertionError("this should never call. VMM is configured for Clopla and must get rid of legacy scheduler");
	}

	@Override
	public double getCloplaEstimation(Host host, List<Vm> vmsDeployedInHost) {
		return 0;
	}
}
