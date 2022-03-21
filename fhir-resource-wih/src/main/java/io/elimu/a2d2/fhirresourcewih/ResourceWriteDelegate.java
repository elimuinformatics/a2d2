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

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.elimu.a2d2.exception.FhirServerException;
import io.elimu.a2d2.parsing.FhirParseUtil;

public class ResourceWriteDelegate implements WorkItemHandler {

	FHIRDelegateHelper fhirdelegatehelper = null;
	private static final Logger log = LoggerFactory.getLogger(ResourceWriteDelegate.class);
	
	private static final String REFLECT_ERR = "Reflection error";
	private static final String MISSING_RSRC = "Resource is missing";
	private static final String CLIENT_NULL = "Fhir client is null : check server URL";

	public ResourceWriteDelegate() {
		fhirdelegatehelper = new FHIRDelegateHelper();
	}

	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		try {
			Map<String, Object> workItemParam = workItem.getParameters();
			Map<String, Object> workItemResult = workItem.getResults();
			String fhirType = (String) workItemParam.get("fhirVersion");
			URL serverUrl = new URL((String) workItemParam.get("fhirServer"));
			Object oResource = workItemParam.get("resource");
			String actionType = (String) workItem.getParameter("actionType");
			
			if (oResource == null) {
				log.error(MISSING_RSRC);
				throw new FhirServerException(MISSING_RSRC);
			}
			Object fhirResource = getFhirResource(oResource);

			Object ctx = fhirdelegatehelper.getFhirContext(fhirType);
			if (ctx != null) {
				Object client = fhirdelegatehelper.getFhirClient(ctx, serverUrl);
				if (fhirResource == null && oResource instanceof String) {
					fhirResource = FhirParseUtil.getInstance().parseJsonResource(fhirdelegatehelper.getFormatType(fhirType), oResource.toString());
				}

				if (client != null) {
					Object auth = workItemParam.get("authHeader");
					if (!completeworkItem(client, fhirResource, workItemResult, manager, workItem, auth == null ? null : auth.toString(), actionType)) {
						log.error(serverUrl.toString(), CLIENT_NULL);
						throw new FhirServerException(serverUrl.toString(), CLIENT_NULL);
					}

				} else {
					log.error(CLIENT_NULL);
					throw new FhirServerException(CLIENT_NULL);
				}
			}

		} catch (FhirServerException ex) {
			log.error("Error in ResourceWriteDelegate",ex);
			throw new FhirServerException(ex);
		} catch (Exception ex) {
			log.error("FhirAddDeletage failed",ex);
			throw new FhirServerException("FhirAddDeletage failed", ex);
		}

		log.trace("ResourceWriteDelegate is completed");
	}
	
	private Object getFhirResource(Object oResource) {
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			if (oResource != null && cl.loadClass("org.hl7.fhir.instance.model.api.IBaseResource").isAssignableFrom(oResource.getClass())) {
				return oResource;
			} else if (oResource instanceof String && ((String) oResource).isEmpty()) {
				log.error("Resource is empty or not a String");
				throw new FhirServerException(MISSING_RSRC);
			}
		} catch (ClassNotFoundException e) {
			log.error("Resource cannot be invoked with reflect", e);
			throw new FhirServerException(REFLECT_ERR);
		}
		return null;
	}
	
	private boolean completeworkItem(Object client, Object fhirResource,
			Map<String, Object> workItemResult, WorkItemManager manager, WorkItem workItem, String authHeader,String actionType) throws Exception {
		if (fhirdelegatehelper.authenticateFhirServer(client, authHeader)) {
			Object result = null;
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Class<?> resClass = cl.loadClass("org.hl7.fhir.instance.model.api.IBaseResource");
			if(actionType!=null && actionType.equalsIgnoreCase("update")) {
				Object execPath = client.getClass().getMethod("update").invoke(client);
				Method resourceMethod = execPath.getClass().getMethod("resource", resClass);
				resourceMethod.setAccessible(true);
				execPath = resourceMethod.invoke(execPath, fhirResource);
				Method execMethod = execPath.getClass().getMethod("execute");
				execMethod.setAccessible(true);
				result = execMethod.invoke(execPath);
			}else {
				Object execPath = client.getClass().getMethod("create").invoke(client);
				Method execMethod = execPath.getClass().getMethod("resource", resClass);
				execMethod.setAccessible(true);
				Object execPath2 = execMethod.invoke(execPath, fhirResource);
				Method execMethod2 = execPath2.getClass().getMethod("execute");
				execMethod2.setAccessible(true);
				result = execMethod2.invoke(execPath2);
			}
			
			if (result != null) {
				Object resIdObj = result.getClass().getMethod("getId").invoke(result);
				resIdObj = resIdObj.getClass().getMethod("getIdPartAsLong").invoke(resIdObj);
				workItemResult.put("fhirResourceId", resIdObj.toString());
				manager.completeWorkItem(workItem.getId(), workItemResult);
			}
			return true;
		}
		return false;
	}

	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// not implemented on purpose
	}

}
