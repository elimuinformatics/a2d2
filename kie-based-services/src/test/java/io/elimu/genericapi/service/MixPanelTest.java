package io.elimu.genericapi.service;

import java.util.Date;
import java.util.Properties;

import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.junit.Test;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.process.ProcessInstance;

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
		listener.beforeProcessStarted(evt);
		Thread.sleep(10000);//wait for messages to be consumed
	}
}
