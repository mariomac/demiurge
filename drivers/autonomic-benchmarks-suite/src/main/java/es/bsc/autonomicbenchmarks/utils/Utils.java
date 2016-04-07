package es.bsc.autonomicbenchmarks.utils;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import es.bsc.autonomicbenchmarks.benchmarks.*;
import es.bsc.autonomicbenchmarks.configuration.BenchmarkIDs;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static java.nio.file.Files.setPosixFilePermissions;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class Utils {
    private static Logger log = LogManager.getLogger(Utils.class);
    public static boolean createDirectory(String directoryName) {

        File theDir = new File(directoryName);
        // if the directory does not exist, create it
        if (!theDir.exists()) {

            try {
                theDir.mkdirs();
                return true;
            } catch (SecurityException se) {
                throw new RuntimeException("Error in creating directory: " + directoryName + "\n"+ se.getMessage());
            }

        }
        else
            return true;
    }



    public static void cleanIptables(List<String> IPs, List<String> servers, String user, String pass){
        String cmd;
        for (String server : servers) {
            for (String ip : IPs) {
                cmd = "sudo ip addr del " + ip + "/32 dev br100";
                CommandExecutor.executeCommand("sshpass -p " + pass + " ssh -o StrictHostKeyChecking=no " + user +"@" + server + cmd);

            }

        }
    }

    public static Session getSession(String IP){
        JSch.setConfig("StrictHostKeyChecking", "no");
        JSch jsch = new JSch();
        Session session;
        try {
            session = jsch.getSession("ubuntu", IP, 22);
            session.setPassword("ubuntu");
            session.connect();
        }catch (JSchException ex1) {
            throw new RuntimeException("Error in connecting to VM: "+ ex1.getMessage());
        }
        return session;
    }

    public static Properties getProperties(String confFile) {

        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream(System.getProperty("user.dir").concat("/" + confFile));

            // load a properties file
            prop.load(input);
            return prop;

        } catch (IOException ex) {
            log.info("Configuration file does not exists: creating one with default values");

            File fileObject = new File(System.getProperty("user.dir").concat("/" + confFile));
            try {
                createDefaultConfigFile(fileObject);

                input = new FileInputStream(fileObject);

                // load a properties file
                prop.load(input);
                return prop;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {

            throw new RuntimeException("File " + confFile + " does not exists");
        } finally {
            if (input != null) {
                try {
                    input.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;

    }

    public static ArrayList<Integer> convertStringToIntArray(String array){
        List<String> numbers = Arrays.asList(array.split(","));

        ArrayList<Integer> numbersInt = new ArrayList<>();
        for (String number : numbers) {
            numbersInt.add(Integer.valueOf(number));
        }
        return numbersInt;
    }

    public static String getCpCommand(String remotePath, String localPath) {
        //log.info("cp " + remotePath + " " + localPath);
        return "cp " + remotePath + " " + localPath;

    }

    public static String getScpCommand(String remotePath, String localPath, String user, String pass) {
        log.info("sshpass -p " + pass +" scp -o StrictHostKeyChecking=no " + user + "@" + remotePath + " " + localPath);
        return "sshpass -p " + pass +" scp -o StrictHostKeyChecking=no " + user + "@" + remotePath + " " + localPath;
    }

    private static void createDefaultConfigFile(File fileObject) throws Exception {
        log.info("File " + fileObject.getAbsolutePath() + " didn't exist. Creating one with default values...");

        //Create parent directories.
        log.debug("Creating parent directories.");
        new File(fileObject.getParent()).mkdirs();

        //Create an empty file to copy the contents of the default file.
        log.debug("Creating empty file.");
        new File(fileObject.getAbsolutePath()).createNewFile();

        //Copy file.
        log.info("Copying file " + fileObject.getName());
        InputStream streamIn = Utils.class.getResourceAsStream("/" + fileObject.getName());
        FileOutputStream streamOut = new FileOutputStream(fileObject.getAbsolutePath());
        byte[] buf = new byte[8192];
        while (true) {
            int length = streamIn.read(buf);
            if (length < 0) {
                break;
            }
            streamOut.write(buf, 0, length);
        }

        //Close streams after copying.
        try {
            streamIn.close();
        } catch (IOException ex) {
            log.error("Couldn't close input stream");
            log.error(ex.getMessage());
        }
        try {
            streamOut.close();
        } catch (IOException ex) {
            log.error("Couldn't close file output stream");
            log.error(ex.getMessage());
        }

        setPosixFilePermissions(fileObject.toPath(), PosixFilePermissions.fromString("rwxr-xr-x"));
    }

    public static GenericBenchmark getBenchmark(String benchmark, int cpus, int ramGb, int diskGb, int runningTime) {

        switch (benchmark) {
            case BenchmarkIDs.dataServing:
                return new DataServing(cpus, ramGb, diskGb, runningTime, BenchmarkIDs.dataServing);
            case BenchmarkIDs.softwareTesting:
                return new SoftwareTesting(cpus, ramGb, diskGb, runningTime, BenchmarkIDs.softwareTesting);
            case BenchmarkIDs.dataCaching:
                return new DataCaching(cpus, ramGb, diskGb, runningTime, BenchmarkIDs.dataCaching);
            case BenchmarkIDs.dataAnalytics:
                return new DataAnalytics(cpus, ramGb, diskGb, runningTime, BenchmarkIDs.dataAnalytics);
            case BenchmarkIDs.graphAnalytics:
                return new GraphAnalytics(cpus, ramGb, diskGb, runningTime, BenchmarkIDs.graphAnalytics);
            default:
                throw new RuntimeException("The benchmark name specified is not correct.");
        }
    }

}
