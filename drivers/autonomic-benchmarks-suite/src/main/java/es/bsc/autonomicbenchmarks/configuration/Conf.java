package es.bsc.autonomicbenchmarks.configuration;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URL;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public enum Conf {

    INSTANCE;


    private String configurationFileName = null;
    private static Logger log = LogManager.getLogger(Conf.class);

    // Configuration file
    private static final String DEFAULT_CONF_CLASSPATH_LOCATION = "/config_cloudsuite.properties";
    private static final String PROPNAME_CONF_FILE = "config_cloudsuite";
    private static final String DEFAULT_CONF_FILE_LOCATION = "config_cloudsuite.properties";




    public String instancesPath;
    public String resultsPath;
    // VM
    public String vmmURL;
    private Configuration configuration;

    Conf() {
        configuration = getPropertiesObjectFromConfigFile();
        initializeClassAttributes();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Returns a properties file that contains the configuration parameters for the VM Manager.
     *
     * @return the properties file
     */


    private Configuration getPropertiesObjectFromConfigFile() {
        Logger log = LogManager.getLogger(Conf.class);
        try {

            Configuration embeddedConfig = null;
            URL embeddedConfigURL = Configuration.class.getResource(DEFAULT_CONF_CLASSPATH_LOCATION);
            if(embeddedConfigURL != null) {
                try {
                    embeddedConfig = new PropertiesConfiguration(embeddedConfigURL);
                } catch (ConfigurationException e) {
                    log.warn("Error processing embedded config file", e);
                }
            }

            String defaultFileName = DEFAULT_CONF_FILE_LOCATION;
            if(new File(DEFAULT_CONF_FILE_LOCATION).exists()) {
                defaultFileName = DEFAULT_CONF_FILE_LOCATION;
            }else{

            }
            configurationFileName = System.getProperty(PROPNAME_CONF_FILE, defaultFileName);
            log.debug("Loading cloudsuite configuration file: " + configurationFileName);

            Configuration fileConfig = null;
            if(new File(configurationFileName).exists()) {
                fileConfig = new PropertiesConfiguration(configurationFileName);
            }
            if(embeddedConfig == null) {
                if(fileConfig == null) {
                    throw new IllegalStateException("No configuration found at " + configurationFileName);
                }
                return fileConfig;
            } else {
                CompositeConfiguration compositeConfiguration = new CompositeConfiguration();
                if(fileConfig != null) {
                    compositeConfiguration.addConfiguration(fileConfig);
                }
                compositeConfiguration.addConfiguration(embeddedConfig);
                return compositeConfiguration;
            }

        } catch (ConfigurationException e) {
            log.error("Error loading autonomic-benchmarks-suite properties file", e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Initializes all the configuration parameters.
     *
     */
    private void initializeClassAttributes() {
        vmmURL = configuration.getString("vmmURL");
        instancesPath = configuration.getString("instancesPath");
        resultsPath = configuration.getString("resultsPath");

    }


}




