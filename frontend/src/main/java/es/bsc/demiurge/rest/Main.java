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

package es.bsc.demiurge.rest;

import es.bsc.demiurge.core.configuration.VmmConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.webapp.WebAppContext;


import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * 
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class Main {

    public static void main(String[] args) throws Exception {
        VmmConfig.INSTANCE.loadBeansConfig();

		int port = 80;
		if(VmmConfig.INSTANCE.deployBaseUrl == null) {
			URL url = new URL(VmmConfig.INSTANCE.deployBaseUrl);
			if(url.getPort() > 0) port = url.getPort();
		}
		Server server = new Server(port);

		ResourceHandler rh = new ResourceHandler();
		rh.setResourceBase(Main.class.getResource("/").toString());
		rh.setWelcomeFiles(new String[] { "index.html" });

		WebAppContext ctx = new WebAppContext();
		ctx.setContextPath("/");
		ctx.setWar();
		ctx.setWelcomeFiles();

		server.setHandler(ctx);
		server.start();
		server.join();
    }

}