package es.bsc.clurge.core.vmm.components;

import es.bsc.clurge.core.selfadaptation.SelfAdaptationManager;
import es.bsc.clurge.core.selfadaptation.options.SelfAdaptationOptions;

public class SelfAdaptationOptsManager {

    private final SelfAdaptationManager selfAdaptationManager;
    
    public SelfAdaptationOptsManager(SelfAdaptationManager selfAdaptationManager) {
        this.selfAdaptationManager = selfAdaptationManager;
    }
    
    /**
     * This function updates the configuration options for the self-adaptation capabilities of the VMM.
     *
     * @param selfAdaptationOptions the options
     */
    public void saveSelfAdaptationOptions(SelfAdaptationOptions selfAdaptationOptions) {
        selfAdaptationManager.saveSelfAdaptationOptions(selfAdaptationOptions);
    }

    /**
     * Returns the self-adaptation options for the self-adaptation capabilities of the VMM.
     *
     * @return the options
     */
    public SelfAdaptationOptions getSelfAdaptationOptions() {
        return selfAdaptationManager.getSelfAdaptationOptions();
    }
    
}
