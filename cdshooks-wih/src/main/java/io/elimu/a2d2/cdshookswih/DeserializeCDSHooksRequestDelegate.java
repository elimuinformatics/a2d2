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

package io.elimu.a2d2.cdshookswih;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.elimu.a2d2.cdsresponse.entity.CDSHookRequest;
import io.elimu.a2d2.cdsresponse.entity.FHIRAuthorization;
import io.elimu.a2d2.exception.WorkItemHandlerException;
import io.elimu.a2d2.parsing.FhirParseUtil;
import io.elimu.a2d2.parsing.FhirParseUtil.FormatType;

public class DeserializeCDSHooksRequestDelegate implements WorkItemHandler {

	private static final Logger log = LoggerFactory.getLogger(DeserializeCDSHooksRequestDelegate.class);

	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		try {
			Map<String, Object> workItemResult = workItem.getResults();
			String requestJson = (String) workItem.getParameter("requestJson");
			CDSHookRequest cdsHookRequest = new Gson().fromJson(requestJson, CDSHookRequest.class);

			Optional<String> hook = Optional.ofNullable(cdsHookRequest.getHook());
			Optional<String> fhirServer = Optional.ofNullable(cdsHookRequest.getFhirServer());
			Optional<String> hookInstance = Optional.ofNullable(cdsHookRequest.getHookInstance());
			Optional<FHIRAuthorization> fhirAuthorization = Optional.ofNullable(cdsHookRequest.getFhirAuthorization());
			Optional<String> user = Optional.ofNullable(cdsHookRequest.getUser());
			Map<String, Object> context = cdsHookRequest.getContext();
			Map<String, Object> prefetch = cdsHookRequest.getPrefetch();

			FormatType fhirVersion = findFormatType((String) workItem.getParameter("fhirVersion"));
		
			if (!hook.isPresent()) {
				log.error("Missing parameter hook");
				throw new WorkItemHandlerException("Missing parameter hook");
			}
			if (!hookInstance.isPresent()) {
				log.error("Missing parameter hookInstance");
				throw new WorkItemHandlerException("Missing parameter hookInstance");
			}

			setWorkItemParams(fhirServer, workItemResult, "fhirServer");
			setWorkItemParams(fhirAuthorization, workItemResult, "fhirAuthorization");
			setWorkItemParams(user, workItemResult, "user");
			
            if (context == null) {
				log.error("Missing parameter context");
				throw new WorkItemHandlerException("Missing parameter context");
			} else {
				setWorkItemResource(context, workItemResult, fhirVersion, "context");
			}
			if (prefetch != null && !prefetch.isEmpty()) { // OPTIONAL PARAM
				setWorkItemResource(prefetch, workItemResult, fhirVersion, "prefetch");
			} else {
				log.warn("Missing or empty parameter prefetch");
			}

			workItemResult.put("cdsHookRequest", cdsHookRequest);
			workItemResult.put("hook", hook.get());
			workItemResult.put("hookInstance", hookInstance.get());

			manager.completeWorkItem(workItem.getId(), workItemResult);
		} catch (IllegalArgumentException ex) {
			log.error("Missing or empty parameter prefetch " + ex.getMessage(), ex);
			throw new WorkItemHandlerException("Missing or empty parameter");
		} catch (UnsupportedOperationException | ClassCastException | NullPointerException ex) {
			log.error("Error in deserializing json string", ex);
			throw new WorkItemHandlerException("Error in deserializing json string");
		} catch (WorkItemHandlerException ex) {
			// rethrow exception without any change or additional logging
			throw ex;
		} catch (Exception ex) {
			log.error("Unknown Error in the Delegate", ex);
			throw new WorkItemHandlerException(ex);
		}
		log.trace("DeserializeCDSHooksRequest Delegate execution completed");
	}
	
	private void setWorkItemParams(Optional<?> item, Map<String, Object> workItemResult, String param) {
		if (item.isPresent()) {
			workItemResult.put(param, item.get());
		} else {
			log.warn("Missing parameter {}", param);
		}
	}

	private void setWorkItemResource(Map<String, Object> item, Map<String, Object> workItemResult,
			FormatType fhirVersion, String param) {
		for (Entry<String, Object> entry : item.entrySet()) {
			workItemResult.put(param.concat("_").concat(entry.getKey()), entry.getValue());
			Object resource = parseResource(entry.getValue(), fhirVersion);
			if (resource != null) {
				workItemResult.put(param.concat("Resource_").concat(entry.getKey()), resource);
				log.debug("Added  the {} Resource ::{}", param, entry.getKey());
			}
		}
	}

	private Object parseResource(Object jsonObject, FormatType fhirVersion) {
		try {

			JSONObject parsedJsonObj = null;

			if(jsonObject instanceof Map) {
				parsedJsonObj = (JSONObject) new JSONParser().
						parse(JSONObject.toJSONString((Map<?,?>)jsonObject));
			}


			if((parsedJsonObj!=null) && parsedJsonObj.get("resourceType") != null) {

				String resourceData =  parsedJsonObj.toJSONString();
				resourceData = resourceData.replaceAll("(\\:\\d+)\\.0(\\}|\\,|\\]|\\s)","$1 $2");

				return FhirParseUtil.getInstance().parseJsonResource(
						fhirVersion, resourceData);
			}
		}
		catch(Exception ex) { // If the jsonObject is not a json String like plain String
			return null;
		}
		return null;
	}
	
	private FormatType findFormatType(String fhirVersion) {
		if ("STU3".equalsIgnoreCase(fhirVersion))
			return FormatType.FHIR3;
		if ("R4".equalsIgnoreCase(fhirVersion))
			return FormatType.FHIR4;
		FormatType type = FormatType.FHIR2;
		if (!"DSTU2".equalsIgnoreCase(fhirVersion))
			log.warn("Missing supported fhirVersion, parse as FHIR2");
		return type;
	}

	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		manager.abortWorkItem(workItem.getId());
		log.warn("DeserializeCDSHooksRequest Response delegate Aborted");
	}

}
