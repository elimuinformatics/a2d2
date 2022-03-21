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

import java.net.URL;

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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.impl.BaseClient;
import io.elimu.a2d2.exception.FhirServerException;
import io.elimu.a2d2.fhirresourcewih.FHIRDelegateHelper;
import io.elimu.a2d2.fhirresourcewih.ResourceWriteDelegate;

//@Ignore ("disable for now as the test is based on public testing server")
public class ResourceWriteDelegateTest {

	private static WorkItemManager workItemManager;

	private static WorkItemHandler wih;

	private static String resource;
	
	private static String updatedResource;

	private static IGenericClient client;
	
	private static final String FHIR2_URL="http://hapi.fhir.org/baseDstu2";

	@BeforeClass
	public static void initResourcesForTest() {

		resource = "{\"resourceType\":\"Patient\",\"text\":{\"status\":\"generated\",\"div\":\"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\"><p><b>Patient</b></p><p><b>Name</b>: Timonen, Kimmo</p><p><b>DOB</b>: 1975-03-18</p><p><b>Sex</b>: Male</p><p><b>Status</b>: Active</p></div>\"},\"identifier\":[{\"use\":\"usual\",\"type\":{\"coding\":[{\"system\":\"http://hl7.org/fhir/v2/0203\",\"code\":\"MR\",\"display\":\"Medical record number\",\"userSelected\":false}],\"text\":\"MRN\"},\"system\":\"urn:oid:1.1.1.1.1.1\",\"value\":\"10002805\",\"period\":{\"start\":\"2017-02-02T17:17:23.000Z\"}}],\"active\":true,\"name\":[{\"use\":\"official\",\"text\":\"Timonen, Kimmo\",\"family\":[\"Television\"],\"given\":[\"Kimmo\"],\"prefix\":[\"Mr.\"],\"period\":{\"start\":\"2017-02-02T17:17:29.000Z\"}}],\"gender\":\"Male\",\"birthDate\":\"1500-01-01\",\"communication\":[{\"language\":{\"text\":\"English\"}}]}";

		FhirContext ctx = FhirContext.forDstu2();
		client = ctx.newRestfulGenericClient(FHIR2_URL);
		client.setEncoding(EncodingEnum.JSON);
		((BaseClient) client).setDontValidateConformance(true);

		KieHelper kieHelper = new KieHelper();
		KieBase kieBase = kieHelper.build();
		KieSession ksession = kieBase.newKieSession();
		wih = (WorkItemHandler) new ResourceWriteDelegate();
		workItemManager = ksession.getWorkItemManager();

		workItemManager.registerWorkItemHandler("ResourceWriteDelegate", wih);

	}

	@Test
	public void testDelegatehelper() throws Exception {
		FHIRDelegateHelper helper = new FHIRDelegateHelper();
		Object ctx = helper.getFhirContext("R4");
		helper.getFhirClient(ctx, new URL(FHIR2_URL));
	}

	@Test
	public void testFHIRAddDelegate() {

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("fhirVersion", "DSTU2");
		execution.setParameter("fhirServer", FHIR2_URL);
		execution.setParameter("authHeader", "");
		execution.setParameter("resource", resource);

		wih.executeWorkItem(execution, workItemManager);

		String fhirResourceId = (String) execution.getResult("fhirResourceId");
		Assert.assertNotNull(fhirResourceId);
		client.delete().resourceById("Patient", fhirResourceId).execute();

	}

	@Test(expected = FhirServerException.class)
	public void testFHIRAddDelegateUrlMissing() {

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("fhirVersion", "DSTU2");
		execution.setParameter("resource", resource);

		wih.executeWorkItem(execution, workItemManager);
	}

	@Test(expected = FhirServerException.class)
	public void testFHIRAddDelegateResourceMissing() {

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("fhirVersion", "DSTU2");
		execution.setParameter("fhirServer", FHIR2_URL);
		execution.setParameter("authHeader", "");

		wih.executeWorkItem(execution, workItemManager);
	}

	@Test
	public void testFHIRAddDelegateWithActionType() {
		

		WorkItemImpl execution1 = new WorkItemImpl();
		WorkItemImpl execution = new WorkItemImpl();
		execution1.setParameter("fhirVersion", "DSTU2");
		execution1.setParameter("fhirServer", FHIR2_URL);
		execution1.setParameter("authHeader", "");
		execution1.setParameter("resource", resource);
		wih.executeWorkItem(execution1, workItemManager);
		String createdfhirResourceId = (String) execution1.getResult("fhirResourceId");
		
		execution.setParameter("fhirVersion", "DSTU2");
		execution.setParameter("fhirServer", FHIR2_URL);
		updatedResource = "{\"resourceType\":\"Patient\",\"id\":\""+createdfhirResourceId+"\",\"text\":{\"status\":\"generated\",\"div\":\"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\"><p><b>Patient</b></p><p><b>Name</b>: Timonen, Kimmo</p><p><b>DOB</b>: 1975-03-18</p><p><b>Sex</b>: Male</p><p><b>Status</b>: Active</p></div>\"},\"identifier\":[{\"use\":\"usual\",\"type\":{\"coding\":[{\"system\":\"http://hl7.org/fhir/v2/0203\",\"code\":\"MR\",\"display\":\"Medical record number\",\"userSelected\":false}],\"text\":\"MRN\"},\"system\":\"urn:oid:1.1.1.1.1.1\",\"value\":\"10002805\",\"period\":{\"start\":\"2017-02-02T17:17:23.000Z\"}}],\"active\":true,\"name\":[{\"use\":\"official\",\"text\":\"Timonen, Kimmo\",\"family\":[\"Television\"],\"given\":[\"Kimmo\"],\"prefix\":[\"Mr.\"],\"period\":{\"start\":\"2017-02-02T17:17:29.000Z\"}}],\"gender\":\"Male\",\"birthDate\":\"1500-01-01\",\"communication\":[{\"language\":{\"text\":\"English\"}}]}";
		execution.setParameter("authHeader", "");
		execution.setParameter("resource", updatedResource);
		execution.setParameter("actionType", "update");
		wih.executeWorkItem(execution, workItemManager);
		String fhirResourceId = (String) execution.getResult("fhirResourceId");
		Assert.assertNotNull(fhirResourceId);
		client.delete().resourceById("Patient", fhirResourceId).execute();

	}
}
