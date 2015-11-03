package es.bsc.clurge.core;

import es.bsc.clurge.cloudmw.CloudMiddleware;
import es.bsc.clurge.db.PersistenceManager;
import es.bsc.clurge.sched.DeploymentScheduler;
import es.bsc.clurge.vmm.VmManager;
import es.bsc.clurge.core.config.VmManagerConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;

/**
 * Main Clurge class
 * provides access to all the components as well as to the configuration
 */
public enum Clurge {
	INSTANCE;

    private static final String CLURGE_BEANS_SYSPROPERTY_NAME = "beans.file";
	private static final String CLURGE_DEFAULT_FILE = "clurge-beans.xml";

	private DeploymentScheduler deploymentScheduler;
	private PersistenceManager persistenceManager;
	private VmManager vmManager;
	private CloudMiddleware cloudMiddleware;

	private VmManagerConfiguration configuration;

    /**
     * Preference for looking for clurge-beans.xml
     *
     * 1. The file path specified of a system property called: beans.file
     * 2. A file called clurge-beans.xml in the system path where the application is running
     * 3. The file called clurge-beans.xml in the root of the classpath
     */
    public void init() throws RuntimeException {
		// todo: quit this as a singleton
		configuration = VmManagerConfiguration.getInstance();

		System.setProperty("dbName",configuration.dbName);

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

    }

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

	public VmManagerConfiguration getConfiguration() {
		return configuration;
	}
}
