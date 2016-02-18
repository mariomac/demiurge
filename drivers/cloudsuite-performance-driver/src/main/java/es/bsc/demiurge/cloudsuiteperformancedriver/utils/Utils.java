package es.bsc.demiurge.cloudsuiteperformancedriver.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Utils {

    // Suppress default constructor for noninstantiability
    private Utils() {
        throw new AssertionError();
    }

    public static String readFile(String filePath) {
        URL url = Resources.getResource(filePath);
        try {
            return Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error while reading ile : " + filePath);
        }
    }

    public static String sendCommandToJschSession(String command, Session session)  {
        StringBuilder outputBuffer = new StringBuilder();
        try {
            Channel channel = session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
            InputStream commandOutput = channel.getInputStream();
            channel.connect();
            int readByte = commandOutput.read();

            while(readByte != 0xffffffff) {
                outputBuffer.append((char)readByte);
                readByte = commandOutput.read();
            }
            channel.disconnect();
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return outputBuffer.toString();
    }

}
