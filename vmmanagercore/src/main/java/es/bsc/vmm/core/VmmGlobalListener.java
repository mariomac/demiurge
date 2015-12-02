package es.bsc.vmm.core;

import es.bsc.vmm.core.configuration.VmmConfig;

/**
 * @author Mario Macías (http://github.com/mariomac)
 */
public interface VmmGlobalListener {
    void onVmmStart();
    void onVmmStop();
}
