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

package io.elimu.a2d2.corewih;

import java.util.Map;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.elimu.a2d2.genericmodel.ServiceRequest;

public class CreateServiceRequestDelegate implements WorkItemHandler {

	private static final String SERVICE_REQUEST = "serviceRequest";
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {

		Map<String, Object> workItemResult = workItem.getResults();

		ServiceDelegateHelper serviceDelegateHelper = new ServiceDelegateHelper();
		ServiceRequest serviceRequest = serviceDelegateHelper.createServiceRequest(workItem);

		workItemResult.put(SERVICE_REQUEST, serviceRequest);
		manager.completeWorkItem(workItem.getId(), workItemResult);

		logger.trace("Create ServiceRequest  Delegate execution completed");
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		manager.abortWorkItem(workItem.getId());
		logger.warn("Service Request delegate Aborted");
	}

}
