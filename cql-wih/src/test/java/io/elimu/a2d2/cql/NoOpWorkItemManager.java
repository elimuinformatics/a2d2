package io.elimu.a2d2.cql;

import java.util.Map;

import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

public class NoOpWorkItemManager implements WorkItemManager {

	private boolean completed = false;
	
	@Override
	public void completeWorkItem(long id, Map<String, Object> results) {
		completed = true;
	}

	@Override
	public void abortWorkItem(long id) {
	}

	@Override
	public void registerWorkItemHandler(String workItemName, WorkItemHandler handler) {
	}

	public boolean isCompleted() {
		return completed;
	}
}
