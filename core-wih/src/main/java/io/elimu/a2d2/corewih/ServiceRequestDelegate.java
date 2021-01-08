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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.elimu.a2d2.exception.WorkItemHandlerException;
import io.elimu.a2d2.genericmodel.ServiceRequest;

public class ServiceRequestDelegate implements WorkItemHandler {

	private static final String SERVICE_REQUEST = "serviceRequest";
	private static final String REQUEST_BODY = "body";
	private static final String REQUEST_PATH = "path";
	private static final String REQUEST_USER = "user";
	private static final String REQUEST_METHOD = "method";
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		try {
			ServiceRequest serviceRequest = (ServiceRequest) workItem.getParameter(SERVICE_REQUEST);
			Map<String, Object> workItemResult = workItem.getResults();

			if (!(serviceRequest instanceof ServiceRequest)) {
				logger.error("ServiceRequest is missing or Wrong type");
				throw new WorkItemHandlerException("ServiceRequest is missing or Wrong type");
			}

			setWorkItemParams(serviceRequest.getBody(), workItemResult, REQUEST_BODY);
			setWorkItemParams(serviceRequest.getPath(), workItemResult, REQUEST_PATH);
			setWorkItemParams(serviceRequest.getUser(), workItemResult, REQUEST_USER);
			setWorkItemParams(serviceRequest.getMethod(), workItemResult, REQUEST_METHOD);

			setWorkItemParamList(serviceRequest.getParams(), workItemResult);
			setWorkItemHeaderParam(serviceRequest.getHeaders(), workItemResult);

			manager.completeWorkItem(workItem.getId(), workItemResult);
		} catch (IllegalArgumentException ex) {
			logger.error("Error in serviceRequest", ex);
			throw new WorkItemHandlerException("Error in serviceRequest", ex);
		} catch (UnsupportedOperationException | ClassCastException | NullPointerException ex) {
			logger.error("Error in workresult Item Mapping", ex);
			throw new WorkItemHandlerException("Error in workresult Item Mapping", ex);
		}

		logger.trace("Service Request delegate completed");

	}
	
	private void setWorkItemParams(String item, Map<String, Object> workItemResult, String param) {
		if (item != null) {
			workItemResult.put(param, item);
		} else {
			logger.warn("Service request {} is missing", param);
		}
	}
	
	private void setWorkItemHeaderParam(Map<String, List<String>> headers, Map<String, Object> workItemResult) {
		if (headers == null || headers.isEmpty()) {
			logger.warn("Service request headers are missing");
		} else {
			for (Entry<String, List<String>> entry : headers.entrySet()) {
				String key = entry.getKey();
				if (key.contains("-")) {
					key = key.replace("-", "__");
				}
				if (entry.getValue().size() > 1) {
					workItemResult.put("header_values_" + key, entry.getValue());
				} else if (entry.getValue().size() == 1) {
					workItemResult.put("header_value_" + key, entry.getValue().get(0));
				}
			}
		}
	}
	
	private void setWorkItemParamList(Map<String, List<String>> params, Map<String, Object> workItemResult) {
		if (params == null || params.isEmpty()) { 
			logger.warn("Service request parameters are missing");
		} else {
			for (Entry<String, List<String>> entry : params.entrySet()) {
				if (entry.getValue().size() > 1) {
					workItemResult.put("param_values_" + entry.getKey(), entry.getValue());
				} else if (entry.getValue().size() == 1) {
					workItemResult.put("param_value_" + entry.getKey(), entry.getValue().get(0));
				}
			}
		}
	}

	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		manager.abortWorkItem(workItem.getId());
		logger.warn("Service Request delegate Aborted");
	}

}
