package io.elimu.serviceapi.config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;

import io.elimu.a2d2.genericmodel.ServiceRequest;
import io.elimu.serviceapi.service.AppContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigAPIWorkItemHandler implements WorkItemHandler {

	private KieSession ksession;
	private static final Logger LOG = LoggerFactory.getLogger(ConfigAPIUtil.class);

	public ConfigAPIWorkItemHandler(KieSession ksession) {
		this.ksession = ksession;
	}
	
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String env = AppContextUtils.getInstance().getProfileName();
		String appName = (String) workItem.getParameter("configApiAppName");
		String client = (String) workItem.getParameter("configApiClient");
		WorkflowProcessInstance pI = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());
		ServiceRequest request = (ServiceRequest) pI.getVariable("serviceRequest");
		Map<String, Object> newValues = new HashMap<>();
		try {
			newValues = new ConfigAPIUtil().getConfig(request, env, client, appName);
		} catch (Exception e) {
			LOG.error("Timed out", e);
			
		}
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
