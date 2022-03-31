package io.elimu.serviceapi.config;

import java.util.Map;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;

import io.elimu.serviceapi.service.AppContextUtils;

public class ConfigAPIWorkItemHandler implements WorkItemHandler {

	private KieSession ksession;

	public ConfigAPIWorkItemHandler(KieSession ksession) {
		this.ksession = ksession;
	}
	
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String env = AppContextUtils.getInstance().getProfileName();
		String appName = (String) workItem.getParameter("configApiAppName");
		String client = (String) workItem.getParameter("configApiClient");
		WorkflowProcessInstance pI = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());
		Map<String, Object> newValues = new ConfigAPIUtil().getConfig(env, client, appName);
		for (Map.Entry<String, Object> entry : newValues.entrySet()) {
			pI.setVariable(entry.getKey(), entry.getValue());
		}
		manager.completeWorkItem(workItem.getId(), newValues);
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// do nothing
	}

}
