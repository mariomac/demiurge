package es.bsc.clurge.core;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Main Clurge class
 * provides access to all the components as well as to the configuration
 */
public class Clurge {

    private static final String CLURGE_BEANS_SYSPROPERTY_NAME = "beans.file";
    /**
     * Preference for looking for clurge-beans.xml
     *
     * 1. The file path specified of a system property called: beans.file
     * 2. A file called clurge-beans.xml in the system path where the application is running
     * 3. The file called clurge-beans.xml in the root of the classpath
     */
    public void init() {

        // http://crunchify.com/simplest-spring-mvc-hello-world-example-tutorial-spring-model-view-controller-tips/

        ApplicationContext springContext;
        if(System.getProperty(CLURGE_BEANS_SYSPROPERTY_NAME) != null
                && !System.getProperty(CLURGE_BEANS_SYSPROPERTY_NAME).trim().equals("")) {
            springContext = new FileSystemXmlApplicationContext(System.getProperty(CLURGE_BEANS_SYSPROPERTY_NAME));
        }
    }
}
