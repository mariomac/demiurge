/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.demiurge.core.utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import static java.nio.file.Files.setPosixFilePermissions;

/**
 * This helper class contains auxiliary methods to work with the file system.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es).
 *
 */
public class FileSystem {

    private static final Logger logger = LogManager.getLogger(FileSystem.class);
    // Suppress default constructor for non-instantiability
    private FileSystem() {
        throw new AssertionError();
    }

    public static void deleteFile(String filePath) {
        Path path = FileSystems.getDefault().getPath(filePath);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Error while deleting a file.");
        }
    }

    public static void writeToFile(String fname, long timestamp, String s){
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(fname, true)));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.append(timestamp + "," + s + "\n");
        writer.close();

    }

    public static void writeStringToFile(String fname, String s,  boolean append){
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(fname, append)));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.append(s + "\n");
        writer.close();

    }

    public static void writeToFile(String fname, long timestamp, int s, boolean append){
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(fname, append)));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.append(timestamp + "," + s + "\n");
        writer.close();

    }


    public static String getFilePath(String configFile) {

        String path = System.getProperty("user.dir");

        File fileObject = new File(path.concat(configFile));
        if (!fileObject.exists()) {
            try {
                createDefaultConfigFile(fileObject);
            } catch (Exception ex) {
                logger.error("Error reading " + path.concat(configFile) + " configuration file: ", ex);
            }
        }

        return path.concat(configFile);
    }

    private static void createDefaultConfigFile(File fileObject) throws Exception {
        logger.debug("File " + fileObject.getAbsolutePath() + " didn't exist. Creating one with default values...");

        //Create parent directories.
        logger.debug("Creating parent directories.");
        new File(fileObject.getParent()).mkdirs();

        //Create an empty file to copy the contents of the default file.
        logger.debug("Creating empty file.");
        new File(fileObject.getAbsolutePath()).createNewFile();

        //Copy file.
        logger.debug("Copying file " + fileObject.getName());
        InputStream streamIn = FileSystem.class.getResourceAsStream("/" + fileObject.getName());
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
            logger.error("Couldn't close input stream");
            logger.error(ex.getMessage());
        }
        try {
            streamOut.close();
        } catch (IOException ex) {
            logger.error("Couldn't close file output stream");
            logger.error(ex.getMessage());
        }

        setPosixFilePermissions(fileObject.toPath(), PosixFilePermissions.fromString("rwxr-xr-x"));
    }


}
