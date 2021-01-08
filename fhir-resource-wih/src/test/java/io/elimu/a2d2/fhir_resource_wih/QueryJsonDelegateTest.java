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

package io.elimu.a2d2.fhir_resource_wih;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.utils.KieHelper;
import io.elimu.a2d2.exception.WorkItemHandlerException;
import io.elimu.a2d2.fhirresourcewih.QueryJsonDelegate;

public class QueryJsonDelegateTest {
	private static WorkItemManager workItemManager;

	private static WorkItemHandler wih;

	@BeforeClass
	public static void initResourcesForTest() {
		KieHelper kieHelper = new KieHelper();
		KieBase kieBase = kieHelper.build();
		KieSession ksession = kieBase.newKieSession();
		wih = (WorkItemHandler) new QueryJsonDelegate();
		workItemManager = ksession.getWorkItemManager();
		workItemManager.registerWorkItemHandler("QueryJsonDelegate", wih);
	}

	@Test
	public void testQueryJsonDelegateWithDiscoveryService() {

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("json", "{\"services\":[{\"hook\":\"questionnaire\",\"title\":\"Questionnaire\",\"description\":\"Questionnaire Service\",\"id\":\"CHADSVascScoreCalc\",\"prefetch\":{\"patient\":\"Patient/{{Patient.id}}\"}},{\"hook\":\"medication-prescribe\",\"title\":\"Recommend hello-med\",\"description\":\"Returns hello world hook for medication-prescribe\",\"id\":\"hello-world-med-prescribe\",\"prefetch\":{\"patient\":\"Patient/{{context.patientId}}\"}},{\"hook\":\"patient-view\",\"title\":\"Recommend Option Grid\",\"description\":\"Returns relevant CDS Hooks cards with links to the option grid\",\"id\":\"option-grid\",\"prefetch\":{\"patient\":\"Patient/{{context.patientId}}\"}},{\"hook\":\"patient-view\",\"title\":\"Recommend Option Grid (new)\",\"description\":\"Returns relevant CDS Hooks cards with links to the option grid\",\"id\":\"option-grid-new\",\"prefetch\":{\"patient\":\"Patient/{{context.patientId}}\"}},{\"hook\":\"medication-prescribe\",\"title\":\"Test of Pharmacogenomics CDS service\",\"description\":\"Prep for the Jan 2019 connectathon\",\"id\":\"pgxtest\",\"prefetch\":{\"patient\":\"Patient/{{context.patientId}}\"}},{\"hook\":\"medication-prescribe\",\"title\":\"Elimu Medication Pharmacogenomics Service\",\"description\":\"Elimu Medication Pharmacogenomics Service\",\"id\":\"pharmacogenomics\",\"prefetch\":{\"patient\":\"Patient/{{Patient.id}}\"}},{\"hook\":\"patient-view\",\"title\":\"Recommend PRO\",\"description\":\"PRO Recommend Service\",\"id\":\"pro-recommend\",\"prefetch\":{\"patient\":\"Patient/{{Patient.id}}\"}},{\"hook\":\"patient-view\",\"title\":\"Recommend PRO\",\"description\":\"PRO Recommend Service\",\"id\":\"pro-recommend-new\",\"prefetch\":{\"patient\":\"Patient/{{Patient.id}}\"}},{\"hook\":\"questionnaire\",\"title\":\"Questionnaire\",\"description\":\"Questionnaire Service\",\"id\":\"questionnaire\",\"prefetch\":{\"patient\":\"Patient/{{Patient.id}}\"}}]}");
		execution.setParameter("query", "$..id");

		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNotNull(execution.getResult("result"));

	}

	@Test
	public void testQueryJsonDelegateWithCDSService() {

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("json", "{\"hook\":\"patient-view\",\"hookInstance\":\"d1577c69-dfbe-44ad-ba6d-3e05e953b2ea\","
				+ "\"fhirServer\":\"http://fhir3/baseDstu3\",\"user\":\"Practitioner/COREPRACTITIONER1\","
				+ "\"patient\":\"SMART-1288992\",\"context\":{\"proStartOffset\":1,\"patientId\":\"SMART-1288992\"},\"prefetch\":{}}");
		execution.setParameter("query", "$.hook");

		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNotNull(execution.getResult("result"));
		Assert.assertTrue(execution.getResult("result").toString().equals("\"patient-view\""));

	}

	@Test(expected=WorkItemHandlerException.class)
	public void testQueryJsonDelegateWithCDSServiceWithNullQuery() {

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("json", "{\"hook\":\"patient-view\",\"hookInstance\":\"d1577c69-dfbe-44ad-ba6d-3e05e953b2ea\","
				+ "\"fhirServer\":\"http://fhir3/baseDstu3\",\"user\":\"Practitioner/COREPRACTITIONER1\","
				+ "\"patient\":\"SMART-1288992\",\"context\":{\"proStartOffset\":1,\"patientId\":\"SMART-1288992\"},\"prefetch\":{}}");
		execution.setParameter("query", "");

		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNotNull(execution.getResult("result"));
		Assert.assertTrue(execution.getResult("result").toString().equals("\"patient-view\""));

	}

	@Test(expected=WorkItemHandlerException.class)
	public void testQueryJsonDelegateWithCDSServiceWithNullJson() {

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("json",null);
		execution.setParameter("query", "$.hook");

		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNotNull(execution.getResult("result"));
		Assert.assertTrue(execution.getResult("result").toString().equals("\"patient-view\""));

	}

	@Test(expected=WorkItemHandlerException.class)
	public void testQueryJsonDelegateWithCDSServiceWithjsonOnlyquotes() {

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("json","");
		execution.setParameter("query", "$.hook");

		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNotNull(execution.getResult("result"));
		Assert.assertTrue(execution.getResult("result").toString().equals("\"patient-view\""));

	}

	@Test(expected=WorkItemHandlerException.class)
	public void testQueryJsonDelegateWithCDSServiceWithIllegalQuery() {

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("json"
				, "{\"hook\":\"patient-view\",\"hookInstance\":\"d1577c69-dfbe-44ad-ba6d-3e05e953b2ea\","
				+ "\"fhirServer\":\"http://fhir3/baseDstu3\",\"user\":\"Practitioner/COREPRACTITIONER1\","
				+ "\"patient\":\"SMART-1288992\",\"context\":{\"proStartOffset\":1,\"patientId\":\"SMART-1288992\"},\"prefetch\":{}}");
		execution.setParameter("query", "$.fhir");

		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNull(execution.getResult("result"));

	}

}
