package es.bsc.autonomicbenchmarks.controllers;

import es.bsc.autonomicbenchmarks.benchmarks.GenericBenchmark;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class QueueBenchmarkManager {

    final BlockingQueue<GenericBenchmark> queueBenchmarkConfiguring = new LinkedBlockingQueue<>();
    final BlockingQueue<GenericBenchmark> queueBenchmarkExecuting = new LinkedBlockingQueue<>();

    final HashMap<String, GenericBenchmark> vmBenchmark = new HashMap<>();

    public boolean addBenchmarkToQueue(GenericBenchmark b){
        vmBenchmark.put(b.getVmId(), b);
        return queueBenchmarkConfiguring.offer(b);
    }

    public void removeBenchmarkFromQueue(String vmId){
        // Remove Benchmark from both the queues
        if (!queueBenchmarkExecuting.remove(vmBenchmark.get(vmId))){
            queueBenchmarkConfiguring.remove(vmBenchmark.get(vmId));
        }
    }

    public void benchmarkChangeState(GenericBenchmark b){
        // Remove from configuring and put in executing
        queueBenchmarkConfiguring.remove(b);
        queueBenchmarkExecuting.offer(b);
    }

}
