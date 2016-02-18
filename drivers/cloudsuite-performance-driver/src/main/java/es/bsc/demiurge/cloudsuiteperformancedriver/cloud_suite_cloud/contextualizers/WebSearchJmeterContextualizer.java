package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.contextualizers;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import es.bsc.demiurge.cloudsuiteperformancedriver.utils.Utils;

public class WebSearchJmeterContextualizer {

    private static final String EXPORT_JAVA_COMMAND = "export JAVA_HOME=/usr/lib/jvm/java-6-openjdk-amd64";
    private static final String START_TOMCAT_COMMAND = "/home/bsc/search-release/apache-tomcat-7.0.23/bin/startup.sh";
    private static final String START_NUTCH_COMMAND =
            "nohup /home/bsc/nutch-test/dis_search/bin/nutch server 8890"
                    + " /home/bsc/nutch-test/local"
                    + " > foo.out 2> foo.err < /dev/null &";
    private static final String START_JMETER_COMMAND = "jmeter -n -t nutch.jmx -l nutch_results.jtl";
    private static final String EXECUTE_BENCHMARK_REPEATEDLY_COMMAND =
            "for i in `seq 1000`; do " + START_JMETER_COMMAND + ";sleep 15; done";

    public static void startWebSearch(String vmIp) {
        JSch.setConfig("StrictHostKeyChecking", "no");
        JSch jsch = new JSch();
        Session session;
        try {
            session = jsch.getSession("bsc", vmIp, 22);
            session.setPassword("bsc");
            session.connect();
            Utils.sendCommandToJschSession(EXPORT_JAVA_COMMAND + ";" + START_NUTCH_COMMAND, session);
            Utils.sendCommandToJschSession(EXPORT_JAVA_COMMAND + ";" + START_TOMCAT_COMMAND, session);
            session.disconnect();
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

    public static void startJmeterClient(String vmIp, String serverVmIp) {
        JSch.setConfig("StrictHostKeyChecking", "no");
        JSch jsch = new JSch();
        Session session;
        try {
            session = jsch.getSession("ubuntu", vmIp, 22);
            session.setPassword("ubuntu");
            session.connect();
            Utils.sendCommandToJschSession(jmeterModifyServerIpCommand(serverVmIp), session);
            Utils.sendCommandToJschSession(jmeterModifyRuntime(), session);
            Utils.sendCommandToJschSession(EXECUTE_BENCHMARK_REPEATEDLY_COMMAND, session);
            session.disconnect();
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

    private static String jmeterModifyRuntime() {
        return "sed -i.bak -e '25d' nutch.jmx;" +
                "sed -i.bak '24 a\\<stringProp name=\"ThreadGroup.duration\">6000</stringProp>' nutch.jmx" ;
    }

    private static String jmeterModifyServerIpCommand(String serverIp) {
        return "sed -i.bak -e '41d' nutch.jmx;" +
                "sed -i.bak '40 a\\<stringProp name=\"HTTPSampler.domain\">" + serverIp + "</stringProp>' nutch.jmx";
    }

}
