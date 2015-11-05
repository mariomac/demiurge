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

package es.bsc.clurge.monit;

import es.bsc.clurge.domain.PhysicalHost;

import static es.bsc.clurge.monit.HostPowerButtonAction.TURN_OFF;
import static es.bsc.clurge.monit.HostPowerButtonAction.TURN_ON;

/**
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class HostButtonPresserRunnable implements Runnable {

    private final PhysicalHost host;
    private final HostPowerButtonAction action;

    public HostButtonPresserRunnable(PhysicalHost host, HostPowerButtonAction action) {
        this.host = host;
        this.action = action;
    }
    
    @Override
    public void run() {
        switch (action) {
            case TURN_ON:
                turnOnServer();
                break;
            case TURN_OFF:
                turnOffServer();
                break;
            default:
                break;
        }
    }
    
    private void turnOnServer() {
        try {
            Thread.sleep(host.getTurnOnDelaySeconds()*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        host.turnOn();
    }
    
    private void turnOffServer() {
        try {
            Thread.sleep(host.getTurnOffDelaySeconds()*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        host.turnOff();
    }
    
}
