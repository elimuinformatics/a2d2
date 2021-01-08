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

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.utils.KieHelper;

import io.elimu.a2d2.corewih.ServiceRestAPIDelegate;
import io.elimu.a2d2.corewih.ServiceSoapAPIDelegate;
import io.elimu.a2d2.exception.WorkItemHandlerException;
import io.elimu.a2d2.genericmodel.ClientRequest;
import io.elimu.a2d2.genericmodel.ServiceResponse;

public class ServiceRestAPIDelegateTest {

	private static WorkItemManager workItemManager;
	private static WorkItemHandler wih ;
	private static final String SERVICE_RESPONSE = "serviceResponse";

	@BeforeClass
	public static void initResourcesForTest() {

		KieHelper kieHelper = new KieHelper();
		KieBase kieBase = kieHelper.build();
		KieSession ksession = kieBase.newKieSession();
		wih = (WorkItemHandler) new ServiceRestAPIDelegate();
		workItemManager = ksession.getWorkItemManager();
		workItemManager.registerWorkItemHandler("ServiceRestAPIDelegate", wih);
	}
	
	@Test
	public void testGetAPIWithParameters() {

		wih = (WorkItemHandler) new ServiceRestAPIDelegate();
		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("url", "https://httpbin.org/get");
		execution.setParameter("header_value_header1", "header_val");

		execution.setParameter("param_value_param1", "test1");
		execution.setParameter("param_value_param2", "test2");
		execution.setParameter("method", "GET");

		wih.executeWorkItem(execution, workItemManager);
		ServiceResponse serviceResponse = (ServiceResponse) execution.getResult(SERVICE_RESPONSE);
		Assert.assertNotNull(serviceResponse);
		Assert.assertNotNull(serviceResponse.getBody());
	}

	@Test
	public void testGetAPIStrictRedirect() {
		wih = (WorkItemHandler) new ServiceRestAPIDelegate();
		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("url", "https://google.com");
		execution.setParameter("method", "GET");
		execution.setParameter("strictRedirect", "true");
		wih.executeWorkItem(execution, workItemManager);
		ServiceResponse resp = (ServiceResponse) execution.getResult(SERVICE_RESPONSE);
		
		Assert.assertNotNull(resp);
		Assert.assertEquals(200, resp.getResponseCode().intValue());
		Assert.assertNotNull(resp.getBody());
	}
	
	@Test
	public void testGetAPILaxRedirect() {
		wih = (WorkItemHandler) new ServiceRestAPIDelegate();
		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("url", "https://google.com");
		execution.setParameter("method", "GET");
		wih.executeWorkItem(execution, workItemManager);
		ServiceResponse resp = (ServiceResponse) execution.getResult(SERVICE_RESPONSE);
		
		Assert.assertNotNull(resp);
		Assert.assertEquals(200, resp.getResponseCode().intValue());
		Assert.assertNotNull(resp.getBody());
	}
	
	@Test
	public void testGetAPIWithStandardCookie() {

		wih = (WorkItemHandler) new ServiceRestAPIDelegate();
		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("url", "https://api.ncbi.nlm.nih.gov/variation/v0/spdi/NC_000012.11:21382187:G:GAGTCCAC/all_equivalent_contextual");
		execution.setParameter("method", "GET");

		wih.executeWorkItem(execution, workItemManager);
		ServiceResponse serviceResponse = (ServiceResponse) execution.getResult(SERVICE_RESPONSE);
		
		Assert.assertNotNull(serviceResponse);
		Assert.assertEquals(new Integer(200), serviceResponse.getResponseCode());
		Assert.assertNotNull(serviceResponse.getBody());
	}

	
	@Test(expected = WorkItemHandlerException.class)
	public void testMissingMethodParameters() {

		wih = (WorkItemHandler) new ServiceRestAPIDelegate();
		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("url", "https://httpbin.org/get");
		execution.setParameter("header_value_header1", "header_val");
		execution.setParameter("param_value_param1", "test1");
		wih.executeWorkItem(execution, workItemManager);
	}

	@Test
	public void testPostAPIWithBodyEncoding() {
		wih = (WorkItemHandler) new ServiceRestAPIDelegate();

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("url", "https://httpbin.org/post");
		execution.setParameter("header_value_header1", "header_val");
		execution.setParameter("body_value_name", "test");
		execution.setParameter("body_value_salary", "123");
		execution.setParameter("body_value_age", "23");

		execution.setParameter("method", "POST");
		execution.setParameter("bodyEncoding", "x-www-form-urlencoded");


		wih.executeWorkItem(execution, workItemManager);
		ServiceResponse serviceResponse = (ServiceResponse) execution.getResult(SERVICE_RESPONSE);
		Assert.assertNotNull(serviceResponse);
		Assert.assertNotNull(serviceResponse.getBody());
		Assert.assertTrue(serviceResponse.getResponseCode().equals(200));
	}

