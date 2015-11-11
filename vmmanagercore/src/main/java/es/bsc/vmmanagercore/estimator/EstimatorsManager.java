package es.bsc.vmmanagercore.estimator;


import es.bsc.vmmanagercore.drivers.Estimator;

import java.util.*;

public class EstimatorsManager implements Iterable<Estimator> {
    private Map<Class<? extends Estimator>, Estimator> estimators = new HashMap<>();

    public EstimatorsManager(Set<Estimator> estimators) {
        for(Estimator e: estimators) {
            this.estimators.put(e.getClass(),e);
        }
    }

    public Estimator get(Class<? extends Estimator> e) {
        return estimators.get(e);
    }

    @Override
    public Iterator<Estimator> iterator() {
        return estimators.values().iterator();
    }

}
