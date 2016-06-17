package es.bsc.demiurge.renewit.ganglia;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")

public class GangliaSchema {

    @SerializedName("ds_name")
    @Expose
    private String dsName;
    @SerializedName("cluster_name")
    @Expose
    private String clusterName;
    @SerializedName("graph_type")
    @Expose
    private String graphType;
    @SerializedName("host_name")
    @Expose
    private String hostName;
    @SerializedName("metric_name")
    @Expose
    private String metricName;
    @SerializedName("color")
    @Expose
    private String color;
    @SerializedName("datapoints")
    @Expose
    private List<List<String>> datapoints = new ArrayList<List<String>>();


    /**
     *
     * @return
     * The dsName
     */
    public String getDsName() {
        return dsName;
    }

    /**
     *
     * @param dsName
     * The ds_name
     */
    public void setDsName(String dsName) {
        this.dsName = dsName;
    }

    /**
     *
     * @return
     * The clusterName
     */
    public String getClusterName() {
        return clusterName;
    }

    /**
     *
     * @param clusterName
     * The cluster_name
     */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    /**
     *
     * @return
     * The graphType
     */
    public String getGraphType() {
        return graphType;
    }

    /**
     *
     * @param graphType
     * The graph_type
     */
    public void setGraphType(String graphType) {
        this.graphType = graphType;
    }

    /**
     *
     * @return
     * The hostName
     */
    public String getHostName() {
        return hostName;
    }

    /**
     *
     * @param hostName
     * The host_name
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     *
     * @return
     * The metricName
     */
    public String getMetricName() {
        return metricName;
    }

    /**
     *
     * @param metricName
     * The metric_name
     */
    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    /**
     *
     * @return
     * The color
     */
    public String getColor() {
        return color;
    }

    /**
     *
     * @param color
     * The color
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     *
     * @return
     * The datapoints
     */
    public ArrayList<MetricDatapoint> getDatapoints() {

        ArrayList<MetricDatapoint> dataP = new ArrayList<MetricDatapoint>();
        for (List<String> data : datapoints){
            dataP.add(new MetricDatapoint(data.get(0), data.get(1)));
        }
        return dataP;
    }



    @Override
    public String toString(){
        return "{"
                + "\n\t" + clusterName
                + "\n\t" + hostName
                + "\n\t" + metricName
                + "\n}";
    }

}

