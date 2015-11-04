package es.bsc.clurge;

import es.bsc.clurge.common.cloudmw.CloudMiddleware;
import es.bsc.clurge.common.db.PersistenceManager;
import es.bsc.clurge.common.monit.HostsMonitoringManager;
import es.bsc.clurge.common.sched.DeploymentScheduler;
import es.bsc.clurge.common.vmm.VmManager;
import es.bsc.clurge.common.vmm.VmManagerListener;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;
import java.util.List;

/**
 * Main Clurge class
 * provides access to all the components as well as to the configuration
 */
public enum Clurge {
	INSTANCE;

	// Configuration default constants
	private static final String PROPNAME_CONF_FILE = "config";

	private static final String DEFAULT_CONF_FILE_LOCATION = "/etc/ascetic/vmm/vmmconfig.properties";

    private static final String CLURGE_BEANS_SYSPROPERTY_NAME = "beans.file";
	private static final String CLURGE_DEFAULT_FILE = "clurge-beans.xml";

	private DeploymentScheduler deploymentScheduler;
	private PersistenceManager persistenceManager;
	private VmManager vmManager;
	private CloudMiddleware cloudMiddleware;
	private HostsMonitoringManager monitoringManager;

	private Configuration configuration;

	private Logger log = LogManager.getLogger(Clurge.class);

	Clurge() {
		init();
	}

	private Configuration getPropertiesObjectFromConfigFile() throws ConfigurationException {
		String customFileLocation = System.getProperty(PROPNAME_CONF_FILE,DEFAULT_CONF_FILE_LOCATION);

		log.debug("Loading configuration file: " + customFileLocation);
		return new PropertiesConfiguration(customFileLocation);
	}

    /**
     * Preference for looking for clurge-beans.xml
     *
     * 1. The file path specified of a system property called: beans.file
     * 2. A file called clurge-beans.xml in the system path where the application is running
     * 3. The file called clurge-beans.xml in the root of the classpath
     */
    public void init() throws RuntimeException {

		try {
			configuration = getPropertiesObjectFromConfigFile();
		} catch(ConfigurationException e) {
			throw new RuntimeException("Cannot load Configuration: " +e.getMessage(),e);
		}

		// http://crunchify.com/simplest-spring-mvc-hello-world-example-tutorial-spring-model-view-controller-tips/
		ApplicationContext springContext;
        if(System.getProperty(CLURGE_BEANS_SYSPROPERTY_NAME) != null
                && !System.getProperty(CLURGE_BEANS_SYSPROPERTY_NAME).trim().equals("")) {
            springContext = new FileSystemXmlApplicationContext(System.getProperty(CLURGE_BEANS_SYSPROPERTY_NAME));
        } else {
			File file = new File(CLURGE_DEFAULT_FILE);
			if(file.exists()) {
				springContext = new FileSystemXmlApplicationContext(CLURGE_DEFAULT_FILE);
			} else {
				springContext = new ClassPathXmlApplicationContext('/'+CLURGE_DEFAULT_FILE);
			}
		}

		persistenceManager = springContext.getBean("persistenceManager",PersistenceManager.class);
		vmManager = springContext.getBean("vmManager",VmManager.class);

		@SuppressWarnings(value = "unchecked")
		List listeners = springContext.getBean("vmManagerListeners",List.class);
		if(listeners != null) {
			for(Object o : listeners) {
				vmManager.addListener((VmManagerListener) o);
			}
		}


		cloudMiddleware = springContext.getBean("deploymentMiddleware",CloudMiddleware.class);





    }

	public Configuration getConfiguration() { return configuration; }

	public DeploymentScheduler getDeploymentScheduler() {
		return deploymentScheduler;
	}

	public PersistenceManager getPersistenceManager() {
		return persistenceManager;
	}

	public VmManager getVmManager() {
		return vmManager;
	}

	public CloudMiddleware getCloudMiddleware() {
		return cloudMiddleware;
	}

	public HostsMonitoringManager getMonitoringManager() {
		return monitoringManager;
	}
}
