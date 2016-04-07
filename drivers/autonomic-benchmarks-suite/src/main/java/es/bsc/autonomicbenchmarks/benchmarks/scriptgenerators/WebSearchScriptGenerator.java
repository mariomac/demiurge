package es.bsc.autonomicbenchmarks.benchmarks.scriptgenerators;

public class WebSearchScriptGenerator {

    private static final String NUTCH_CONFIG_FILE = "/home/bsc/nutch-test/dis_search/conf/nutch-default.xml";
    private static final String RUN_XML_FILE = "/home/bsc/faban/search/deploy/run.xml";
    private static final String END_OF_LINE = System.getProperty("line.separator");
    
    public String generateScript(int cpus) {
        return "#cloud-config" + END_OF_LINE
                + "password: bsc" + END_OF_LINE
                + "chpasswd: { expire: False }" + END_OF_LINE
                + "ssh_pwauth: True" + END_OF_LINE
                + "runcmd:" + END_OF_LINE
                + getChangeNutchConfigFileCommands(cpus) + END_OF_LINE
                + generateExportJavaHomeCommand() + END_OF_LINE
                + generateModifyRunXmlCommand() + END_OF_LINE
                + " - sleep 10" + END_OF_LINE
                + generateStartTomcatCommand() + END_OF_LINE
                + " - sleep 10" + END_OF_LINE
                + generatePrintTimestampStartCommand() + END_OF_LINE
                + generateStartNutchCommand() + END_OF_LINE
                + " - sleep 10" + END_OF_LINE
                + END_OF_LINE;
    }
    
    private String getChangeNutchConfigFileCommands(int cpus) {
        return " - [ sed, -i.bak, -e, '905d', " + NUTCH_CONFIG_FILE + " ]" + END_OF_LINE  // num handlers
                + " - [ sed, -i.bak, '904 a\\<value>" + cpus + "</value>', " + NUTCH_CONFIG_FILE + " ] " + END_OF_LINE
                + " - sed -i.bak -e '627d' " + NUTCH_CONFIG_FILE + END_OF_LINE  // fetcher threads
                + " - sed -i.bak '626 a\\<value>" + cpus + "</value>' " + NUTCH_CONFIG_FILE + END_OF_LINE
                + " - sed -i.bak -e '634d' " + NUTCH_CONFIG_FILE + END_OF_LINE // fetcher threads per host
                + " - sed -i.bak '633 a\\<value>" + cpus + "</value>' " + NUTCH_CONFIG_FILE + END_OF_LINE
                + " - sed -i.bak -e '641d' " + NUTCH_CONFIG_FILE + END_OF_LINE // fetcher threads per host by ip (true)
                + " - sed -i.bak '640 a\\<value>true</value>' " + NUTCH_CONFIG_FILE + END_OF_LINE;
    }

    private String generateExportJavaHomeCommand() {
        return " - [ export, JAVA_HOME=/usr/lib/jvm/java-6-openjdk-amd64 ]";
    }

    private String generateStartNutchCommand() {
        return " - /home/bsc/nutch-test/dis_search/bin/nutch server 8890 /home/bsc/nutch-test/local \"&> a.txt\"";
    }
    
    private String generateStartTomcatCommand() {
        return " - [ /home/bsc/search-release/apache-tomcat-7.0.23/bin/startup.sh ]";
    }
    
    private String generateModifyRunXmlCommand() {
        return " - sed -i.bak -e '25d' " + RUN_XML_FILE + END_OF_LINE  // scale
                + " - sed -i.bak '24 a\\<fa:scale>3000</fa:scale>' " + RUN_XML_FILE + END_OF_LINE
                + " - sed -i.bak -e '28d' " + RUN_XML_FILE + END_OF_LINE  // ramp up
                + " - sed -i.bak '27 a\\<fa:rampUp>300</fa:rampUp>' " + RUN_XML_FILE + END_OF_LINE
                + " - sed -i.bak -e '29d' " + RUN_XML_FILE + END_OF_LINE // steady state
                + " - sed -i.bak '28 a\\<fa:steadyState>300</fa:steadyState>' " + RUN_XML_FILE + END_OF_LINE
                + " - sed -i.bak -e '30d' " + RUN_XML_FILE + END_OF_LINE // ramp down
                + " - sed -i.bak '29 a\\<fa:rampDown>120</fa:rampDown>' " + RUN_XML_FILE + END_OF_LINE;
    }

    private String generatePrintTimestampStartCommand() {
        return " - echo \"timestamp_start:$(date +%s)\"";
    }
    
}
