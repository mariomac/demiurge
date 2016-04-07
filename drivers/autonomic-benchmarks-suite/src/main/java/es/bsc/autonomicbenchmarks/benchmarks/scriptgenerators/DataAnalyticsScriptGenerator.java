package es.bsc.autonomicbenchmarks.benchmarks.scriptgenerators;


public class DataAnalyticsScriptGenerator {

    private static final String END_OF_LINE = System.getProperty("line.separator");
    private static final String HADOOP_HOME = "/home/ubuntu/data_analytics/analytics-release/hadoop-0.20.2";
    private static final String JAVA_HOME = "/usr/lib/jvm/java-6-openjdk-amd64";
    private static final String MAHOUT_HOME = "/home/ubuntu/data_analytics/analytics-release/mahout-distribution-0.6";
    private static final String MAPRED_XML_PATH = 
            "/home/ubuntu/data_analytics/analytics-release/hadoop-0.20.2/conf/mapred-site.xml";
    private static final String HADOOP_ENV_SH_PATH = 
            "/home/ubuntu/data_analytics/analytics-release/hadoop-0.20.2/conf/hadoop-env.sh";
    private static final int HADOOP_HEAPSIZE = 1024;

    public String generateScript(int ramGb) {
        return "#cloud-config" + END_OF_LINE
                + "password: bsc" + END_OF_LINE
                + "chpasswd: { expire: False }" + END_OF_LINE
                + "ssh_pwauth: True" + END_OF_LINE
                + "runcmd:" + END_OF_LINE
                + getAddKeyToAuthorizedKeys() + END_OF_LINE
                + getExportEnvVarsCommand() + END_OF_LINE
                + getAddFinalsToMapredXmlCommand() + END_OF_LINE
                + getModifyMapredXmlCommand(ramGb) + END_OF_LINE
                + getModifyHadoopEnvCommand() + END_OF_LINE
                + getRemoveKnownHostsCommand() + END_OF_LINE
                + getModifySshConfigCommand() + END_OF_LINE
                + getHadoopStopAllCommand() + END_OF_LINE
                + getHadoopStartAllCommand() + END_OF_LINE
                + " - sleep 10" + END_OF_LINE
                + " - " + HADOOP_HOME + "/bin/hadoop dfsadmin -safemode leave" + END_OF_LINE
                + " - sleep 30" + END_OF_LINE
                + generatePrintTimestampStartCommand() + END_OF_LINE
                + getRunMahoutModelValidationCommand() + END_OF_LINE
                + END_OF_LINE;
    }

    private String getAddKeyToAuthorizedKeys() {
        return " - cat /root/.ssh/id_rsa.pub >> /root/.ssh/authorized_keys" + END_OF_LINE;
    }
    
    private String getExportEnvVarsCommand() {
        return " - [ export, HADOOP_HOME=" + HADOOP_HOME + " ]" + END_OF_LINE 
                + " - [ export, JAVA_HOME=" + JAVA_HOME + " ]" + END_OF_LINE
                + " - [ export, MAHOUT_HOME=" + MAHOUT_HOME + " ]";
    }
    
    private String getAddFinalsToMapredXmlCommand() {
        return " - sed -i.bak, '34 a\\<final>true</final>' " + MAPRED_XML_PATH + " " + END_OF_LINE
                + " - sed -i.bak, '43 a\\<final>true</final>' " + MAPRED_XML_PATH + " " + END_OF_LINE
                + " - sed -i.bak, '49 a\\<final>true</final>' " + MAPRED_XML_PATH + " " + END_OF_LINE
                + " - sed -i.bak, '55 a\\<final>true</final>' " + MAPRED_XML_PATH + " " + END_OF_LINE
                + " - sed -i.bak, '61 a\\<final>true</final>' " + MAPRED_XML_PATH + " " + END_OF_LINE
                + " - sed -i.bak, '79 a\\<final>true</final>' " + MAPRED_XML_PATH + " " + END_OF_LINE
                + " - sed -i.bak, '87 a\\<final>true</final>' " + MAPRED_XML_PATH + " " + END_OF_LINE;
    }
    
    private String getModifyMapredXmlCommand(int ramGb) {
        int maxMappers = calculateMaxMappers(ramGb);
        int maxReducers = calculateMaxReducers(ramGb);
        return getModifyMapredXmlMaxMappers(maxMappers) + END_OF_LINE
                + getModifyMapredXmlMaxReducers(maxReducers) + END_OF_LINE
                + getModifyMapredXmlMappers(maxMappers) + END_OF_LINE
                + getModifyMapredXmlReducers(maxReducers) + END_OF_LINE
                + getModifyJavaHeapMapredXml() + END_OF_LINE
                + getModifyReduceParallelCopies() + END_OF_LINE
                + getModifyTrackerHandlerCount();
    }
    
