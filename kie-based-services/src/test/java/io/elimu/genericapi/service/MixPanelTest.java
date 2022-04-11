package io.elimu.genericapi.service;

import java.util.Collections;
import java.util.Date;
import java.util.Properties;

import org.drools.core.event.ProcessVariableChangedEventImpl;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.junit.Test;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.process.ProcessInstance;

import io.elimu.a2d2.genericmodel.ServiceRequest;

public class MixPanelTest {

	@Test
	public void testMixPanelConnection() throws Exception {
		if (System.getProperty("mixpanel.app.token") == null) {
			return;
		}
		Properties config = new Properties();
		config.setProperty("mixpanel.log", "process.test-process.start");
		//for PP HTN, mixpanel.log in pp-htn-program should be like this:
			//mixpanel.log=process.pp-htn-program-start,process.node.SMSSingleInteraction.trigger
		//for pp-htn-formsubmit should be like this
			//mixpanel.log=process.pp-htn-formsubmit.end
		MixPanelEventListener listener = new MixPanelEventListener(config);
		final ProcessInstance procInst = new RuleFlowProcessInstance() {
			private static final long serialVersionUID = 1L;
			@Override
			public String getProcessId() {
				return "test-process";
			}
			@Override
			public long getId() {
				return 33L;
			}
		};
		ProcessStartedEvent evt = new ProcessStartedEvent() {
			
			@Override
			public KieRuntime getKieRuntime() {
				return null;
			}
			
			@Override
			public ProcessInstance getProcessInstance() {
				return procInst;
			}
			
			@Override
			public Date getEventDate() {
				return null;
			}
		};
		ServiceRequest request = new ServiceRequest();
		request.addHeaderValue("mixpanel-distinct-id", "test-distinct-id");
		request.addHeaderValue("mixpanel-whatever-field", "test value");
		ProcessVariableChangedEvent serviceRequestEvent = new ProcessVariableChangedEventImpl(
				"serviceRequest", "serviceRequest", null, request, Collections.emptyList(), procInst, null);
		listener.afterVariableChanged(serviceRequestEvent);
		listener.beforeProcessStarted(evt);
		while (MixPanelStack.getInstance().size() > 0) {
			Thread.sleep(1000); //wait for messages to be consumed
		}
	}
}
