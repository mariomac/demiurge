package es.bsc.clurge;

import es.bsc.clurge.cloudmw.CloudMiddleware;
import es.bsc.clurge.estimates.Estimator;
import es.bsc.clurge.sched.SchedulingAlgorithm;
import es.bsc.clurge.vmm.VmManagerListener;
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
import java.util.Map;

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

	private VirtualMachineManager vmManager;
	private ImageManager imageManager;
	private MonitoringManager monitoringManager;
	private PersistenceManager persistenceManager;
	private PhysicalHostManager physicalHostManager;
	private SchedulingManager schedulingManager;
	private CloudMiddleware cloudMiddleware;

	private Configuration configuration;

	private Logger log = LogManager.getLogger(Clurge.class);

	private Map<Class<? extends Estimator>, Estimator> estimators;

	private Map<Class<? extends SchedulingAlgorithm>, SchedulingAlgorithm> schedulingAlgorithmMap;

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

//		<bean id="vmManager" scope="singleton" class="es.bsc.clurge.vmm.GenericVirtualMachineManager" />
//		<bean id="imageManager" scope="singleton" class="..."/>
//		<bean id="monitoringManager" scope="singleton" class="..."/>
//		<bean id="persistenceManager" scope="singleton" class="..."/>
//		<bean id="physicalHostManager" scope="singleton" class="..."/>
//		<bean id="cloudMiddleware" scope="singleton" class="..."/>
//		<bean id="schedulingManager" scope="singleton" class="..."/>

		monitoringManager = springContext.getBean("monitoringManager",MonitoringManager.class);
		persistenceManager = springContext.getBean("persistenceManager",PersistenceManager.class);
		imageManager = springContext.getBean("imageManager",ImageManager.class);
		physicalHostManager = springContext.getBean("physicalHostManager", PhysicalHostManager.class);
		vmManager = springContext.getBean("vmManager",VirtualMachineManager.class);

		@SuppressWarnings(value = "unchecked")
		List listeners = springContext.getBean("vmManagerListeners",List.class);
		if(listeners != null) {
			for(Object o : listeners) {
				vmManager.addListener((VmManagerListener) o);
			}
		}


		cloudMiddleware = springContext.getBean("deploymentMiddleware",CloudMiddleware.class);

		estimators = springContext.getBean("estimators",Map.class);

		// todo: remove this
		cloplaEnergyEstimator = springContext.getBean("cloplaEnergyEstimator",CloplaEstimator.class);
		cloplaPriceEstimator = springContext.getBean("cloplaPriceEstimator",CloplaEstimator.class);

		schedulingAlgorithmMap = springContext.getBean("schedulingAlgorithmMap", Map.class);


	}

	public Configuration getConfiguration() { return configuration; }

	public VirtualMachineManager getVirtualMachineManager() {
		return vmManager;
	}

	public ImageManager getImageManager() {
		return imageManager;
	}

	public MonitoringManager getMonitoringManager() {
		return monitoringManager;
	}

	public PhysicalHostManager getPhysicalHostManager() {
		return physicalHostManager;
	}

	public SchedulingManager getSchedulingManager() {
		return schedulingManager;
	}

	public PersistenceManager getPersistenceManager() {
		return persistenceManager;
	}

	public CloudMiddleware getCloudMiddleware() {
		return cloudMiddleware;
	}

	public Map<Class<? extends Estimator>, Estimator> getEstimators() {
		return estimators;
	}

	public Estimator getEstimator(Class<? extends Estimator> clazz) {
		return estimators.get(clazz);
	}

}
