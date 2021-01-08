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
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.elimu.a2d2.exception.WorkItemHandlerException;
import io.elimu.a2d2.parsing.FhirParseUtil;
import io.elimu.a2d2.parsing.FhirParseUtil.FormatType;

public class DeserializeResourceDelegate implements WorkItemHandler {

	private static final Logger log = LoggerFactory.getLogger(DeserializeResourceDelegate.class);

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		FormatType type = FHIRDelegateHelper.getFormatTypeFromVersion((String) workItem.getParameter("fhirVersion"));
		String json = (String) workItem.getParameter("json");
		String xml = (String) workItem.getParameter("xml");
		IBaseResource resource = null;
		if (json != null) {
			resource = FhirParseUtil.getInstance().parseJsonResource(type, json);
		} else if (xml != null) {
			resource = FhirParseUtil.getInstance().parseXmlResource(type, xml);
		}
		if (resource == null) {
			log.error("Either xml or json payloads were both empty, or both invalid");
			throw new WorkItemHandlerException("Either xml or json payloads were both empty, or both invalid");
		}
		Map<String, Object> results = workItem.getResults();
		results.put("fhirResource", resource);
		manager.completeWorkItem(workItem.getId(), results);
		log.trace("DeserializeResource Delegate is completed");
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		manager.abortWorkItem(workItem.getId());
	}
}
