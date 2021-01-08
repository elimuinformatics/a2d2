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
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.utils.KieHelper;
import io.elimu.a2d2.corewih.ServiceResponseDelegate;
import io.elimu.a2d2.genericmodel.ServiceResponse;

public class ServiceResponseDelegateTest {

	private static WorkItemManager workItemManager;

	private static WorkItemHandler wih;

	@BeforeClass
	public static void initResourcesForTest() {
		KieHelper kieHelper = new KieHelper();
		KieBase kieBase = kieHelper.build();
		KieSession ksession = kieBase.newKieSession();
		wih = (WorkItemHandler) new ServiceResponseDelegate();
		workItemManager = ksession.getWorkItemManager();
		workItemManager.registerWorkItemHandler("ServiceResponseDelegate", wih);
	}

	@Test
	public void testServiceResponseDelegate() {
		ServiceResponse serviceResponse =new ServiceResponse();
		String responseBody="{\"cards\":[{\"summary\":\"PRO for depression\",\"detail\":\"Patient reported outcomes that are recommended to be completed by patients at risk for depression\",\"indicator\":\"info\",\"links\":[{\"label\":\"SC CTSI\",\"url\":\"https://research.usc.edu/translational-research-clinical-trials/\",\"type\":\"absolute\"}],\"suggestions\":[{\"label\":\"Order PRO\",\"uuid\":\"858d8dc0-8686-48d8-bccd-734e707deda2\",\"actions\":[{\"description\":\"Create a Procedure Request\",\"type\":\"create\",\"resource\":{\"resource\":{\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"1234567\",\"display\":\"Code from Boston Childrens\"}],\"text\":\"PROMIS Bank v1.0 - Depression\"},\"performer\":{\"reference\":\"Patient/SMART-1288992\"},\"subject\":{\"reference\":\"Patient/SMART-1288992\"},\"occurrenceDateTime\":\"2018-10-22T12:25:45+00:00\",\"reasonCode\":[{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"35489007\",\"display\":\"Depressive disorder (disorder)\"}]}],\"category\":[{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"386053000\",\"display\":\"Evaluation procedure (procedure)\"}],\"text\":\"Evaluation\"}],\"resourceType\":\"ProcedureRequest\",\"status\":\"draft\"},\"description\":\"Create a Procedure Request\",\"type\":\"create\"}}]}],\"source\":{\"label\":\"http://usc.edu\",\"url\":\"https://example.com\"}},{\"summary\":\"PRO series for patients with elective hip replacement\",\"detail\":\"Series of patient reported outcomes that are recommended to be completed by patients undergoing hip replacement\",\"indicator\":\"info\",\"links\":[{\"label\":\"SC CTSI\",\"url\":\"https://research.usc.edu/translational-research-clinical-trials/\",\"type\":\"absolute\"}],\"suggestions\":[{\"label\":\"Order PRO series\",\"uuid\":\"858d8dc0-8686-48d8-bccd-734e707deda2\",\"actions\":[{\"description\":\"Create a Procedure Request with the series\",\"type\":\"create\",\"resource\":{\"resource\":{\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"1234567\",\"display\":\"Code from Boston Childrens\"}],\"text\":\"CJR - Hip replacement series\"},\"performer\":{\"reference\":\"Patient/SMART-1288992\"},\"subject\":{\"reference\":\"Patient/SMART-1288992\"},\"occurrenceDateTime\":\"2018-10-22T12:25:45+00:00\",\"reasonCode\":[{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"52734007\",\"display\":\"Total replacement of hip (procedure)\"}]}],\"category\":[{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"386053000\",\"display\":\"Evaluation procedure (procedure)\"}],\"text\":\"Evaluation\"}],\"resourceType\":\"ProcedureRequest\",\"status\":\"draft\"},\"description\":\"Create a Procedure Request with the series\",\"type\":\"create\"}}]}],\"source\":{\"label\":\"http://usc.edu\",\"url\":\"https://example.com\"}}]}";

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("serviceResponse", serviceResponse);
		execution.setParameter("body", responseBody);
		execution.setParameter("responseCode", 200);
		execution.setParameter("header_value_Content__Type", "application/json");
		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNotNull(execution.getResult("serviceResponse"));
	}


	@Test
	public void testServiceResponseMissingBodyDelegate() {
		ServiceResponse serviceResponse =new ServiceResponse();

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("serviceResponse", serviceResponse);
		execution.setParameter("responseCode", 200);
		execution.setParameter("header_value_Content__Type", "application/json");

		wih.executeWorkItem(execution, workItemManager);
		serviceResponse = (ServiceResponse) execution.getResult("serviceResponse");

		Assert.assertNull(serviceResponse.getBody());
	}

