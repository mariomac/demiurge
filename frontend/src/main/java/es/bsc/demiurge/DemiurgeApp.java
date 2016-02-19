package es.bsc.demiurge;

import es.bsc.demiurge.core.configuration.Config;
import es.bsc.demiurge.ws.container.EmbeddedJetty;
import es.bsc.demiurge.ws.rest.DemiurgeRestV1;
import org.eclipse.jetty.rewrite.handler.RedirectPatternRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * @author Mario Macías (http://github.com/mariomac)
 */
public class DemiurgeApp {
    public static void main(String[] args) {
        Config.INSTANCE.loadBeansConfig();
        try {
            new EmbeddedJetty().startJetty(Config.INSTANCE.connectionPort);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*

        // Configure jersey servlet for rest services
        ServletContextHandler restContexthandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        restContexthandler.setContextPath("/api");

        ServletHolder jerseyServlet = restContexthandler.addServlet(
                org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        // Tells the Jersey Servlet which REST service/class to load.
//		jerseyServlet.setInitParameter("jersey.config.server.provider.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        jerseyServlet.setInitParameter(
                "jersey.config.server.provider.classnames",DemiurgeRestV1.class.getCanonicalName());

        // Tells the Jersey Servlet which REST service/class to load.

        // Configure static assets
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setWelcomeFiles(new String[] {"index.html"});
        resourceHandler.setResourceBase(Main.class.getClassLoader().getResource("webapp").toExternalForm());
        resourceHandler.setDirectoriesListed(true);

        ContextHandler resourceCtxHandler = new ContextHandler("/gui");
        resourceCtxHandler.setHandler(resourceHandler);

        HandlerCollection handlerCollection = new HandlerCollection();
        handlerCollection.addHandler(restContexthandler);
        handlerCollection.addHandler(resourceCtxHandler);

        // Redirect from /gui to /gui/
        RewriteHandler rewriteHandler = new RewriteHandler();
        RedirectPatternRule rpr = new RedirectPatternRule();
        rpr.setPattern("/gui");
        rpr.setLocation("/gui/");
        rewriteHandler.addRule(rpr);

        handlerCollection.addHandler(rewriteHandler);


        final Server jettyServer = new Server(Config.INSTANCE.connectionPort);
        jettyServer.setHandler(handlerCollection);

        jettyServer.start();
        jettyServer.join();
        */


    }
}
