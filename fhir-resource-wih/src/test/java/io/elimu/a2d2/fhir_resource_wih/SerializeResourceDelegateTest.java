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
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.utils.KieHelper;

import ca.uhn.fhir.model.dstu2.resource.Patient;
import io.elimu.a2d2.exception.WorkItemHandlerException;
import io.elimu.a2d2.fhirresourcewih.SerializeResourceDelegate;

@Ignore ("disable for now as the test is based on public testing server")
public class SerializeResourceDelegateTest {

	private static WorkItemManager workItemManager;

	private static WorkItemHandler wih;
	
	private static final String FHIR2_URL="http://hapi.fhir.org/baseDstu2";

	@BeforeClass
	public static void initResourcesForTest() {
		KieHelper kieHelper = new KieHelper();
		KieBase kieBase = kieHelper.build();
		KieSession ksession = kieBase.newKieSession();
		wih = (WorkItemHandler) new SerializeResourceDelegate();
		workItemManager = ksession.getWorkItemManager();
		workItemManager.registerWorkItemHandler("SerializeResourceDelegate", wih);
	}

	@Test
	public void testFhir2JsonDelegate() {

		DelegateHelperTest.setUrl(FHIR2_URL);
		Patient patient = DelegateHelperTest.createFHIR2TestPatient();

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("fhirVersion", "FHIR2");
		execution.setParameter("outputType", "json");
		execution.setParameter("payload", patient);

		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNotNull(execution.getResult("output"));

		DelegateHelperTest.deleteResourceByPatient(patient.getId().getIdPart());

	}

	@Test(expected=WorkItemHandlerException.class)
	public void testFhir2JsonDelegatePayloadMissing() {

		DelegateHelperTest.setUrl(FHIR2_URL);
		Patient patient = DelegateHelperTest.createFHIR2TestPatient();

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("fhirVersion", "FHIR2");
		execution.setParameter("outputType", "json");

		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNotNull(execution.getResult("output"));

		DelegateHelperTest.deleteResourceByPatient(patient.getId().getIdPart());

	}

}
