package es.bsc.demiurge.renewit.manager;

import es.bsc.demiurge.cloudsuiteperformancedriver.core.PerformanceDriverCore;
import es.bsc.demiurge.core.manager.GenericVmManager;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class PerformanceVmManager extends GenericVmManager {

    private PerformanceDriverCore performanceDriverCore = new PerformanceDriverCore();

    public PerformanceVmManager() {

        super();
    }

    public PerformanceDriverCore getPerformanceDriverCore() {
        return performanceDriverCore;
    }



}
