package es.bsc.demiurge.core.predictors;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class EnergyFileModel {

    private long time;
    private int totalEnergy;
    private int renewableEnergy;
    private double RES;

    public EnergyFileModel(long time, int totalEnergy, int renewableEnergy, double RES) {
        this.time = time;
        this.totalEnergy = totalEnergy;
        this.renewableEnergy = renewableEnergy;
        this.RES = RES;
    }

    public long getTime() {
        return time;
    }

    public int getTotalEnergy() {
        return totalEnergy;
    }

    public int getRenewableEnergy() {
        return renewableEnergy;
    }

    public double getRES() {
        return RES;
    }
}
