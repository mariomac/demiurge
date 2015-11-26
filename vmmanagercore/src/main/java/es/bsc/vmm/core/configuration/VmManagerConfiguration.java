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

package es.bsc.vmm.core.configuration;

import es.bsc.vmm.core.cloudmiddleware.CloudMiddleware;
import es.bsc.vmm.core.drivers.Estimator;
import es.bsc.vmm.core.drivers.VmmListener;
import es.bsc.vmm.core.manager.DeploymentEngine;
import es.bsc.vmm.core.drivers.Monitoring;
import es.bsc.vmm.core.manager.VmManager;
import es.bsc.vmm.core.monitoring.hosts.HostFactory;
import es.bsc.vmm.core.scheduler.SchedulingAlgorithmsRepository;
import es.bsc.vmm.core.vmplacement.CloplaConversor;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Singleton class that contains all the configuration parameters.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es), Mario Macias (http://github.com/mariomac)
 *
 */
public enum VmManagerConfiguration {
    INSTANCE;

	// Configuration file
	private static final String PROPNAME_CONF_FILE = "config";

    private static final String DEFAULT_CONF_FILE_LOCATION = "/etc/ascetic/vmm/vmmconfig.properties";
    private static final String DEFAULT_DB_NAME = "VmManagerDb";
    private static final String DEFAULT_BEANS_LOCATION = "/Beans.xml";

    // TODO: remove ATTRIBUTES and access only through apache configuration
    public String dbName;

    // OpenStack configuration
    public boolean deployVmWithVolume;

    // Testing configuration
    public String testingImageId;
    public String testingImageUrl;
    public String testingImageName;
    public String testingDeploymentBaseUrl;

    // Servers host names
    public String[] hosts;

    // Deploy
    public String deployBaseUrl;
    public String deployPackage;

    // VM deployments
    public DeploymentEngine deploymentEngine;

    private Monitoring monitoring;
    private CloudMiddleware cloudMiddleware;
    private Set<Estimator> estimators;

    // Zabbix config
    public String zabbixDbIp;
    public String zabbixDbUser;
    public String zabbixDbPassword;

	private SchedulingAlgorithmsRepository schedulingAlgorithmsRepository;
    private Configuration configuration;
	private List<VmmListener> vmmListeners;
	private HostFactory hostFactory;
	private VmManager vmManager;
    private CloplaConversor cloplaConversor;

    VmManagerConfiguration() {
        configuration = getPropertiesObjectFromConfigFile();
        initializeClassAttributes();
        loadBeansConfig();
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
		Logger log = LogManager.getLogger(VmManagerConfiguration.class);
        try {
			String customFileLocation = System.getProperty(PROPNAME_CONF_FILE,DEFAULT_CONF_FILE_LOCATION);

            log.debug("Loading configuration file: " + customFileLocation);
            return new PropertiesConfiguration(customFileLocation);
        } catch (ConfigurationException e) {
			log.error("Error loading properties file", e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Initializes all the configuration parameters.
     *
     */
    private void initializeClassAttributes() {
        Logger logger = LogManager.getLogger(VmManagerConfiguration.class);
        dbName = configuration.getString("dbName", DEFAULT_DB_NAME);

        testingImageId = configuration.getString("testingImageId");
        testingImageUrl = configuration.getString("testingImageUrl");
        testingImageName = configuration.getString("testingImageName");
        testingDeploymentBaseUrl = configuration.getString("testingDeploymentBaseUrl");
        deployBaseUrl = configuration.getString("deployBaseUrl");
        deployPackage = configuration.getString("deployPackage");
        hosts = configuration.getStringArray("hosts");
        try {
            deploymentEngine = DeploymentEngine.fromName(configuration.getString("deploymentEngine"));
        } catch(Exception e) {
            logger.error("Deployment Engine null or unknown. Assuming LEGACY: " + e.getMessage());
            deploymentEngine = DeploymentEngine.LEGACY;
        }
        zabbixDbIp = configuration.getString("zabbixDbIp");
        zabbixDbUser = configuration.getString("zabbixDbUser");
        zabbixDbPassword = configuration.getString("zabbixDbPassword");
        deployVmWithVolume = configuration.getBoolean("deployVmWithVolume", false);

		logger.debug("Loading configuration: " + toString());
    }

    public void loadBeansConfig() {
        ApplicationContext springContext = new ClassPathXmlApplicationContext(DEFAULT_BEANS_LOCATION);
		vmManager = springContext.getBean("vmManager",VmManager.class);


        cloudMiddleware = springContext.getBean("cloudMiddleware",CloudMiddleware.class);
        monitoring = springContext.getBean("monitoring",Monitoring.class);
        estimators = springContext.getBean("estimatorsManager", Set.class);

		schedulingAlgorithmsRepository = springContext.getBean("schedulingAlgorithmsRepository",SchedulingAlgorithmsRepository.class);
		vmmListeners = springContext.getBean("vmmListeners", List.class);

		hostFactory = springContext.getBean("hostFactory", HostFactory.class);

        // by the moment, don't need to put into beans (to simplify)
        cloplaConversor = new CloplaConversor();
    }

	public SchedulingAlgorithmsRepository getSchedulingAlgorithmsRepository() {
		return schedulingAlgorithmsRepository;
	}

	public Monitoring getMonitoring() {
        return monitoring;
    }

    public CloudMiddleware getCloudMiddleware() {
        return cloudMiddleware;
    }

	public Set<Estimator> getEstimators() {
		return estimators;
	}

	public List<VmmListener> getVmmListeners() {
		return vmmListeners;
	}

    public CloplaConversor getCloplaConversor() {
        return cloplaConversor;
    }

    @Override
	public String toString() {
		return "VmManagerConfiguration{" +
                "\n\tdbName='" + dbName + '\'' +
				"\n\ttestingImageId='" + testingImageId + '\'' +
				"\n\ttestingImageUrl='" + testingImageUrl + '\'' +
				"\n\ttestingImageName='" + testingImageName + '\'' +
				"\n\ttestingDeploymentBaseUrl='" + testingDeploymentBaseUrl + '\'' +
				"\n\thosts=" + Arrays.toString(hosts) +
				"\n\tdeployBaseUrl='" + deployBaseUrl + '\'' +
				"\n\tdeployPackage='" + deployPackage + '\'' +
				"\n\tdeploymentEngine='" + deploymentEngine + '\'' +
				"\n\tzabbixDbIp='" + zabbixDbIp + '\'' +
				"\n\tzabbixDbUser='" + zabbixDbUser + '\'' +
				"\n\tzabbixDbPassword='" + zabbixDbPassword + '\'' +
                "\n\tdeployVmWithVolume='" + deployVmWithVolume + '\'' +
				"\n}";
	}

	public HostFactory getHostFactory() {
		return hostFactory;
	}

	public VmManager getVmManager() {
		return vmManager;
	}
}
