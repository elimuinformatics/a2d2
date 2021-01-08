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

package io.elimu.a2d2.core_wih;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.utils.KieHelper;
import io.elimu.a2d2.corewih.ServiceRequestDelegate;
import io.elimu.a2d2.exception.WorkItemHandlerException;
import io.elimu.a2d2.genericmodel.ServiceRequest;

public class ServiceRequestDelegateTest {

	private static WorkItemManager workItemManager;

	private static WorkItemHandler wih;

	private static String requestBody;
	private static String user ;
	private static String method ;
	private static String path ;
	private static Map<String, List<String>> headermap = new HashMap<String, List<String>>();
	private static Map<String, List<String>> parammap = new HashMap<String, List<String>>();

	@BeforeClass
	public static void initResourcesForTest() {
		KieHelper kieHelper = new KieHelper();
		KieBase kieBase = kieHelper.build();
		KieSession ksession = kieBase.newKieSession();
		wih = (WorkItemHandler) new ServiceRequestDelegate();
		workItemManager = ksession.getWorkItemManager();
		workItemManager.registerWorkItemHandler("ServiceRequestDelegate", wih);

		requestBody = "{\"hook\":\"patient-view\",\"hookInstance\":\"d1577c69-dfbe-44ad-ba6d-3e05e953b2ea\",\"fhirServer\":\"http://hapi.fhir.org/baseDstu3\",\"user\":\"Practitioner/COREPRACTITIONER1\",\"patient\":\"SMART-1288992\",\"context\":{\"patientId\":\"SMART-1288992\"},\"prefetch\":{\"patient\":{\"resourceType\":\"Patient\",\"gender\":\"male\",\"birthDate\":\"1925-12-23\",\"id\":\"SMART-1288992\",\"active\":true}}}";
		path = "/api/v1/cds-services/pro-recommend";
		user = "Practitioner";
		method = "GET";
		List<String> headerlist = new ArrayList<>();
		headerlist.add("application/json");
		headermap.put("Content-Type", headerlist);

		List<String> paramlist = new ArrayList<>();
		paramlist.add("459");
		paramlist.add("603");
		parammap.put("Observation", paramlist);
	}

	@Test
	public void testServiceRequestDelegate() {
		ServiceRequest serviceRequest = new ServiceRequest();
		serviceRequest.setBody(requestBody);
		serviceRequest.setPath(path);
		serviceRequest.setUser(user);
		serviceRequest.setMethod(method);
		serviceRequest.setHeaders(headermap);
		serviceRequest.setParams(parammap);
		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("serviceRequest", serviceRequest);
		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNotNull(execution.getResult("body"));
		Assert.assertNotNull(execution.getResult("path"));
		Assert.assertNotNull(execution.getResult("user"));
		Assert.assertNotNull(execution.getResult("method"));
		Assert.assertNotNull(execution.getResult("header_value_Content__Type"));
		Assert.assertNotNull(execution.getResult("param_values_Observation"));
	}

	@Test(expected = WorkItemHandlerException.class)
	public void testServiceRequestMissingServiceRequestDelegate() {
		WorkItemImpl execution = new WorkItemImpl();
		wih.executeWorkItem(execution, workItemManager);
	}

	@Test(expected = WorkItemHandlerException.class)
	public void testServiceRequestTypeChangeServiceRequestDelegate() {
		WorkItemImpl execution = new WorkItemImpl();
		wih.executeWorkItem(execution, workItemManager);
		execution.setParameter("serviceRequest", "StringVariable");
	}

	@Test
	public void testServiceRequestMissingBodyDelegate() {
		ServiceRequest serviceRequest = new ServiceRequest();
		serviceRequest.setPath(path);
		serviceRequest.setUser(user);
		serviceRequest.setMethod(method);
		serviceRequest.setHeaders(headermap);
		serviceRequest.setParams(parammap);
		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("serviceRequest", serviceRequest);
		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNull(execution.getResult("body"));
		Assert.assertNotNull(execution.getResult("path"));
		Assert.assertNotNull(execution.getResult("user"));
		Assert.assertNotNull(execution.getResult("method"));
		Assert.assertNotNull(execution.getResult("header_value_Content__Type"));
		Assert.assertNotNull(execution.getResult("param_values_Observation"));
	}
	@Test
	public void testServiceRequestMissingPathDelegate() {
		ServiceRequest serviceRequest = new ServiceRequest();
		serviceRequest.setBody(requestBody);
		serviceRequest.setUser(user);
		serviceRequest.setMethod(method);
		serviceRequest.setHeaders(headermap);
		serviceRequest.setParams(parammap);
		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("serviceRequest", serviceRequest);
		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNotNull(execution.getResult("body"));
		Assert.assertNull(execution.getResult("path"));
		Assert.assertNotNull(execution.getResult("user"));
		Assert.assertNotNull(execution.getResult("method"));
		Assert.assertNotNull(execution.getResult("header_value_Content__Type"));
		Assert.assertNotNull(execution.getResult("param_values_Observation"));
	}

	@Test
	public void testServiceRequestMissinguserDelegate() {
		ServiceRequest serviceRequest = new ServiceRequest();
		serviceRequest.setBody(requestBody);
		serviceRequest.setPath(path);
		serviceRequest.setMethod(method);
		serviceRequest.setHeaders(headermap);
		serviceRequest.setParams(parammap);
		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("serviceRequest", serviceRequest);
		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNotNull(execution.getResult("body"));
		Assert.assertNotNull(execution.getResult("path"));
		Assert.assertNull(execution.getResult("user"));
		Assert.assertNotNull(execution.getResult("method"));
		Assert.assertNotNull(execution.getResult("header_value_Content__Type"));
		Assert.assertNotNull(execution.getResult("param_values_Observation"));
	}

}
