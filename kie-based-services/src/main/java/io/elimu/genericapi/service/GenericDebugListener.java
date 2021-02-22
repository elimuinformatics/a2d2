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

public class GenericDebugListener implements ProcessEventListener, RuleRuntimeEventListener, AgendaEventListener {

	private final Logger logger;
	
	public GenericDebugListener(String serviceId, KieSession ksession) {
		ksession.addEventListener((ProcessEventListener) this);
		ksession.addEventListener((RuleRuntimeEventListener) this);
		ksession.addEventListener((AgendaEventListener) this);
		this.logger = LoggerFactory.getLogger("io.elimu.a2d2."+serviceId);
	}

	private void log(KieRuntimeEvent event) {
		logger.info(event.toString());
	}
	
	@Override
	public void matchCreated(MatchCreatedEvent event) {
		log(event);
	}

	@Override
	public void matchCancelled(MatchCancelledEvent event) {
		log(event);
	}

	@Override
	public void beforeMatchFired(BeforeMatchFiredEvent event) {
		log(event);
	}

	@Override
	public void afterMatchFired(AfterMatchFiredEvent event) {
		log(event);
	}

	@Override
	public void agendaGroupPopped(AgendaGroupPoppedEvent event) {
		log(event);
	}

	@Override
	public void agendaGroupPushed(AgendaGroupPushedEvent event) {
		log(event);
	}

	@Override
	public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
		log(event);
	}

	@Override
	public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
		log(event);
	}

	@Override
	public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
		log(event);
	}

	@Override
	public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
		log(event);
	}

	@Override
	public void objectInserted(ObjectInsertedEvent event) {
		log(event);
	}

	@Override
	public void objectUpdated(ObjectUpdatedEvent event) {
		log(event);
	}

	@Override
	public void objectDeleted(ObjectDeletedEvent event) {
		log(event);
	}

	@Override
	public void beforeProcessStarted(ProcessStartedEvent event) {
		log(event);
	}

	@Override
	public void afterProcessStarted(ProcessStartedEvent event) {
		log(event);
	}

	@Override
	public void beforeProcessCompleted(ProcessCompletedEvent event) {
		log(event);
	}

	@Override
	public void afterProcessCompleted(ProcessCompletedEvent event) {
		log(event);
	}

	@Override
	public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
		log(event);
	}

	@Override
	public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
		log(event);
	}

	@Override
	public void beforeNodeLeft(ProcessNodeLeftEvent event) {
		log(event);
	}

	@Override
	public void afterNodeLeft(ProcessNodeLeftEvent event) {
		log(event);
	}

	@Override
	public void beforeVariableChanged(ProcessVariableChangedEvent event) {
		log(event);
	}

	@Override
	public void afterVariableChanged(ProcessVariableChangedEvent event) {
		log(event);
	}
}
