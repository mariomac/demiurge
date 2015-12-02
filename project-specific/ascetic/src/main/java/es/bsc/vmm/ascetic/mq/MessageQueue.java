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

package es.bsc.vmm.ascetic.mq;

import com.google.gson.Gson;
import es.bsc.vmm.core.drivers.VmAction;
import es.bsc.vmm.core.models.vms.VmDeployed;
import org.apache.log4j.LogManager;


public class MessageQueue {

    // Suppress default constructor for non-instantiability
    // Let's leave it like this for now, although it is probably a better strategy to instantiate it with
    // the IP of the queue
    private MessageQueue() {
        throw new AssertionError();
    }

    private static final Gson gson = new Gson();
    private static final ActiveMqAdapter activeMqAdapter = new ActiveMqAdapter();

    public static void publishMessageVmDeployed(VmDeployed vm) {
        publishMessage("virtual-machine-manager.vm." + vm.getId() + ".deployed", vm);
    }

    public static void publishMessageVmDestroyed(VmDeployed vm) {
        publishMessage("virtual-machine-manager.vm." + vm.getId() + ".destroyed", vm);
    }

    public static void publishMessageVmChangedState(VmDeployed vm, VmAction action) {
        publishMessage("virtual-machine-manager.vm." + vm.getId() + "." + action.getCamelCase(), vm);
    }

    private static void publishMessage(String topic, Object messageObject) {
        String json = gson.toJson(messageObject);
        LogManager.getLogger(MessageQueue.class).debug(topic+"\n"+json);
        activeMqAdapter.publishMessage(topic, json);
    }

}
