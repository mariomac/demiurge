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

package es.bsc.clurge.vmm.components;

import es.bsc.clurge.core.cloudmiddleware.CloudMiddleware;
import es.bsc.clurge.core.cloudmiddleware.CloudMiddlewareException;
import es.bsc.clurge.core.configuration.VmManagerConfiguration;
import es.bsc.clurge.core.db.VmManagerDb;
import es.bsc.clurge.core.logging.VMMLogger;
import es.bsc.clurge.core.message_queue.MessageQueue;
import es.bsc.clurge.core.modellers.energy.EnergyModeller;
import es.bsc.clurge.core.modellers.energy.ascetic.AsceticEnergyModellerAdapter;
import es.bsc.clurge.core.modellers.price.PricingModeller;
import es.bsc.clurge.core.modellers.price.ascetic.AsceticPricingModellerAdapter;
import es.bsc.clurge.common.models.vms.Vm;
import es.bsc.clurge.common.models.vms.VmDeployed;
import es.bsc.clurge.core.monitoring.hosts.Host;
import es.bsc.clurge.core.monitoring.zabbix.ZabbixConnector;
import es.bsc.clurge.core.scheduler.Scheduler;
import es.bsc.clurge.core.selfadaptation.AfterVmDeleteSelfAdaptationRunnable;
import es.bsc.clurge.core.selfadaptation.AfterVmsDeploymentSelfAdaptationRunnable;
import es.bsc.clurge.core.selfadaptation.SelfAdaptationManager;

/**
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VmsManager {
    


    
}
