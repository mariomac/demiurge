package es.bsc.clurge.vmm.generic;

import es.bsc.clurge.ImageManager;
import es.bsc.clurge.SchedulingManager;
import es.bsc.clurge.VirtualMachineManager;
import es.bsc.clurge.domain.VirtualMachine;
import es.bsc.clurge.exception.CloudMiddlewareException;
import es.bsc.clurge.vmm.VmAction;
import es.bsc.clurge.vmm.VmManagerListener;

import java.util.List;

/**
 * Created by mmacias on 9/11/15.
 */
public class GenericVirtualMachineManager implements VirtualMachineManager {
	private ImageManager imageManager;
	private SchedulingManager schedulingManager;


	@Override
	public ImageManager getImageManager() {
		return null;
	}

	@Override
	public SchedulingManager getSchedulingManager() {
		return null;
	}

	@Override
	public List<VirtualMachine> getAllVms() {
		return null;
	}

	@Override
	public VirtualMachine getVm(String vmId) throws CloudMiddlewareException {
		return null;
	}

	@Override
	public List<VirtualMachine> getVmsOfApp(String appId) {
		return null;
	}

	@Override
	public void deleteVmsOfApp(String appId) {

	}

	@Override
	public void deleteVm(String vmId) throws CloudMiddlewareException {

	}

	@Override
	public List<String> deployVms(List<VirtualMachine> vms) throws CloudMiddlewareException {
		return null;
	}

	@Override
	public void migrateVm(String vmId, String destinationHostName) throws CloudMiddlewareException {

	}

	@Override
	public boolean existsVm(String vmId) {
		return false;
	}

	@Override
	public void addListener(VmManagerListener listener) {

	}

	@Override
	public void removeListener(VmManagerListener listener) {

	}

	@Override
	public void performActionOnVm(String vmId, VmAction action) throws CloudMiddlewareException {

	}
}
