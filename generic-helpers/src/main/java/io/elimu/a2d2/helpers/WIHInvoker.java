// Copyright 2018-2020 Elimu Informatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.elimu.a2d2.helpers;

import java.util.HashMap;
import java.util.Map;
import org.drools.core.WorkItemHandlerNotFoundException;
import org.drools.core.process.instance.impl.DefaultWorkItemManager;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.drools.core.spi.KnowledgeHelper;
import org.drools.persistence.jpa.processinstance.JPAWorkItemManager;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

public class WIHInvoker {

	private WIHInvoker() {
		throw new IllegalStateException("Utility class");
	}
	
	public static Map<String, Object> invokeDyn(KnowledgeHelper helper, String wihName, Object... params) {
		Map<String, Object> mapParams = new HashMap<>();
		if (params == null || params.length %2 != 0) {
			throw new RuntimeException("invokeDyn requires an even number of parameters");
		}
		for (int index = 0; index < params.length; index+=2) {
			mapParams.put(String.valueOf(params[index]), params[index+1]);
		}
		return invoke(helper, wihName, mapParams);
	}

	public static Map<String, Object> invoke(KnowledgeHelper helper, String wihName, Map<String, Object> params) {
		WorkItemHandler handler = getWorkItemHandler(helper, wihName);
		if (handler == null) {
			 throw new WorkItemHandlerNotFoundException("WorkItemHandler not found", wihName);
		}
		ForcedSyncWorkItemManager pseudoManager = new ForcedSyncWorkItemManager();
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setId(-1);
		workItem.setName(wihName);
		workItem.setNodeId(-1);
		workItem.setNodeInstanceId(-1);
		workItem.setParameters(params);
		workItem.setProcessInstanceId(-1);
		handler.executeWorkItem(workItem, pseudoManager);
		if (!pseudoManager.isCompleted()) {
			throw new RuntimeException("WorkItemHandlers invoked with " + WIHInvoker.class.getSimpleName()
					+ " must be sync (the must invoke completeWorkItem)");
		}
		return workItem.getResults();
	}

	private static WorkItemHandler getWorkItemHandler(KnowledgeHelper helper, String wihName) {
		for (KieSession ksession : helper.getKieRuntime().getKieBase().getKieSessions()) {
			WorkItemManager manager = ksession.getWorkItemManager();
			WorkItemHandler handler = null;
			if (manager instanceof DefaultWorkItemManager) {
				handler = ((DefaultWorkItemManager) manager).getWorkItemHandler(wihName);
			} else if (manager instanceof JPAWorkItemManager) {
				handler = ((JPAWorkItemManager) manager).getWorkItemHandler(wihName);
			}
			if (handler != null) {
				return handler;
			}
		}
		return null;
	}

	public static class ForcedSyncWorkItemManager implements WorkItemManager {

		private boolean completed = false;

		@Override
		public void completeWorkItem(long id, Map<String, Object> results) {
			completed = true;
		}

		@Override
		public void abortWorkItem(long id) {
			//default implementation
		}

		@Override
		public void registerWorkItemHandler(String workItemName, WorkItemHandler handler) {
			//default implementation
		}

		public boolean isCompleted() {
			return completed;
		}
	}
}
