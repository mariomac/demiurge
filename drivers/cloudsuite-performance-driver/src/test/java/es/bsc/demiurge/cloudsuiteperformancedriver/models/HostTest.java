package es.bsc.demiurge.cloudsuiteperformancedriver.models;

import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class HostTest {

    @Test
    public void detectsItHasEnoughSpaceToHostAVm() {
        Host host = new Host("testHost", 4, 4, 4, 1, 1, 1);
        assertTrue(host.hasEnoughSpaceToHost(new VmSize(1, 1, 1)));
    }

    @Test
    public void detectsItHasEnoughSpaceToHostAVmThatOccupiesAllTheAvailableSpace() {
        Host host = new Host("testHost", 4, 4, 4, 1, 1, 1);
        assertTrue(host.hasEnoughSpaceToHost(new VmSize(3, 3, 3)));
    }

    @Test
    public void detectsItDoesNotHaveEnoughSpaceToHostAVm() {
        Host host = new Host("testHost", 4, 4, 4, 1, 1, 1);
        assertFalse(host.hasEnoughSpaceToHost(new VmSize(8, 8, 8)));
    }

}
