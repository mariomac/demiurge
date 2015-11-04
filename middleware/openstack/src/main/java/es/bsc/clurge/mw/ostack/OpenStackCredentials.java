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

package es.bsc.clurge.mw.ostack;

import com.google.common.base.Preconditions;
import org.apache.commons.configuration.Configuration;
import static es.bsc.clurge.mw.ostack.OpenStackMiddleware.*;

/**
 * OpenStack credentials.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es), Mario Macias (http://github.com/mariomac)
 */
class OpenStackCredentials {

    private final String openStackIP;
    private final int keyStonePort;
    private final String keyStoneTenant;
    private final String keyStoneUser;
    private final String keyStonePassword;
    private final int glancePort;
    private final String keyStoneTenantId;

    public OpenStackCredentials(Configuration config) {
        this.openStackIP = config.getString(CONFIG_IP);
        this.keyStonePort = config.getInt(CONFIG_KEYSTONE_PORT);
        this.keyStoneTenant = config.getString(CONFIG_KEYSTONE_TENANT);
        this.keyStoneUser = config.getString(CONFIG_KEYSTONE_USER);
        this.keyStonePassword = config.getString(CONFIG_KEYSTONE_PASSWORD);
        this.glancePort = config.getInt(CONFIG_GLANCE_PORT);
        this.keyStoneTenantId = config.getString(CONFIG_KEYSTONE_TENANT_ID);

		validateConstructorParams(openStackIP, keyStonePort, keyStoneTenant, keyStoneUser, keyStonePassword,
				glancePort, keyStoneTenantId);
    }

    public String getOpenStackIP() {
        return openStackIP;
    }

    public int getKeyStonePort() {
        return keyStonePort;
    }

    public String getKeyStoneTenant() {
        return keyStoneTenant;
    }

    public String getKeyStoneUser() {
        return keyStoneUser;
    }

    public String getKeyStonePassword() {
        return keyStonePassword;
    }

    public int getGlancePort() {
        return glancePort;
    }

    public String getKeyStoneTenantId() {
        return keyStoneTenantId;
    }

    private void validateConstructorParams(String openStackIP, int keyStonePort, String keyStoneTenant,
                                           String keyStoneUser, String keyStonePassword, int glancePort,
                                           String keyStoneTenantId) {
        Preconditions.checkNotNull(openStackIP);
        Preconditions.checkNotNull(keyStonePort);
        Preconditions.checkNotNull(keyStoneTenant);
        Preconditions.checkNotNull(keyStoneUser);
        Preconditions.checkNotNull(keyStonePassword);
        Preconditions.checkNotNull(glancePort);
        Preconditions.checkNotNull(keyStoneTenantId);
    }

}
