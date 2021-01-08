package io.elimu.a2d2.service;

import java.util.HashMap;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogDelegate implements WorkItemHandler {

	private static final Logger LOG = LoggerFactory.getLogger(LogDelegate.class);
	
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String msg = (String) workItem.getParameter("Message");
		LOG.info(msg);
		manager.completeWorkItem(workItem.getId(), new HashMap<>());
	}
	
	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// not implemented on purpose
	}
}
