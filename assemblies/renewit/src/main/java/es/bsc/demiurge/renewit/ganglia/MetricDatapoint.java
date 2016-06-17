package es.bsc.demiurge.renewit.ganglia;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class MetricDatapoint {

    private String value;
    private String timestamp;

    public MetricDatapoint(String value, String timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getValue() {
        return value;
    }

    public String getTimestamp() {
        return timestamp;
    }


}
