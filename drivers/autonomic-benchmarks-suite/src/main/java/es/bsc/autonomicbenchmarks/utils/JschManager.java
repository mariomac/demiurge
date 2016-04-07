package es.bsc.autonomicbenchmarks.utils;

import com.jcraft.jsch.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.InputStream;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class JschManager {

    private static Logger log = LogManager.getLogger(JschManager.class);
    private Session session;


    public JschManager(String host, String user, String pass) throws JSchException{

        JSch.setConfig("StrictHostKeyChecking", "no");
        JSch jsch = new JSch();

        session = jsch.getSession(user, host, 22);
        session.setPassword(pass);
        session.connect();
    }


    public String sendCommand(String command)  {
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

    public  static String sendCommand(String command, Session session)  {
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


    public void close()
    {
        session.disconnect();
    }

    public Session getSession() {
        return session;
    }
}
