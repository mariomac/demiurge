package es.bsc.demiurge.cloudsuiteperformancedriver.workloads;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

public class Workload {

    private final List<BenchmarkExecution> benchmarkExecutionList = new ArrayList<>();

    public Workload(List<BenchmarkExecution> benchmarkExecutionList) {
        this.benchmarkExecutionList.addAll(benchmarkExecutionList);
    }

    public List<BenchmarkExecution> getBenchmarkExecutionList() {
        return new ArrayList<>(benchmarkExecutionList);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("benchmarkExecutionList", benchmarkExecutionList)
                .toString();
    }

}
