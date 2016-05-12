package es.bsc.vmm.ascetic.mq;

import com.google.gson.Gson;
import es.bsc.demiurge.core.VmmGlobalListener;
import es.bsc.demiurge.core.configuration.Config;
import es.bsc.demiurge.core.drivers.VmAction;
import es.bsc.demiurge.core.drivers.VmmListener;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.models.vms.VmDeployed;
import java.util.ArrayList;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

/**
 * Created by mmacias on 19/11/15.
 */
public class MqEventsManager implements VmmListener, VmmGlobalListener, MessageListener {

	private ActiveMqAdapter activeMqAdapter = new ActiveMqAdapter();
	Logger log = LogManager.getLogger(MqEventsManager.class);

	@Override
	public void onVmDeployment(VmDeployed vm) {
		publishMessageVmDeployed(vm);
		if (vm.getSlaId() != null && !"".equals(vm.getSlaId().trim())) {
			try {
				activeMqAdapter.listenToQueue(
						String.format(VIOLATION_QUEUE_NAME, vm.getSlaId(), vm.getId()),
						this);
			} catch (JMSException e) {
				log.error("Cannot subscribe to message queue after vm deployment: " + e.getMessage());
			}
		}
	}

	@Override
	public void onVmDestruction(VmDeployed vm) {
		publishMessageVmDestroyed(vm);
		if(vm.getSlaId() != null) {
			activeMqAdapter.closeQueue(String.format(VIOLATION_QUEUE_NAME, vm.getSlaId(), vm.getId()));
		}
	}

	@Override
	public void onVmAction(VmDeployed vm, VmAction action) {
		publishMessageVmChangedState(vm, action);
	}

	// next methods do nothing intentionally
	@Override public void onVmMigration(VmDeployed vm) {}
	@Override public void onPreVmDeployment(Vm vm) {}
	// ----


	// Listens all queues for all the currently executed VMs
	@Override
	public void onVmmStart() {
		log.debug("Listening SLA queues for all the currently running VMs");
		for(VmDeployed vm : Config.INSTANCE.getVmManager().getAllVms()) {
			if(vm.getSlaId() != null && !"".equals(vm.getSlaId().trim())) {
				String queueId = String.format(VIOLATION_QUEUE_NAME, vm.getSlaId(), vm.getId());
				try {
					activeMqAdapter.listenToQueue(queueId, this);
				} catch(Exception e) {
					log.warn("Cannot listen to queue " + queueId +": " + e.getMessage());
				}
			}
		}
	}

	@Override
	public void onVmmStop() {
		activeMqAdapter.closeAllQueues();
	}

	private long lastSelfAdaptation = 0;
	private static final long MIN_TIME_BETWEEN_SELF_ADAPTATIONS = 5 * 60 * 1000;

	private static final long IGNORE_MESSAGES_OLDER_THAN = 30 * 1000;
     
    private String getNextXmlTag(String xml, String startTag, String endTag) {
        int start = xml.indexOf(startTag);
        int end = xml.indexOf(endTag);
        if(start == -1 || end == -1){ return null; }
        return xml.substring(start, end + endTag.length());
    }
     
    private List<JSONObject> xmlToJson(String xml, String startTag, String endTag) {
        String nextBetween = "";
        List<JSONObject> results = new ArrayList<>();
        while( (nextBetween = getNextXmlTag(xml, startTag, endTag)) != null ){
            JSONObject xmlJSONObj = org.json.XML.toJSONObject(nextBetween);
            results.add(xmlJSONObj);
            xml = xml.substring(nextBetween.length()); //moving pointer to next tag
        }
        return results;
    }
     
    private Map<String, String> getRequirementsFromSlaTerm(String slaAgreementTerm, String guaranteedValue) {
        Map<String, String> vmRequirements = new HashMap<>();
        String cpuRequirements = "";
        String diskRequirements = "";
        if(slaAgreementTerm.equals("hw_platform")){
            String hardware_requirements[] = guaranteedValue.split(";");
            if(hardware_requirements.length >= 1){
               cpuRequirements = hardware_requirements[0];
            }
            if(hardware_requirements.length >= 2){
               diskRequirements = hardware_requirements[1];
            }
        }

        if(!cpuRequirements.equals("")){
            String cpu_requirements[] = cpuRequirements.split("/");
            if(cpu_requirements.length >= 1){ vmRequirements.put("processor_architecture", cpu_requirements[0]); }
            if(cpu_requirements.length >= 2){ vmRequirements.put("processor_brand", cpu_requirements[1]); }
            if(cpu_requirements.length >= 3){ vmRequirements.put("processor_model", cpu_requirements[2]); }
        }

        if(!diskRequirements.equals("")){
            String disk_requirements[] = diskRequirements.split("/");
            if(disk_requirements.length >= 1){ vmRequirements.put("disk_type", disk_requirements[0]); }
        }

        return vmRequirements;
    }
    
