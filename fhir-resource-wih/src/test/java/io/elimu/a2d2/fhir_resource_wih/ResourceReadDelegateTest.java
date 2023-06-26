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
import org.hl7.fhir.r4.model.codesystems.AdministrativeGender;
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
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import io.elimu.a2d2.exception.FhirServerException;
import io.elimu.a2d2.fhirresourcewih.ResourceReadDelegate;

@Ignore ("disable for now as the test is based on public testing server")
public class ResourceReadDelegateTest {

	private static final String fhirUrl = "http://hapi.fhir.org/baseDstu2";
	private static final String fhirUrl4 = "http://hapi.fhir.org/baseR4";

	private static WorkItemManager workItemManager;

	private static WorkItemHandler wih;

	@BeforeClass
	public static void initResourcesForTest() {
		KieHelper kieHelper = new KieHelper();
		KieBase kieBase = kieHelper.build();
		KieSession ksession = kieBase.newKieSession();
		wih = (WorkItemHandler) new ResourceReadDelegate();
		workItemManager = ksession.getWorkItemManager();
		workItemManager.registerWorkItemHandler("ResourceReadDelegate", wih);

	}

	@Test
	public void testFHIR4ResourceReadDelegate() {

		String patientId = "2162201";
		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("fhirServer", fhirUrl4);
		execution.setParameter("resourceType", "Patient");
		execution.setParameter("resourceId", patientId);
		execution.setParameter("fhirVersion", "DSTU2");

		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNotNull(execution.getResult("resource"));
	}
	
	@Test
	public void testFHIRResourceReadDelegate() {

		DelegateHelperTest.setUrl(fhirUrl);
		Patient patient = DelegateHelperTest.createFHIR2TestPatient();
		String patientId = patient.getId().getIdPart();

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("fhirServer", fhirUrl);
		execution.setParameter("resourceType", patient.getResourceName());
		execution.setParameter("resourceId", patientId);
		execution.setParameter("fhirVersion", "R4");

		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNotNull(execution.getResult("resource"));

		DelegateHelperTest.deleteResourceByPatient(patientId);

	}
	

	@Test(expected = FhirServerException.class)
	public void testFHIRResourceReadDelegateResourceTypeMissing() {

		DelegateHelperTest.setUrl(fhirUrl);
		Patient patient = DelegateHelperTest.createFHIR2TestPatient();
		String patientId = patient.getId().getIdPart();

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("fhirServer", fhirUrl);
		execution.setParameter("fhirVersion", "DSTU2");

		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNotNull(execution.getResult("resource"));

		DelegateHelperTest.deleteResourceByPatient(patientId);

	}

}