	@Test
	public void testServiceResponseMissingServiceResponseDelegate() {
		String responseBody="{\"cards\":[{\"summary\":\"PRO for depression\",\"detail\":\"Patient reported outcomes that are recommended to be completed by patients at risk for depression\",\"indicator\":\"info\",\"links\":[{\"label\":\"SC CTSI\",\"url\":\"https://research.usc.edu/translational-research-clinical-trials/\",\"type\":\"absolute\"}],\"suggestions\":[{\"label\":\"Order PRO\",\"uuid\":\"858d8dc0-8686-48d8-bccd-734e707deda2\",\"actions\":[{\"description\":\"Create a Procedure Request\",\"type\":\"create\",\"resource\":{\"resource\":{\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"1234567\",\"display\":\"Code from Boston Childrens\"}],\"text\":\"PROMIS Bank v1.0 - Depression\"},\"performer\":{\"reference\":\"Patient/SMART-1288992\"},\"subject\":{\"reference\":\"Patient/SMART-1288992\"},\"occurrenceDateTime\":\"2018-10-22T12:25:45+00:00\",\"reasonCode\":[{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"35489007\",\"display\":\"Depressive disorder (disorder)\"}]}],\"category\":[{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"386053000\",\"display\":\"Evaluation procedure (procedure)\"}],\"text\":\"Evaluation\"}],\"resourceType\":\"ProcedureRequest\",\"status\":\"draft\"},\"description\":\"Create a Procedure Request\",\"type\":\"create\"}}]}],\"source\":{\"label\":\"http://usc.edu\",\"url\":\"https://example.com\"}},{\"summary\":\"PRO series for patients with elective hip replacement\",\"detail\":\"Series of patient reported outcomes that are recommended to be completed by patients undergoing hip replacement\",\"indicator\":\"info\",\"links\":[{\"label\":\"SC CTSI\",\"url\":\"https://research.usc.edu/translational-research-clinical-trials/\",\"type\":\"absolute\"}],\"suggestions\":[{\"label\":\"Order PRO series\",\"uuid\":\"858d8dc0-8686-48d8-bccd-734e707deda2\",\"actions\":[{\"description\":\"Create a Procedure Request with the series\",\"type\":\"create\",\"resource\":{\"resource\":{\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"1234567\",\"display\":\"Code from Boston Childrens\"}],\"text\":\"CJR - Hip replacement series\"},\"performer\":{\"reference\":\"Patient/SMART-1288992\"},\"subject\":{\"reference\":\"Patient/SMART-1288992\"},\"occurrenceDateTime\":\"2018-10-22T12:25:45+00:00\",\"reasonCode\":[{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"52734007\",\"display\":\"Total replacement of hip (procedure)\"}]}],\"category\":[{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"386053000\",\"display\":\"Evaluation procedure (procedure)\"}],\"text\":\"Evaluation\"}],\"resourceType\":\"ProcedureRequest\",\"status\":\"draft\"},\"description\":\"Create a Procedure Request with the series\",\"type\":\"create\"}}]}],\"source\":{\"label\":\"http://usc.edu\",\"url\":\"https://example.com\"}}]}";

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("responseCode", 200);
		execution.setParameter("header_value_Content__Type", "application/json");
		execution.setParameter("body", responseBody);

		wih.executeWorkItem(execution, workItemManager);
		ServiceResponse serviceResponse = (ServiceResponse) execution.getResult("serviceResponse");

		Assert.assertNotNull(serviceResponse.getBody());
		Assert.assertNotNull(serviceResponse.getHeader("Content-Type"));
		Assert.assertNotNull(serviceResponse.getResponseCode());
	}
	
	@Test
	public void testRemoveMethodDemo() {
		String header = "header_value_Content__Type";
		String headerNoChange = "header_value_Content";
		String headerAttached = "header_value____Content__Type";
		String headerHead = "__header_value-Content__Type";
		String headerTail = "header_value-Content__Type__";

		Assert.assertEquals(header.replace("__", "-"), "header_value_Content-Type");
		Assert.assertEquals(headerNoChange.replace("__", "-"), "header_value_Content");
		Assert.assertEquals(headerAttached.replace("__", "-"), "header_value--Content-Type");
		Assert.assertEquals(headerHead.replace("__", "-"), "-header_value-Content-Type");
		Assert.assertEquals(headerTail.replace("__", "-"), "header_value-Content-Type-");
		
	}
}
