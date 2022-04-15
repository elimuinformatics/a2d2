package io.elimu.genericapi.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.AgendaGroupPoppedEvent;
import org.kie.api.event.rule.AgendaGroupPushedEvent;
import org.kie.api.event.rule.BeforeMatchFiredEvent;
import org.kie.api.event.rule.MatchCancelledEvent;
import org.kie.api.event.rule.MatchCreatedEvent;
import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;
import org.kie.api.event.rule.RuleFlowGroupActivatedEvent;
import org.kie.api.event.rule.RuleFlowGroupDeactivatedEvent;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.elimu.a2d2.genericmodel.ServiceRequest;

/**
 * Considering keys
 * 
 * mixpanel.log=process.start,process.end,process.var.varName.change,process.node.nodeName.trigger,process.node.nodeName.left,rules.ruleName.fire
 *
 */
public class MixPanelEventListener implements ProcessEventListener, RuleRuntimeEventListener, AgendaEventListener {

	private static final Logger LOG = LoggerFactory.getLogger(MixPanelEventListener.class);
	private final Properties serviceConfig;
	private String distinctId;
	private Map<String, String> otherData = new HashMap<>();
	
	public MixPanelEventListener(Properties config) {
		this.serviceConfig = config;
	}
	
	protected void process(JSONObject event) {
		event.put("distinctId", this.distinctId);
		event.put("$distinct_id", distinctId);
		this.otherData.forEach((k,v) -> event.put(k, v));
		MixPanelStack.getInstance().add(event);
	}
	
	protected void identify(ServiceRequest serviceRequest) {
		if (serviceRequest != null) {
			this.distinctId = serviceRequest.getHeader("mixpanel-distinct-id");
			this.otherData.clear();
			for (String headerName : serviceRequest.getHeaders().keySet()) {
				if (headerName.startsWith("mixpanel-")) {
					this.otherData.put(headerName.replace("mixpanel-", ""), serviceRequest.getHeader(headerName));
				}
			}
		}
	}
	
	@Override
	public void matchCreated(MatchCreatedEvent event) {
	}

	@Override
	public void matchCancelled(MatchCancelledEvent event) {
	}

	@Override
	public void beforeMatchFired(BeforeMatchFiredEvent event) {
	}

	@Override
	public void afterMatchFired(AfterMatchFiredEvent event) {
		if (serviceConfig.containsKey("mixpanel.log") && serviceConfig.getProperty("mixpanel.log").contains("rules." + event.getMatch().getRule().getName() + ".fire")) {
			try {
				JSONObject evt = new JSONObject();
				evt.put("eventType", ("afterMatchFired"));
				evt.put("eventDescription", "Rule " + event.getMatch().getRule().getName() + " fired");
				evt.put("ruleName", (event.getMatch().getRule().getName()));
				JSONArray array = new JSONArray();
				for (Object obj : event.getMatch().getObjects()) {
					array.put(hidePasswordValue(obj));
				}
				evt.put("objects", array);
				process(evt);
			} catch (JSONException e) {
				LOG.warn("problem creating MixPanel event data", e);
			}
		}
	}

	@Override
	public void agendaGroupPopped(AgendaGroupPoppedEvent event) {
	}

	@Override
	public void agendaGroupPushed(AgendaGroupPushedEvent event) {
	}

