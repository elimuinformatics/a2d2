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
import java.util.Map.Entry;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.elimu.a2d2.genericmodel.ServiceResponse;

public class ServiceResponseDelegate implements WorkItemHandler {

	private static final String SERVICE_RESPONSE = "serviceResponse";
	private static final String RESPONSE_CODE = "responseCode";
	private static final String HEADER_VALUE_PARAM = "header_value_";
	private static final String RESPONSE_BODY = "body";
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {

		Map<String, Object> workItemResult = workItem.getResults();
		String body = (String) workItem.getParameter(RESPONSE_BODY);
		Object objResponseCode = workItem.getParameter(RESPONSE_CODE);
		Integer responseCode = null;
		if (objResponseCode != null) {
			responseCode = Integer.valueOf(workItem.getParameter(RESPONSE_CODE).toString());
		}
		ServiceResponse serviceResponse = (ServiceResponse) workItem.getParameter(SERVICE_RESPONSE);

		if (serviceResponse == null) {
			serviceResponse = new ServiceResponse();
		}

		if (body == null) {
			logger.warn("body is null");
		}
		else {
			serviceResponse.setBody(body);
		}

		if (responseCode == null) {
			logger.warn("responseCode is null");
		}
		else {
			serviceResponse.setResponseCode(responseCode);
		}


		for (Entry<String, Object> entry : workItem.getParameters().entrySet()) {
			if (entry.getKey().startsWith(HEADER_VALUE_PARAM)) {
				String key = entry.getKey().replace(HEADER_VALUE_PARAM, "");
				if (key.contains("__")) {
					key = key.replace("__", "-");
				}
				serviceResponse.addHeaderValue(key, (String) entry.getValue());
			}
		}

		workItemResult.put(SERVICE_RESPONSE, serviceResponse);
		manager.completeWorkItem(workItem.getId(), workItemResult);

		logger.trace("Service Response Delegate execution completed");
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		manager.abortWorkItem(workItem.getId());
		logger.warn("Service Response delegate Aborted");
	}

}
