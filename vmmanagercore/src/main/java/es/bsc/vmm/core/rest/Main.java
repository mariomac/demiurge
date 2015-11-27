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

package es.bsc.vmm.core.rest;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import es.bsc.vmm.core.configuration.VmManagerConfiguration;
import org.glassfish.grizzly.http.server.HttpServer;

import java.io.IOException;
import java.net.URI;

/**
 * 
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class Main {

    public static final String BASE_URI = VmManagerConfiguration.INSTANCE.deployBaseUrl;
    public static final String DEPLOY_PACKAGE = VmManagerConfiguration.INSTANCE.deployPackage;
    public static final String STOP_MESSAGE = "Press any key to stop the server...";

    @SuppressWarnings("unchecked")
    public static HttpServer createServer() {
        final ResourceConfig rc = new PackagesResourceConfig(DEPLOY_PACKAGE);
        rc.getContainerResponseFilters().add(CorsSupportFilter.class);

        try {
            URI baseUri = URI.create(BASE_URI);
            return GrizzlyServerFactory.createHttpServer(baseUri, rc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Main function.
     *
     * @param args arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        VmManagerConfiguration.INSTANCE.loadBeansConfig();

		final HttpServer server = createServer();
        server.start();
        System.out.println(STOP_MESSAGE);
        System.in.read();
    }
    
}