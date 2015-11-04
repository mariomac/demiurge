package es.bsc.clurge.common.sched;

import es.bsc.clurge.common.models.scheduling.DeploymentPlan;
import es.bsc.clurge.common.models.vms.Vm;

import java.util.List;

public interface DeploymentScheduler {
    DeploymentPlan chooseBestDeploymentPlan(List<Vm> vms);
}
