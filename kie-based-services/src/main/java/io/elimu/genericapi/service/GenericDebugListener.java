package io.elimu.genericapi.service;

import org.kie.api.event.KieRuntimeEvent;
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
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class GenericDebugListener implements ProcessEventListener, RuleRuntimeEventListener, AgendaEventListener {

	private final Logger logger;
	private final boolean sendToLogs;	
	private JsonArray events = new JsonArray();
	
	public GenericDebugListener(String serviceId, KieSession ksession, boolean sendToLogs) {
		ksession.addEventListener((ProcessEventListener) this);
		ksession.addEventListener((RuleRuntimeEventListener) this);
		ksession.addEventListener((AgendaEventListener) this);
		this.sendToLogs = sendToLogs;
		this.logger = LoggerFactory.getLogger("io.elimu.a2d2."+serviceId);
	}

	private void log(KieRuntimeEvent event) {
		if (this.sendToLogs) {
			logger.info(event.toString());
		}
	}
	
	@Override
	public void matchCreated(MatchCreatedEvent event) {
		log(event);
		JsonObject evt = new JsonObject();
		evt.add("eventType", new JsonPrimitive("matchCreated"));
		evt.add("ruleName", new JsonPrimitive(event.getMatch().getRule().getName()));
		JsonArray array = new JsonArray();
		for (Object obj : event.getMatch().getObjects()) {
			array.add(obj.toString());
		}
		evt.add("objects", array);
		events.add(evt);
	}

	@Override
	public void matchCancelled(MatchCancelledEvent event) {
		log(event);
		JsonObject evt = new JsonObject();
		evt.add("eventType", new JsonPrimitive("matchCancelled"));
		evt.add("cause", new JsonPrimitive(event.getCause().toString()));
		evt.add("ruleName", new JsonPrimitive(event.getMatch().getRule().getName()));
		JsonArray array = new JsonArray();
		for (Object obj : event.getMatch().getObjects()) {
			array.add(obj.toString());
		}
		evt.add("objects", array);
		events.add(evt);
	}

	@Override
	public void beforeMatchFired(BeforeMatchFiredEvent event) {
		log(event);
		JsonObject evt = new JsonObject();
		evt.add("eventType", new JsonPrimitive("beforeMatchFired"));
		evt.add("ruleName", new JsonPrimitive(event.getMatch().getRule().getName()));
		JsonArray array = new JsonArray();
		for (Object obj : event.getMatch().getObjects()) {
			array.add(obj.toString());
		}
		evt.add("objects", array);
		events.add(evt);
	}

	@Override
	public void afterMatchFired(AfterMatchFiredEvent event) {
		log(event);
		JsonObject evt = new JsonObject();
		evt.add("eventType", new JsonPrimitive("afterMatchFired"));
		evt.add("ruleName", new JsonPrimitive(event.getMatch().getRule().getName()));
		JsonArray array = new JsonArray();
		for (Object obj : event.getMatch().getObjects()) {
			array.add(obj.toString());
		}
		evt.add("objects", array);
		events.add(evt);
	}

	@Override
	public void agendaGroupPopped(AgendaGroupPoppedEvent event) {
		log(event);
		JsonObject evt = new JsonObject();
		evt.add("eventType", new JsonPrimitive("agendaGroupPopped"));
		evt.add("agendaGroup", new JsonPrimitive(event.getAgendaGroup().getName()));
		events.add(evt);
	}

	@Override
	public void agendaGroupPushed(AgendaGroupPushedEvent event) {
		log(event);
		JsonObject evt = new JsonObject();
		evt.add("eventType", new JsonPrimitive("agendaGroupPushed"));
		evt.add("agendaGroup", new JsonPrimitive(event.getAgendaGroup().getName()));
		events.add(evt);
	}

	@Override
	public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
		log(event);
		JsonObject evt = new JsonObject();
		evt.add("eventType", new JsonPrimitive("beforeRuleFlowGroupActivated"));
		evt.add("ruleFlowGroup", new JsonPrimitive(event.getRuleFlowGroup().getName()));
		events.add(evt);
	}

	@Override
	public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
		log(event);
		JsonObject evt = new JsonObject();
		evt.add("eventType", new JsonPrimitive("afterRuleFlowGroupActivated"));
		evt.add("ruleFlowGroup", new JsonPrimitive(event.getRuleFlowGroup().getName()));
		events.add(evt);
	}

	@Override
	public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
		log(event);
		JsonObject evt = new JsonObject();
		evt.add("eventType", new JsonPrimitive("beforeRuleFlowGroupDeactivated"));
		evt.add("ruleFlowGroup", new JsonPrimitive(event.getRuleFlowGroup().getName()));
		events.add(evt);
	}

	@Override
	public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
		log(event);
		JsonObject evt = new JsonObject();
		evt.add("eventType", new JsonPrimitive("afterRuleFlowGroupDeactivated"));
		evt.add("ruleFlowGroup", new JsonPrimitive(event.getRuleFlowGroup().getName()));
		events.add(evt);
	}

	@Override
	public void objectInserted(ObjectInsertedEvent event) {
		log(event);
		JsonObject evt = new JsonObject();
		evt.add("eventType", new JsonPrimitive("objectInserted"));
		if (event.getRule() != null) {
			evt.add("ruleName", new JsonPrimitive(event.getRule().getName()));
		}
		evt.add("object", new JsonPrimitive(event.getObject().toString()));
		events.add(evt);
	}

	@Override
	public void objectUpdated(ObjectUpdatedEvent event) {
		log(event);
		JsonObject evt = new JsonObject();
		evt.add("eventType", new JsonPrimitive("objectUpdated"));
		if (event.getRule() != null) {
			evt.add("ruleName", new JsonPrimitive(event.getRule().getName()));
		}
		evt.add("object", new JsonPrimitive(event.getObject().toString()));
		events.add(evt);
	}

	@Override
	public void objectDeleted(ObjectDeletedEvent event) {
		log(event);
		JsonObject evt = new JsonObject();
		evt.add("eventType", new JsonPrimitive("objectDeleted"));
		if (event.getRule() != null) {
			evt.add("ruleName", new JsonPrimitive(event.getRule().getName()));
		}
		evt.add("object", new JsonPrimitive(event.getOldObject().toString()));
		events.add(evt);
	}

	@Override
	public void beforeProcessStarted(ProcessStartedEvent event) {
		log(event);
		JsonObject evt = new JsonObject();
		evt.add("eventType", new JsonPrimitive("beforeProcessStarted"));
		evt.add("processId", new JsonPrimitive(event.getProcessInstance().getProcessId()));
		evt.add("processInstanceId", new JsonPrimitive(event.getProcessInstance().getId()));
		events.add(evt);
	}

	@Override
	public void afterProcessStarted(ProcessStartedEvent event) {
		log(event);
		JsonObject evt = new JsonObject();
		evt.add("eventType", new JsonPrimitive("afterProcessStarted"));
		evt.add("processId", new JsonPrimitive(event.getProcessInstance().getProcessId()));
		evt.add("processInstanceId", new JsonPrimitive(event.getProcessInstance().getId()));
		events.add(evt);
	}

	@Override
	public void beforeProcessCompleted(ProcessCompletedEvent event) {
		log(event);
		JsonObject evt = new JsonObject();
		evt.add("eventType", new JsonPrimitive("beforeProcessCompleted"));
		evt.add("processId", new JsonPrimitive(event.getProcessInstance().getProcessId()));
		evt.add("processInstanceId", new JsonPrimitive(event.getProcessInstance().getId()));
		events.add(evt);
	}

	@Override
	public void afterProcessCompleted(ProcessCompletedEvent event) {
		log(event);
		JsonObject evt = new JsonObject();
		evt.add("eventType", new JsonPrimitive("afterProcessCompleted"));
		evt.add("processId", new JsonPrimitive(event.getProcessInstance().getProcessId()));
		evt.add("processInstanceId", new JsonPrimitive(event.getProcessInstance().getId()));
		events.add(evt);
	}

	@Override
	public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
		log(event);
		JsonObject evt = new JsonObject();
		evt.add("eventType", new JsonPrimitive("beforeNodeTriggered"));
		evt.add("processId", new JsonPrimitive(event.getProcessInstance().getProcessId()));
		evt.add("processInstanceId", new JsonPrimitive(event.getProcessInstance().getId()));
		evt.add("nodeName", new JsonPrimitive(event.getNodeInstance().getNodeName()));
		events.add(evt);
	}

	@Override
	public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
		log(event);
		JsonObject evt = new JsonObject();
		evt.add("eventType", new JsonPrimitive("afterNodeTriggered"));
		evt.add("processId", new JsonPrimitive(event.getProcessInstance().getProcessId()));
		evt.add("processInstanceId", new JsonPrimitive(event.getProcessInstance().getId()));
		evt.add("nodeName", new JsonPrimitive(event.getNodeInstance().getNodeName()));
		events.add(evt);
	}

	@Override
	public void beforeNodeLeft(ProcessNodeLeftEvent event) {
		log(event);
		JsonObject evt = new JsonObject();
		evt.add("eventType", new JsonPrimitive("beforeNodeLeft"));
		evt.add("processId", new JsonPrimitive(event.getProcessInstance().getProcessId()));
		evt.add("processInstanceId", new JsonPrimitive(event.getProcessInstance().getId()));
		evt.add("nodeName", new JsonPrimitive(event.getNodeInstance().getNodeName()));
		events.add(evt);
	}

	@Override
	public void afterNodeLeft(ProcessNodeLeftEvent event) {
		log(event);
		JsonObject evt = new JsonObject();
		evt.add("eventType", new JsonPrimitive("afterNodeLeft"));
		evt.add("processId", new JsonPrimitive(event.getProcessInstance().getProcessId()));
		evt.add("processInstanceId", new JsonPrimitive(event.getProcessInstance().getId()));
		evt.add("nodeName", new JsonPrimitive(event.getNodeInstance().getNodeName()));
		events.add(evt);
	}

	@Override
	public void beforeVariableChanged(ProcessVariableChangedEvent event) {
		log(event);
		JsonObject evt = new JsonObject();
		evt.add("eventType", new JsonPrimitive("beforeVariableChanged"));
		evt.add("processId", new JsonPrimitive(event.getProcessInstance().getProcessId()));
		evt.add("processInstanceId", new JsonPrimitive(event.getProcessInstance().getId()));
		evt.add("variableId", new JsonPrimitive(event.getVariableId()));
		evt.add("oldValue", event.getOldValue() == null ? JsonNull.INSTANCE : new JsonPrimitive(event.getOldValue().toString()));
		evt.add("newValue", event.getNewValue() == null ? JsonNull.INSTANCE : new JsonPrimitive(event.getNewValue().toString()));
		events.add(evt);
	}

	@Override
	public void afterVariableChanged(ProcessVariableChangedEvent event) {
		log(event);
		JsonObject evt = new JsonObject();
		evt.add("eventType", new JsonPrimitive("afterVariableChanged"));
		evt.add("processId", new JsonPrimitive(event.getProcessInstance().getProcessId()));
		evt.add("processInstanceId", new JsonPrimitive(event.getProcessInstance().getId()));
		evt.add("variableId", new JsonPrimitive(event.getVariableId()));
		evt.add("oldValue", event.getOldValue() == null ? JsonNull.INSTANCE : new JsonPrimitive(event.getOldValue().toString()));
		evt.add("newValue", event.getNewValue() == null ? JsonNull.INSTANCE : new JsonPrimitive(event.getNewValue().toString()));
		events.add(evt);
	}

	public String toJson(String originalBody) {
		JsonObject retval = new JsonObject();
		retval.add("result", new JsonPrimitive(originalBody));
		retval.add("debug", events);
		return new Gson().toJson(retval);
	}
	
}
