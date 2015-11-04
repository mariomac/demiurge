package es.bsc.clurge.sched;

import es.bsc.clurge.models.vms.Vm;
import es.bsc.clurge.models.scheduling.DeploymentPlan;

import java.util.List;

public interface DeploymentScheduler {
    DeploymentPlan chooseBestDeploymentPlan(List<Vm> vms);
}
