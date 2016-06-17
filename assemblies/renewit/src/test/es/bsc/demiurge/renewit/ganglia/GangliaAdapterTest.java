package es.bsc.demiurge.renewit.ganglia;

import org.junit.Test;

import static es.bsc.demiurge.renewit.ganglia.GangliaAdapter.getHostGangliaMetrics;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class GangliaAdapterTest {

    @Test
    public void readPower(){
        long time = System.currentTimeMillis()/1000;
        getHostGangliaMetrics("bscgrid28",time, time);
    }
}
