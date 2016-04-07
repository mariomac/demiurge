package es.bsc.autonomicbenchmarks.benchmarks.scriptgenerators;

public class WebSearchJmeterScriptGenerator {

    private static final String NUTCH_CONFIG_FILE = "/home/bsc/nutch-test/dis_search/conf/nutch-default.xml";
    private static final String END_OF_LINE = System.getProperty("line.separator");
    
    public String generateScript(int cpus) {
        return "#cloud-config" + END_OF_LINE
                + "password: bsc" + END_OF_LINE
                + "chpasswd: { expire: False }" + END_OF_LINE
                + "ssh_pwauth: True" + END_OF_LINE
                + "runcmd:" + END_OF_LINE
                + getChangeNutchConfigFileCommands(cpus) + END_OF_LINE
                + generatePrintTimestampStartCommand() + END_OF_LINE
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

    private String generatePrintTimestampStartCommand() {
        return " - echo \"timestamp_start:$(date +%s)\"";
    }


    public String getNutchClientCOnfigFile(String ipServer, Integer numThreads, Integer duration){
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<jmeterTestPlan version=\"1.2\" properties=\"2.3\" jmeter=\"2.8.20130705\">\n" +
                "  <hashTree>\n" +
                "    <TestPlan guiclass=\"TestPlanGui\" testclass=\"TestPlan\" testname=\"Plan de Pruebas\" enabled=\"true\">\n" +
                "      <stringProp name=\"TestPlan.comments\"></stringProp>\n" +
                "      <boolProp name=\"TestPlan.functional_mode\">false</boolProp>\n" +
                "      <boolProp name=\"TestPlan.serialize_threadgroups\">false</boolProp>\n" +
                "      <elementProp name=\"TestPlan.user_defined_variables\" elementType=\"Arguments\" guiclass=\"ArgumentsPanel\" testclass=\"Arguments\" testname=\"Variables definidas por el Usuario\" enabled=\"true\">\n" +
                "        <collectionProp name=\"Arguments.arguments\"/>\n" +
                "      </elementProp>\n" +
                "      <stringProp name=\"TestPlan.user_define_classpath\"></stringProp>\n" +
                "    </TestPlan>\n" +
                "    <hashTree>\n" +
                "      <ThreadGroup guiclass=\"ThreadGroupGui\" testclass=\"ThreadGroup\" testname=\"Thread Group\" enabled=\"true\">\n" +
                "        <stringProp name=\"ThreadGroup.on_sample_error\">continue</stringProp>\n" +
                "        <elementProp name=\"ThreadGroup.main_controller\" elementType=\"LoopController\" guiclass=\"LoopControlPanel\" testclass=\"LoopController\" testname=\"Loop Controller\" enabled=\"true\">\n" +
                "          <boolProp name=\"LoopController.continue_forever\">false</boolProp>\n" +
                "          <intProp name=\"LoopController.loops\">-1</intProp>\n" +
                "        </elementProp>\n" +
                "        <stringProp name=\"ThreadGroup.num_threads\">"+ numThreads +"</stringProp>\n" +
                "        <stringProp name=\"ThreadGroup.ramp_time\">1</stringProp>\n" +
                "        <longProp name=\"ThreadGroup.start_time\">1428915647000</longProp>\n" +
                "        <longProp name=\"ThreadGroup.end_time\">1428915647000</longProp>\n" +
                "        <boolProp name=\"ThreadGroup.scheduler\">true</boolProp>\n" +
                "        <stringProp name=\"ThreadGroup.duration\">"+ duration +"</stringProp>\n" +
                "        <stringProp name=\"ThreadGroup.delay\">10</stringProp>\n" +
                "      </ThreadGroup>\n" +
                "      <hashTree>\n" +
                "        <HTTPSamplerProxy guiclass=\"HttpTestSampleGui\" testclass=\"HTTPSamplerProxy\" testname=\"HTTP Request\" enabled=\"true\">\n" +
                "          <elementProp name=\"HTTPsampler.Arguments\" elementType=\"Arguments\" guiclass=\"HTTPArgumentsPanel\" testclass=\"Arguments\" testname=\"User Defined Variables\" enabled=\"true\">\n" +
                "            <collectionProp name=\"Arguments.arguments\">\n" +
                "              <elementProp name=\"query\" elementType=\"HTTPArgument\">\n" +
                "                <boolProp name=\"HTTPArgument.always_encode\">false</boolProp>\n" +
                "                <stringProp name=\"Argument.value\">apache</stringProp>\n" +
                "                <stringProp name=\"Argument.metadata\">=</stringProp>\n" +
                "                <boolProp name=\"HTTPArgument.use_equals\">true</boolProp>\n" +
                "                <stringProp name=\"Argument.name\">query</stringProp>\n" +
                "              </elementProp>\n" +
                "            </collectionProp>\n" +
                "          </elementProp>\n" +
                "          <stringProp name=\"HTTPSampler.domain\">"+ ipServer +"</stringProp>\n" +
                "          <stringProp name=\"HTTPSampler.port\">8080</stringProp>\n" +
                "          <stringProp name=\"HTTPSampler.connect_timeout\"></stringProp>\n" +
                "          <stringProp name=\"HTTPSampler.response_timeout\"></stringProp>\n" +
                "          <stringProp name=\"HTTPSampler.protocol\"></stringProp>\n" +
                "          <stringProp name=\"HTTPSampler.contentEncoding\"></stringProp>\n" +
                "          <stringProp name=\"HTTPSampler.path\">/search.jsp</stringProp>\n" +
                "          <stringProp name=\"HTTPSampler.method\">GET</stringProp>\n" +
                "          <boolProp name=\"HTTPSampler.follow_redirects\">true</boolProp>\n" +
                "          <boolProp name=\"HTTPSampler.auto_redirects\">false</boolProp>\n" +
                "          <boolProp name=\"HTTPSampler.use_keepalive\">true</boolProp>\n" +
                "          <boolProp name=\"HTTPSampler.DO_MULTIPART_POST\">false</boolProp>\n" +
                "          <stringProp name=\"HTTPSampler.implementation\">HttpClient4</stringProp>\n" +
                "          <boolProp name=\"HTTPSampler.monitor\">false</boolProp>\n" +
                "          <stringProp name=\"HTTPSampler.embedded_url_re\"></stringProp>\n" +
                "        </HTTPSamplerProxy>\n" +
                "        <hashTree>\n" +
                "          <ResultCollector guiclass=\"ViewResultsFullVisualizer\" testclass=\"ResultCollector\" testname=\"View Results Tree\" enabled=\"true\">\n" +
                "            <boolProp name=\"ResultCollector.error_logging\">false</boolProp>\n" +
                "            <objProp>\n" +
                "              <name>saveConfig</name>\n" +
                "              <value class=\"SampleSaveConfiguration\">\n" +
                "                <time>true</time>\n" +
                "                <latency>true</latency>\n" +
                "                <timestamp>true</timestamp>\n" +
                "                <success>true</success>\n" +
                "                <label>true</label>\n" +
                "                <code>true</code>\n" +
                "                <message>true</message>\n" +
                "                <threadName>true</threadName>\n" +
                "                <dataType>true</dataType>\n" +
                "                <encoding>false</encoding>\n" +
                "                <assertions>true</assertions>\n" +
                "                <subresults>true</subresults>\n" +
                "                <responseData>false</responseData>\n" +
                "                <samplerData>false</samplerData>\n" +
                "                <xml>true</xml>\n" +
                "                <fieldNames>false</fieldNames>\n" +
                "                <responseHeaders>false</responseHeaders>\n" +
                "                <requestHeaders>false</requestHeaders>\n" +
                "                <responseDataOnError>false</responseDataOnError>\n" +
                "                <saveAssertionResultsFailureMessage>false</saveAssertionResultsFailureMessage>\n" +
                "                <assertionsResultsToSave>0</assertionsResultsToSave>\n" +
                "                <bytes>true</bytes>\n" +
                "              </value>\n" +
                "            </objProp>\n" +
                "            <stringProp name=\"filename\"></stringProp>\n" +
                "          </ResultCollector>\n" +
                "          <hashTree/>\n" +
                "          <ResultCollector guiclass=\"SummaryReport\" testclass=\"ResultCollector\" testname=\"Summary Report\" enabled=\"true\">\n" +
                "            <boolProp name=\"ResultCollector.error_logging\">false</boolProp>\n" +
                "            <objProp>\n" +
                "              <name>saveConfig</name>\n" +
                "              <value class=\"SampleSaveConfiguration\">\n" +
                "                <time>true</time>\n" +
                "                <latency>true</latency>\n" +
                "                <timestamp>true</timestamp>\n" +
                "                <success>true</success>\n" +
                "                <label>true</label>\n" +
                "                <code>true</code>\n" +
                "                <message>true</message>\n" +
                "                <threadName>true</threadName>\n" +
                "                <dataType>true</dataType>\n" +
                "                <encoding>false</encoding>\n" +
                "                <assertions>true</assertions>\n" +
                "                <subresults>true</subresults>\n" +
                "                <responseData>false</responseData>\n" +
                "                <samplerData>false</samplerData>\n" +
                "                <xml>true</xml>\n" +
                "                <fieldNames>false</fieldNames>\n" +
                "                <responseHeaders>false</responseHeaders>\n" +
                "                <requestHeaders>false</requestHeaders>\n" +
                "                <responseDataOnError>false</responseDataOnError>\n" +
                "                <saveAssertionResultsFailureMessage>false</saveAssertionResultsFailureMessage>\n" +
                "                <assertionsResultsToSave>0</assertionsResultsToSave>\n" +
                "                <bytes>true</bytes>\n" +
                "              </value>\n" +
                "            </objProp>\n" +
                "            <stringProp name=\"filename\"></stringProp>\n" +
                "          </ResultCollector>\n" +
                "          <hashTree/>\n" +
                "          <ResultCollector guiclass=\"GraphVisualizer\" testclass=\"ResultCollector\" testname=\"Graph Results\" enabled=\"true\">\n" +
                "            <boolProp name=\"ResultCollector.error_logging\">false</boolProp>\n" +
                "            <objProp>\n" +
                "              <name>saveConfig</name>\n" +
                "              <value class=\"SampleSaveConfiguration\">\n" +
                "                <time>true</time>\n" +
                "                <latency>true</latency>\n" +
                "                <timestamp>true</timestamp>\n" +
                "                <success>true</success>\n" +
                "                <label>true</label>\n" +
                "                <code>true</code>\n" +
                "                <message>true</message>\n" +
                "                <threadName>true</threadName>\n" +
                "                <dataType>true</dataType>\n" +
                "                <encoding>false</encoding>\n" +
                "                <assertions>true</assertions>\n" +
                "                <subresults>true</subresults>\n" +
                "                <responseData>false</responseData>\n" +
                "                <samplerData>false</samplerData>\n" +
                "                <xml>true</xml>\n" +
                "                <fieldNames>false</fieldNames>\n" +
                "                <responseHeaders>false</responseHeaders>\n" +
                "                <requestHeaders>false</requestHeaders>\n" +
                "                <responseDataOnError>false</responseDataOnError>\n" +
                "                <saveAssertionResultsFailureMessage>false</saveAssertionResultsFailureMessage>\n" +
                "                <assertionsResultsToSave>0</assertionsResultsToSave>\n" +
                "                <bytes>true</bytes>\n" +
                "              </value>\n" +
                "            </objProp>\n" +
                "            <stringProp name=\"filename\"></stringProp>\n" +
                "          </ResultCollector>\n" +
                "          <hashTree/>\n" +
                "        </hashTree>\n" +
                "        <Summariser guiclass=\"SummariserGui\" testclass=\"Summariser\" testname=\"Generate Summary Results\" enabled=\"true\"/>\n" +
                "        <hashTree/>\n" +
                "      </hashTree>\n" +
                "    </hashTree>\n" +
                "  </hashTree>\n" +
                "</jmeterTestPlan>";
    }


}
