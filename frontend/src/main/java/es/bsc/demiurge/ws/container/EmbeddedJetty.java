package es.bsc.demiurge.ws.container;

import es.bsc.demiurge.core.configuration.Config;
import org.eclipse.jetty.rewrite.handler.RedirectPatternRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.IOException;

/**
 * @author Mario Macías (http://github.com/mariomac)
 */
public class EmbeddedJetty {


    private static final String GUI_MAPPING_URL = "/gui/*"; // todo: combine with "/gui/*"
    private static final String API_MAPPING_URL = "/api/*";


    public void startJetty(int port) throws Exception {
        final Server server = new Server(port);
		HandlerCollection handlerCollection = new HandlerCollection(false);
		RewriteHandler rewriteHandler = new RewriteHandler();
		RedirectPatternRule rpr = new RedirectPatternRule();
		rpr.setPattern("/gui");
		rpr.setLocation("/gui/");
		rewriteHandler.addRule(rpr);
		handlerCollection.addHandler(rewriteHandler);
		handlerCollection.addHandler(getServletContextHandler(getContext()));
        server.setHandler(handlerCollection);
        server.start();
        server.join();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Stopping jetty");
                try {
                    server.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                server.destroy();
            }
        }));
    }

    private static ServletContextHandler getServletContextHandler(WebApplicationContext context) throws IOException {
        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setErrorHandler(null);
        contextHandler.setContextPath(Config.INSTANCE.contextPath);

        DispatcherServlet dispatcherServlet = new DispatcherServlet(context);
		ServletHolder guiServletHolder = new ServletHolder(dispatcherServlet);
        contextHandler.addServlet(guiServletHolder, GUI_MAPPING_URL);

        ServletHolder apiServletContainer = new ServletHolder(
         new ServletContainer(new ApiConfig()));
        contextHandler.addServlet(apiServletContainer, API_MAPPING_URL);
        contextHandler.addEventListener(new ContextLoaderListener(context));
        return contextHandler;
    }

    private static WebApplicationContext getContext() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setConfigLocation("es.bsc.demiurge.ws");
        return context;
    }

}
