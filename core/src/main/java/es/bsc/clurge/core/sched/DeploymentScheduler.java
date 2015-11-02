package es.bsc.clurge.core.sched;

import es.bsc.clurge.core.models.scheduling.DeploymentPlan;
import es.bsc.clurge.core.models.vms.Vm;

import java.util.List;

public interface DeploymentScheduler {
    DeploymentPlan chooseBestDeploymentPlan(List<Vm> vms);
}
