package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.contextualizers;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import es.bsc.demiurge.cloudsuiteperformancedriver.utils.Utils;

public class MediaStreamingContextualizer {

    private static final String RUN_XML_PATH =
            "/home/ubuntu/media_streaming_client/streaming-release/faban-streaming/streaming/deploy/run.xml";

    public static void startMediaStreamingClient(String ipClientVm, String ipServerVm) {
        JSch.setConfig("StrictHostKeyChecking", "no");
        JSch jsch = new JSch();
        Session session;
        try {
            session = jsch.getSession("ubuntu", ipClientVm, 22);
            session.setPassword("ubuntu");
            session.connect();

            // Set 5000 clients
            Utils.sendCommandToJschSession("sed -i.bak -e '29d' " + RUN_XML_PATH, session);
            Utils.sendCommandToJschSession(
                    "sed -i.bak '28 a\\<fa:scale>5000</fa:scale>' " + RUN_XML_PATH, session);

            // Configure server IP in the client
            Utils.sendCommandToJschSession("sed -i.bak -e '133d' " + RUN_XML_PATH, session);
            Utils.sendCommandToJschSession(
                    "sed -i.bak '132 a\\<ipAddress>" + ipServerVm
                            + "</ipAddress>' " + RUN_XML_PATH, session);

            // Make sure that no other tests are running
            Utils.sendCommandToJschSession("sudo pkill java", session);
            Utils.sendCommandToJschSession("sudo pkill rtspclient", session);

            Thread.sleep(10 * 1000); // Not sure if it is necessary

            // Execute test
            Utils.sendCommandToJschSession("/home/ubuntu/media_streaming_client/streaming-release/" +
                    "faban-streaming/streaming/scripts/run-test.sh", session);

            session.disconnect();
        } catch(Exception e) {
            throw new RuntimeException("Error when sending the run command to the client VM");
        }
    }

}
