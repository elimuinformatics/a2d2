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

package io.elimu.a2d2.fhirresourcewih;

import java.util.Map;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.elimu.a2d2.exception.WorkItemHandlerException;
import io.elimu.a2d2.parsing.FhirParseUtil;
import io.elimu.a2d2.parsing.FhirParseUtil.FormatType;

public class SerializeResourceDelegate implements WorkItemHandler {

	private static final Logger log = LoggerFactory.getLogger(SerializeResourceDelegate.class);

	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String outputType = (String) workItem.getParameter("outputType");
		Object payload = workItem.getParameter("payload");
		if (outputType == null) {
			outputType = "json";
		}
		if (payload == null) {
			log.error("payload is a required parameter");
			throw new WorkItemHandlerException("payload is a required parameter");
		}
		FormatType type = FHIRDelegateHelper.getFormatTypeFromVersion((String) workItem.getParameter("fhirVersion"));
		String output = null;
		if ("json".equals(outputType)) {
			output = FhirParseUtil.getInstance().encodeJsonResource(type, payload);
		} else if ("xml".equals(outputType)) {
			output = FhirParseUtil.getInstance().encodeXmlResource(type, payload);
		}
		if (output == null) {
			log.debug("Either xml or json payload was empty or invalid");
			throw new WorkItemHandlerException("Either xml or json payload was empty or invalid");
		}
		Map<String, Object> results = workItem.getResults();
		results.put("output", output);
		manager.completeWorkItem(workItem.getId(), results);
		log.trace("SerializeResource Delegate is completed");
	}

	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		manager.abortWorkItem(workItem.getId());
	}
}
