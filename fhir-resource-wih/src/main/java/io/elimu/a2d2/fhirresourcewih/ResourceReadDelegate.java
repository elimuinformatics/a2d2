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
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import io.elimu.a2d2.exception.FhirServerException;

public class ResourceReadDelegate implements WorkItemHandler {

	private static final String RESOURCE = "resource";
	private static final String R4 = "R4";
	private static final String STU3 = "STU3";
	private static final String DSTU2 = "DSTU2";
	private final FHIRDelegateHelper fhirdelegatehelper;
	private static final Logger log = LoggerFactory.getLogger(ResourceReadDelegate.class);

	public ResourceReadDelegate() {
		fhirdelegatehelper = new FHIRDelegateHelper();
	}

	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {

		URL fhirServer;
		try {

			fhirServer = new URL((String) workItem.getParameter("fhirServer"));

			String resourceType = (String) workItem.getParameter("resourceType");
			String resourceId = (String) workItem.getParameter("resourceId");
			String fhirVersion = (String) workItem.getParameter("fhirVersion");
			String authHeader = (String) workItem.getParameter("authHeader");

			if (resourceId == null) {
				log.error("Required input parameter missing: resourceId");
				throw new FhirServerException("Required input parameter missing");
			}

			FhirContext ctx = fhirdelegatehelper.getFhirContext(fhirVersion);

			IGenericClient client = fhirdelegatehelper.getFhirClient(ctx, fhirServer);

			if (client != null) {

				if (fhirdelegatehelper.authenticateFhirServer(client, authHeader)) {

					Map<String, Object> results = workItem.getResults();

					if (fhirVersion.equalsIgnoreCase(DSTU2)) {
						results.put(RESOURCE, ((ca.uhn.fhir.model.dstu2.resource.Bundle) client.search()
									.forResource(resourceType)
									.where(new StringClientParam("_id").matchesExactly().value(resourceId))
									.execute()).getEntryFirstRep().getResource());

					} else if (fhirVersion.equalsIgnoreCase(STU3)) {
						results.put(RESOURCE, ((org.hl7.fhir.dstu3.model.Bundle) client.search()
								.forResource(resourceType)
								.where(new TokenClientParam("_id").exactly().code(resourceId))
								.execute()).getEntryFirstRep().getResource());
					} else if (fhirVersion.equalsIgnoreCase(R4)) {
						results.put(RESOURCE, ((org.hl7.fhir.r4.model.Bundle) client.search()
								.forResource(resourceType)
								.where(new TokenClientParam("_id").exactly().code(resourceId))
								.execute()).getEntryFirstRep().getResource());
					}

					manager.completeWorkItem(workItem.getId(), results);

				} else {
					log.error("FHIR server unavailable");
					throw new FhirServerException("FHIR server unavailable");
				}
			} else {
				log.error("Incorrect server URL passed");
				throw new FhirServerException("Incorrect server URL passed");
			}
		}  catch (Exception ex) {
			log.error("ResourceReadDelegate is failed",ex);
			throw new FhirServerException(ex);
		}
	}

	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		manager.abortWorkItem(workItem.getId());
	}

}
