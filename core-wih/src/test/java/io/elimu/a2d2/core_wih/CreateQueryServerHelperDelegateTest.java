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

/*import org.drools.core.process.instance.impl.WorkItemImpl;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.utils.KieHelper;
import io.elimu.a2d2.cds.fhir.helper.r4.QueryingServerHelper;
import io.elimu.a2d2.corewih.CreateQueryServerHelperDelegate;
import io.elimu.a2d2.exception.WorkItemHandlerException;
*/
public class CreateQueryServerHelperDelegateTest {

/*	private static WorkItemManager workItemManager;

	private static WorkItemHandler wih;

	@BeforeClass
	public static void initResourcesForTest() {
		KieHelper kieHelper = new KieHelper();
		KieBase kieBase = kieHelper.build();
		KieSession ksession = kieBase.newKieSession();
		wih = (WorkItemHandler) new CreateQueryServerHelperDelegate();
		workItemManager = ksession.getWorkItemManager();
		workItemManager.registerWorkItemHandler("CreateQueryServerHelperDelegate", wih);
	}

	@Test
	public void testCreateQueryServerHelperDelegate() {
		String baseURL = "https://fhir2/baseDstu2/";
		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("token", "b21uaWJ1czpvbW5pYnVz");
		execution.setParameter("baseUrl", baseURL);
		execution.setParameter("fhirVersion", "FHIR2");
		wih.executeWorkItem(execution, workItemManager);
		Assert.assertNotNull(execution.getResult("helper"));
		Assert.assertNotNull(((QueryingServerHelper) execution.getResult("helper")).getFhirUrl());
	}

	@Test(expected = WorkItemHandlerException.class)
	public void testCreateQueryServerHelperDelegateFhirURLMissing() {
		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("token", "b21uaWJ1czpvbW5pYnVz");
		execution.setParameter("fhirVersion", "FHIR2");
		wih.executeWorkItem(execution, workItemManager);
	}

	@Test
	public void testCreateQueryServerHelperDelegateFHIRversionMissing() {
		String baseURL = "https://fhir2/baseDstu2/";
		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("token", "b21uaWJ1czpvbW5pYnVz");
		execution.setParameter("baseUrl", baseURL);
		wih.executeWorkItem(execution, workItemManager);
		Assert.assertNotNull(execution.getResult("helper"));
		Assert.assertNotNull(((QueryingServerHelper) execution.getResult("helper")).getFhirUrl());
	}

	@Test
	public void testCreateQueryServerHelperDelegateTokenMissing() {
		String baseURL = "https://fhir2/baseDstu2/";
		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("baseUrl", baseURL);
		execution.setParameter("fhirVersion", "FHIR2");
		wih.executeWorkItem(execution, workItemManager);
		Assert.assertNotNull(execution.getResult("helper"));
		Assert.assertNotNull(((QueryingServerHelper) execution.getResult("helper")).getFhirUrl());
	}
*/
}