    /**
     * Reads an xml with ViolationMessages and detects the different 
     * vm_requirements that will be needed for self-adaptation.
     * 
     * @param xml
     * @return 
     */
    public Map<String, Map<String, String>> readRequirementsFromViolationMessages(String xml){
        Map<String, Map<String, String>> newRequirements = new HashMap<>();
         for(JSONObject xmlJSONObj : xmlToJson(xml, "<ViolationMessage", "</ViolationMessage>")){
            String vm_id = xmlJSONObj
                    .getJSONObject("ViolationMessage")
                    .getString("vmId");
            String slaAgreementTerm = xmlJSONObj
                    .getJSONObject("ViolationMessage")
                    .getJSONObject("alert")
                    .getString("slaAgreementTerm");
            String guaranteedValue = xmlJSONObj
                    .getJSONObject("ViolationMessage")
                    .getJSONObject("alert")
                    .getJSONObject("slaGuaranteedState")
                    .getString("guaranteedValue");
            String operator = xmlJSONObj
                    .getJSONObject("ViolationMessage")
                    .getJSONObject("alert")
                    .getJSONObject("slaGuaranteedState")
                    .getString("operator");

            log.info("NEW REQUIREMENT: vmId = " + vm_id + 
                    "; slaAgreementTerm = " + slaAgreementTerm + 
                    "; guaranteedValue = " + guaranteedValue + 
                    "; operator = " + operator);
            newRequirements.put(vm_id, getRequirementsFromSlaTerm(slaAgreementTerm, guaranteedValue));
         }
         log.info("ALL REQUIREMENTS: " + newRequirements.toString());
         return newRequirements;
    }

	@Override
	public void onMessage(Message message) {
		long now = System.currentTimeMillis();
		try {
			if (message.getJMSTimestamp() + IGNORE_MESSAGES_OLDER_THAN < now) {
				log.debug("Ignoring old message: " + message.getJMSMessageID());
                return;
			}
            
            log.debug("received message: " + message.toString());

            if (lastSelfAdaptation + MIN_TIME_BETWEEN_SELF_ADAPTATIONS >= System.currentTimeMillis()) {
                Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(lastSelfAdaptation);
                    log.warn("Not triggering self-adaptation since last self-adaptation was at " + 
                        c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND));
                    return;
            }

            if (message instanceof TextMessage) {
                TextMessage tm = (TextMessage) message;
                log.debug(tm.getText());

                if(tm.getText().contains("</ViolationMessage>")){
                    Map<String, Map<String, String>> newRequirements = readRequirementsFromViolationMessages(tm.getText());
                    lastSelfAdaptation = System.currentTimeMillis();
                    Config.INSTANCE.getVmManager().executeSelfAdaptationWithNewRequirements(newRequirements);
                }
                else{
                    lastSelfAdaptation = System.currentTimeMillis();
                    Config.INSTANCE.getVmManager().executeOnDemandSelfAdaptation();
                }
            }
            
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/*
	 * Auxiliary methods for enabling communication
	 */
	private final Gson gson = new Gson();
	public void publishMessageVmDeployed(VmDeployed vm) {
		publishMessage( String.format(VM_STATUS_TOPIC_NAME, vm.getId(), "deployed"), vm);
	}

	public void publishMessageVmDestroyed(VmDeployed vm) {
		publishMessage( String.format(VM_STATUS_TOPIC_NAME, vm.getId(), "destroyed"), vm);
	}

	public void publishMessageVmChangedState(VmDeployed vm, VmAction action) {
		publishMessage( String.format(VM_STATUS_TOPIC_NAME, vm.getId(), action.getCamelCase()), vm);
	}

	private void publishMessage(String topic, Object messageObject) {
		String json = gson.toJson(messageObject);
		log.debug(topic+"\n"+json);
		activeMqAdapter.publishMessage(topic, json);
	}

	// iaas-slam.monitoring.<slaId>.<vmId>.violationNotified
	private static final String VIOLATION_QUEUE_NAME = "iaas-slam.monitoring.%s.%s.violationNotified";

	// virtual-machine-manager.vm.<vmId>.<status>
	private static final String VM_STATUS_TOPIC_NAME = "virtual-machine-manager.vm.%s.%s";

}