	@Test
	public void testPostAPIWithRaw() {
		wih = (WorkItemHandler) new ServiceRestAPIDelegate();

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("url", "https://httpbin.org/post");
		execution.setParameter("header_value_header1", "header_val");
		execution.setParameter("body", "{\"name\":\"test\",\"salary\":\"123\",\"age\":\"23\",\"id\":\"719\"}");

		execution.setParameter("method", "POST");
		execution.setParameter("bodyEncoding", "raw");

		wih.executeWorkItem(execution, workItemManager);
		ServiceResponse serviceResponse = (ServiceResponse) execution.getResult(SERVICE_RESPONSE);
		Assert.assertNotNull(serviceResponse);
		Assert.assertNotNull(serviceResponse.getBody());
		Assert.assertTrue(serviceResponse.getResponseCode().equals(200));

	}

	@Test
	public void testEncodedBodyPostAPI() {
		wih = (WorkItemHandler) new ServiceRestAPIDelegate();

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("url", "https://httpbin.org/post");
		execution.setParameter("body_value_client_secret", "abcdef");
		execution.setParameter("body_value_grant_type", "refresh_token");
		execution.setParameter("body_value_ttl", "60");
		execution.setParameter("body_value_client_id", "zyxwvu");
		execution.setParameter("body_value_refresh_token", "qqqwwwweeerrrtttyy");

		execution.setParameter("method", "POST");
		execution.setParameter("bodyEncoding", "x-www-form-urlencoded");

		wih.executeWorkItem(execution, workItemManager);
		ServiceResponse serviceResponse = (ServiceResponse) execution.getResult(SERVICE_RESPONSE);
		Assert.assertNotNull(serviceResponse);
		Assert.assertNotNull(serviceResponse.getBody());
		Assert.assertTrue(serviceResponse.getResponseCode().equals(200));

	}

	@Test
	public void testBearerToken() {
		wih = (WorkItemHandler) new ServiceRestAPIDelegate();

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("url", "https://httpbin.org/get");
		execution.setParameter("auth_bearertoken", "InJfcXVlc3R5uYWlyZWNsaWVudCIsInJfcXVlc3Rpb25uYWlyZXNoYXJlZCIsInJfaGVhbHRoZGF0YSIsInJfZml0bmVzc2RhdGEiLCJyX2xpZmVzdHlsZSIsInJfdXNlcmluZm9iYXNpYyIsInJfdXNlcmluZm9jb250YWN0Il0sInN1YiI6ImEwOWM4alowVzBpSmFheVM1V29GaVEiLCJ1bmlxdWVfbmFtZSI6ImEwOWM4alowVzBpSmFheVM1V29GaVEiLCJjbGllbnQiOiI0VnNXUWsxS2gwcUZUNmxJWlhzaW5RIiwidHdvZmFjdG9yIjoiZmFsc2UiLCJpc3MiOiJodHRwczovL2hpZ2ktcWEtY29yZS1hdXRoZW50aWNhdGlvbi5henVyZXdlYnNpdGVzLm5ldCIsImF1ZCI6ImVlZjc4YmVhN2M4OTQyOGU5OGRiNmFkMmNkY2ZkNzc1IiwiZXhwIjoxNTQ3NzU3NTcxLCJuYmYiOjE1NDc3NTM5NzF9.LRDMJ2K6hIGzVwXgDkybm3eaTGLEM2nrL4rxIV5wldo");
		execution.setParameter("param_value_categories", "Health");
		execution.setParameter("param_value_startTime", "2018-01-01T13:13:20-06:00Z");
		execution.setParameter("header_value_clientId", "abcdef");
		execution.setParameter("header_value_accept", "application/xml+ihmi");

		execution.setParameter("method", "GET");

		wih.executeWorkItem(execution, workItemManager);
		ServiceResponse serviceResponse = (ServiceResponse) execution.getResult(SERVICE_RESPONSE);
		Assert.assertNotNull(serviceResponse);
		Assert.assertNotNull(serviceResponse.getBody());
		Assert.assertTrue(serviceResponse.getResponseCode().equals(200));
	}
	
	@Test
	@Ignore
	public void testStatusCode() {

		WorkItemImpl execution = new WorkItemImpl();
		ClientRequest clientRequest =new ClientRequest();
		clientRequest.setUrl("http://hapi.fhir.org/baseDstu2/Patient");
		clientRequest.setMethod("GET");
		execution.setParameter("method", "GET");
		execution.setParameter("clientRequest", clientRequest);

		wih.executeWorkItem(execution, workItemManager);
		ServiceResponse serviceResponse = (ServiceResponse) execution.getResult(SERVICE_RESPONSE);
		Assert.assertNotNull(serviceResponse);
		Assert.assertNotNull(serviceResponse.getBody());
		Assert.assertTrue(serviceResponse.getResponseCode().equals(200));
	}
	
	
}
