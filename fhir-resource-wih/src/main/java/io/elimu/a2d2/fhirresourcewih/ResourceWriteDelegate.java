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

import java.net.URL;
import java.util.Map;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import io.elimu.a2d2.exception.FhirServerException;
import io.elimu.a2d2.parsing.FhirParseUtil;

public class ResourceWriteDelegate implements WorkItemHandler {

	FHIRDelegateHelper fhirdelegatehelper = null;
	private static final Logger log = LoggerFactory.getLogger(ResourceWriteDelegate.class);
	
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
			IBaseResource fhirResource = getFhirResource(oResource);

			FhirContext ctx = fhirdelegatehelper.getFhirContext(fhirType);
			if (ctx != null) {
				IGenericClient client = fhirdelegatehelper.getFhirClient(ctx, serverUrl);
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
	
	private IBaseResource getFhirResource(Object oResource) {
		if (oResource instanceof IBaseResource) {
			return (IBaseResource) oResource;
		} else if (oResource instanceof String && ((String) oResource).isEmpty()) {
			log.error("Resource is empty or not a String");
			throw new FhirServerException(MISSING_RSRC);
		}
		return null;
	}
	
	private boolean completeworkItem(IGenericClient client, IBaseResource fhirResource,
			Map<String, Object> workItemResult, WorkItemManager manager, WorkItem workItem, String authHeader,String actionType) {
		if (fhirdelegatehelper.authenticateFhirServer(client, authHeader)) {
			MethodOutcome result = null;
			if(actionType!=null && actionType.equalsIgnoreCase("update")) {
				result = client.update().resource(fhirResource).execute();
			}else {
				result = client.create().resource(fhirResource).execute();
			}
			
			if (result != null) {
				workItemResult.put("fhirResourceId", result.getId().getIdPartAsLong().toString());
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