    private int calculateMaxMappers(int ramGb) {
        return Math.max(1, (ramGb - 4)/4);
    }
    
    private int calculateMaxReducers(int ramGb) {
        return Math.max(1, (ramGb - 4)/4);
    }
    
    private String getModifyHadoopEnvCommand() {
        return " - [ sed, -i.bak, -e, '16d', " + HADOOP_ENV_SH_PATH + " ]" + END_OF_LINE
                + " - [ sed, -i.bak, '15 a\\export HADOOP_HEAPSIZE=" + 
                HADOOP_HEAPSIZE + "', " + HADOOP_ENV_SH_PATH + " ] ";
    }
    
    private String getModifyMapredXmlMaxMappers(int maxMappers) {
        return " - [ sed, -i.bak, -e, '31d', " + MAPRED_XML_PATH + " ]" + END_OF_LINE
                + " - [ sed, -i.bak, '30 a\\<value>" + maxMappers + "</value>', " + MAPRED_XML_PATH + " ] ";
    }
    
    private String getModifyMapredXmlMaxReducers(int maxReducers) {
        return " - [ sed, -i.bak, -e, '40d', " + MAPRED_XML_PATH + " ]" + END_OF_LINE
                + " - [ sed, -i.bak, '39 a\\<value>" + maxReducers + "</value>', " + MAPRED_XML_PATH + " ] ";
    }

    private String getModifyMapredXmlMappers(int mappers) {
        return " - [ sed, -i.bak, -e, '76d', " + MAPRED_XML_PATH + " ]" + END_OF_LINE
                + " - [ sed, -i.bak, '75 a\\<value>" + mappers + "</value>', " + MAPRED_XML_PATH + " ] ";
    }
    
    private String getModifyMapredXmlReducers(int reducers) {
        return " - [ sed, -i.bak, -e, '85d', " + MAPRED_XML_PATH + " ]" + END_OF_LINE
                + " - [ sed, -i.bak, '84 a\\<value>" + reducers + "</value>', " + MAPRED_XML_PATH + " ] ";
    }
    
    private String getModifyJavaHeapMapredXml() {
        return " - [ sed, -i.bak, -e, '61d', " + MAPRED_XML_PATH + " ]" + END_OF_LINE
                + " - [ sed, -i.bak, '60 a\\<value>-Xmx2048M</value>', " + MAPRED_XML_PATH + " ]";
    }
    
    private String getModifyReduceParallelCopies() {
        return " - [ sed, -i.bak, -e, '49d', " + MAPRED_XML_PATH + " ]" + END_OF_LINE
                + " - [ sed, -i.bak, '48 a\\<value>1</value>', " + MAPRED_XML_PATH + " ]";
    }
    
    private String getModifyTrackerHandlerCount() {
        return " - [ sed, -i.bak, -e, '55d', " + MAPRED_XML_PATH + " ]" + END_OF_LINE
                + " - [ sed, -i.bak, '54 a\\<value>1</value>', " + MAPRED_XML_PATH + " ]";
    }

    private String getRemoveKnownHostsCommand() {
        return " - rm /root/.ssh/known_hosts ";
    }
    
    private String getModifySshConfigCommand() {
        return " - [ touch, /home/ubuntu/.ssh/config ]" + END_OF_LINE
                + " - echo Host localhost >> /etc/ssh/ssh_config " + END_OF_LINE
                + " - [ sed, -i.bak, '1 a\\    Hostname localhost', /etc/ssh/ssh_config ]" + END_OF_LINE
                + " - [ sed, -i.bak, '2 a\\    StrictHostKeyChecking no', /etc/ssh/ssh_config ]" + END_OF_LINE;
    }

    private String getHadoopStopAllCommand() {
        return " - [ " + HADOOP_HOME + "/bin/stop-all.sh]";
    }

    private String getHadoopStartAllCommand() {
        return " - [ " + HADOOP_HOME + "/bin/start-all.sh]";
    }
    
    private String getRunMahoutModelValidationCommand() {
        return " - $MAHOUT_HOME/bin/mahout testclassifier -m wikipediamodel -d wikipediainput --method mapreduce";
    }

    private String generatePrintTimestampStartCommand() {
        return " - echo \"timestamp_start:$(date +%s)\"";
    }
    
}