	@Override
	public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
	}

	@Override
	public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
	}

	@Override
	public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
	}

	@Override
	public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
	}

	@Override
	public void objectInserted(ObjectInsertedEvent event) {
	}

	@Override
	public void objectUpdated(ObjectUpdatedEvent event) {
	}

	@Override
	public void objectDeleted(ObjectDeletedEvent event) {
	}

	@Override
	public void beforeProcessStarted(ProcessStartedEvent event) {
		if (serviceConfig.containsKey("mixpanel.log") && serviceConfig.getProperty("mixpanel.log").contains("process." + event.getProcessInstance().getProcessId() + ".start")) {
			try {
				JSONObject evt = new JSONObject();
				evt.put("eventDescription", "Process " + event.getProcessInstance().getProcessId() + " completed");
				evt.put("eventType", ("beforeProcessStarted"));
				evt.put("processId", (event.getProcessInstance().getProcessId()));
				evt.put("processInstanceId", (event.getProcessInstance().getId()));
				process(evt);
			} catch (JSONException e) {
				LOG.warn("problem creating MixPanel event data", e);
			}
		}
	}

	@Override
	public void afterProcessStarted(ProcessStartedEvent event) {
	}

	@Override
	public void beforeProcessCompleted(ProcessCompletedEvent event) {
	}

	@Override
	public void afterProcessCompleted(ProcessCompletedEvent event) {
		if (serviceConfig.containsKey("mixpanel.log") && serviceConfig.getProperty("mixpanel.log").contains("process." + event.getProcessInstance().getProcessId() +  ".end")) {
			try {
				JSONObject evt = new JSONObject();
				evt.put("eventDescription", "Process " + event.getProcessInstance().getProcessId() + " completed");
				evt.put("eventType", ("afterProcessCompleted"));
				evt.put("processId", (event.getProcessInstance().getProcessId()));
				evt.put("processInstanceId", (event.getProcessInstance().getId()));
				process(evt);
			} catch (JSONException e) {
				LOG.warn("problem creating MixPanel event data", e);
			}
		}
	}

	@Override
	public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
		if (serviceConfig.containsKey("mixpanel.log") && serviceConfig.getProperty("mixpanel.log").contains("process.node." + event.getNodeInstance().getNodeName() + ".trigger")) {
			try {
				JSONObject evt = new JSONObject();
				evt.put("eventType", ("beforeNodeTriggered"));
				evt.put("eventDescription", "Process " + event.getProcessInstance().getProcessId() + " node of type " + event.getNodeInstance().getNodeName() + " triggered");
				evt.put("processId", (event.getProcessInstance().getProcessId()));
				evt.put("processInstanceId", (event.getProcessInstance().getId()));
				if (event.getNodeInstance() != null && event.getNodeInstance().getNodeName() != null) {
					evt.put("nodeName", (event.getNodeInstance().getNodeName()));
				}
				process(evt);
			} catch (JSONException e) {
				LOG.warn("problem creating MixPanel event data", e);
			}
		}
	}

	@Override
	public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
	}

	@Override
	public void beforeNodeLeft(ProcessNodeLeftEvent event) {
	}

	@Override
	public void afterNodeLeft(ProcessNodeLeftEvent event) {
		if (serviceConfig.containsKey("mixpanel.log") && serviceConfig.getProperty("mixpanel.log").contains("process.node." + event.getNodeInstance().getNodeName() + ".left")) {
			try {
				JSONObject evt = new JSONObject();
				evt.put("eventDescription", "Process " + event.getProcessInstance().getProcessId() + " node of type " + event.getNodeInstance().getNodeName() + " completed");
				evt.put("eventType", ("afterNodeLeft"));
				evt.put("processId", (event.getProcessInstance().getProcessId()));
				evt.put("processInstanceId", (event.getProcessInstance().getId()));
				if (event.getNodeInstance() != null && event.getNodeInstance().getNodeName() != null) {
					evt.put("nodeName", (event.getNodeInstance().getNodeName()));
				}
				process(evt);
			} catch (JSONException e) {
				LOG.warn("problem creating MixPanel event data", e);
			}
		}
	}

	@Override
	public void beforeVariableChanged(ProcessVariableChangedEvent event) {
	}

	@Override
	public void afterVariableChanged(ProcessVariableChangedEvent event) {
		if ("serviceRequest".equals(event.getVariableId())) {
			identify((ServiceRequest) event.getNewValue());
		}
		if (serviceConfig.containsKey("mixpanel.log") && serviceConfig.getProperty("mixpanel.log").contains("process.var." + event.getVariableId() + ".change")) {
			try {
				JSONObject evt = new JSONObject();
				evt.put("eventDescription", "Process " + event.getProcessInstance().getProcessId() + " variable " + event.getVariableId() + " changed");
				evt.put("eventType", ("afterVariableChanged"));
				evt.put("processId", (event.getProcessInstance().getProcessId()));
				evt.put("processInstanceId", (event.getProcessInstance().getId()));
				evt.put("variableId", (event.getVariableId()));
				if (event.getVariableId().toLowerCase().contains("password")) {
					evt.put("oldValue", event.getOldValue() == null ? null : ("*******"));
					evt.put("newValue", event.getNewValue() == null ? null : ("*******"));
				} else {
					evt.put("oldValue", event.getOldValue() == null ? null : (hidePasswordValue(event.getOldValue())));
					evt.put("newValue", event.getNewValue() == null ? null : (hidePasswordValue(event.getNewValue())));
				}
				process(evt);
			} catch (JSONException e) {
				LOG.warn("problem creating MixPanel event data", e);
			}
		}
	}

	private static String hidePasswordValue(Object value) {
		if (value == null) {
			return "null";
		}
		String retval = value.toString();
		//basically, any variable or attribute in the toString that has password is taken and stripped
		if (retval.toLowerCase().contains("password")) {
			Pattern pat = java.util.regex.Pattern.compile("assword([^=]*)=([^,\\\\])*");
			retval = pat.matcher(retval).replaceAll("assword$1=-----");
		}
		return retval;
	}
}
