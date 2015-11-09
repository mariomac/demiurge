package es.bsc.clurge.sched;

import es.bsc.clurge.models.vms.Vm;

import java.util.List;

public interface DeploymentScheduler {
    DeploymentPlan chooseBestDeploymentPlan(List<Vm> vms);
}
