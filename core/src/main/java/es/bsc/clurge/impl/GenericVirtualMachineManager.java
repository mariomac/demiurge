package es.bsc.clurge.impl;

import es.bsc.clurge.ImageManager;
import es.bsc.clurge.PhysicalHostManager;
import es.bsc.clurge.SchedulingManager;
import es.bsc.clurge.VirtualMachineManager;

/**
 * Created by mmacias on 5/11/15.
 */
public class GenericVirtualMachineManager implements VirtualMachineManager {
	PhysicalHostManager physicalHostManager;
	SchedulingManager schedulingManager;
	ImageManager imageManager;

}
