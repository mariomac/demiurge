package es.bsc.demiurge.renewit.ganglia;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class MetricType {
    private String name;
    private String type;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public MetricType(String name, String type) {
        this.name = name;
        this.type = type;
    }
}
