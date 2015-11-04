package es.bsc.clurge.estimates;

import es.bsc.clurge.models.vms.Vm;

/**
 * Created by mmacias on 4/11/15.
 */
public abstract class AbstractVmEstimate {
	private final Vm vm;

	public AbstractVmEstimate(Vm vm) {
		this.vm = vm;
	}

	public final Vm getVm() {
		return vm;
	}
}
