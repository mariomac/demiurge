package es.bsc.autonomicbenchmarks.controllers;

import es.bsc.autonomicbenchmarks.benchmarks.GenericBenchmark;
import es.bsc.autonomicbenchmarks.configuration.Conf;
import es.bsc.autonomicbenchmarks.httpClient.HttpClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class BenchmarkController implements Runnable {

    private static Logger logger = LogManager.getLogger(BenchmarkController.class);
    private QueueBenchmarkManager queueBenchmarkManager;
    private final HttpClient httpClient;
    private final String baseUrl =  Conf.INSTANCE.vmmURL;
    private final String vmsPath = "vms";

    public BenchmarkController(QueueBenchmarkManager queueBenchmarkManager) {
        this.queueBenchmarkManager = queueBenchmarkManager;
        this.httpClient =  new HttpClient();

    }

    /**
     * For each benchmark we need to check 2 things:
     *  1) It has been configured and it's running
     *  2) It has been executed and finish
     */
    @Override
    public void run() {

        while (true) {
            Iterator<GenericBenchmark> iteratorConfiguring = queueBenchmarkManager.queueBenchmarkConfiguring.iterator();
            Iterator<GenericBenchmark> iteratorExecuting= queueBenchmarkManager.queueBenchmarkExecuting.iterator();

            // Check configuring queue
            //System.out.println("Size of queue configuring: "+queueBenchmarkManager.queueBenchmarkConfiguring.size());
            while (iteratorConfiguring.hasNext()) {

                GenericBenchmark benchmark = iteratorConfiguring.next(); // must be called before you can call i.remove()

                // If benchmark is finished remove element
                if (benchmark.stepConfigured()){
                    //logger.info("Benchmark has been configured -  VM: " + benchmark.getVmId());
                    queueBenchmarkManager.benchmarkChangeState(benchmark);
                    iteratorConfiguring.remove();
                }
            }


            // Check executing queue
            //System.out.println("Size of queue executing: "+queueBenchmarkManager.queueBenchmarkExecuting.size());
            while (iteratorExecuting.hasNext()) {

                GenericBenchmark benchmark = iteratorExecuting.next(); // must be called before you can call i.remove()

                // If benchmark is finished remove element
                if (benchmark.stepExecuted()){
                    logger.info("Benchmark has terminated or runningTime expired -> Destroying VM: " + benchmark.getVmId());
                    destroyVm(benchmark.getVmId());
                    iteratorExecuting.remove();
                }
            }
            // Wait 10 seconds before checking again
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }


    public void destroyVm(String vmId) {
        try {
            httpClient.delete(baseUrl + vmsPath + "/" + vmId);
        } catch (IOException e) {
            throw new RuntimeException("Error while destroying the VM with ID=" + vmId);
        }
    }


}

