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
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			fhirServer = new URL((String) workItem.getParameter("fhirServer"));

			String resourceType = (String) workItem.getParameter("resourceType");
			String resourceId = (String) workItem.getParameter("resourceId");
			String fhirVersion = (String) workItem.getParameter("fhirVersion");
			String authHeader = (String) workItem.getParameter("authHeader");

			if (resourceId == null) {
				log.error("Required input parameter missing: resourceId");
				throw new FhirServerException("Required input parameter missing");
			}

			Object ctx = fhirdelegatehelper.getFhirContext(fhirVersion);

			Object client = fhirdelegatehelper.getFhirClient(ctx, fhirServer);

			if (client != null) {

				if (fhirdelegatehelper.authenticateFhirServer(client, authHeader)) {

					Map<String, Object> results = workItem.getResults();

					if (fhirVersion.equalsIgnoreCase(DSTU2)) {
						Object searchObj = client.getClass().getMethod("search").invoke(client);
						Method forResourceMethod = searchObj.getClass().getMethod("forResource", String.class);
						forResourceMethod.setAccessible(true);
						Object forResource = forResourceMethod.invoke(searchObj, resourceType);
						Object stringClientParam = cl.loadClass("ca.uhn.fhir.rest.gclient.StringClientParam").getConstructor(String.class).newInstance("_id");
						Method matchesMethod = stringClientParam.getClass().getMethod("matchesExactly");
						matchesMethod.setAccessible(true);
						stringClientParam = matchesMethod.invoke(stringClientParam);
						Method valueMethod = stringClientParam.getClass().getMethod("value", String.class);
						valueMethod.setAccessible(true);
						stringClientParam = valueMethod.invoke(stringClientParam, resourceId);
						Method whereMethod = forResource.getClass().getMethod("where", cl.loadClass("ca.uhn.fhir.rest.gclient.ICriterion"));
						whereMethod.setAccessible(true);
						Object whereObj = whereMethod.invoke(forResource, stringClientParam);
						Method execMethod = whereObj.getClass().getMethod("execute");
						execMethod.setAccessible(true);
						Object execObj = execMethod.invoke(whereObj);
						Object entryObj = execObj.getClass().getMethod("getEntryFirstRep").invoke(execObj);
						results.put(RESOURCE, entryObj.getClass().getMethod("getResource").invoke(entryObj));
					} else if (fhirVersion.equalsIgnoreCase(STU3) || fhirVersion.equalsIgnoreCase(R4)) {
						Object searchObj = client.getClass().getMethod("search").invoke(client);
						Method forResourceMethod = searchObj.getClass().getMethod("forResource", String.class);
						forResourceMethod.setAccessible(true);
						Object forResource = forResourceMethod.invoke(searchObj, resourceType);
						Object tokenClientParam = cl.loadClass("ca.uhn.fhir.rest.gclient.TokenClientParam").getConstructor(String.class).newInstance("_id");
						Method exactlyMethod = tokenClientParam.getClass().getMethod("exactly");
						exactlyMethod.setAccessible(true);
						tokenClientParam = exactlyMethod.invoke(tokenClientParam);
						Method codeMethod = tokenClientParam.getClass().getMethod("code", String.class);
						codeMethod.setAccessible(true);
						tokenClientParam = codeMethod.invoke(tokenClientParam, resourceId);
						Method whereMethod = forResource.getClass().getMethod("where", cl.loadClass("ca.uhn.fhir.rest.gclient.ICriterion"));
						whereMethod.setAccessible(true);
						Object whereObj = whereMethod.invoke(forResource, tokenClientParam);
						Method execMethod = whereObj.getClass().getMethod("execute");
						execMethod.setAccessible(true);
						Object execObj = execMethod.invoke(whereObj);
						Object entryObj = execObj.getClass().getMethod("getEntryFirstRep").invoke(execObj);
						results.put(RESOURCE, entryObj.getClass().getMethod("getResource").invoke(entryObj));
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
