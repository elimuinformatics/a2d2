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
import io.elimu.a2d2.fhirresourcewih.DeserializeResourceDelegate;

public class DeserializeResourceDelegateTest {

	private static WorkItemManager workItemManager;

	private static WorkItemHandler wih;

	@BeforeClass
	public static void initResourcesForTest() {

		KieHelper kieHelper = new KieHelper();
		KieBase kieBase = kieHelper.build();
		KieSession ksession = kieBase.newKieSession();
		wih = (WorkItemHandler) new DeserializeResourceDelegate();
		workItemManager = ksession.getWorkItemManager();
		workItemManager.registerWorkItemHandler("DeserializeResourceDelegate", wih);

	}

	/*@Test
	public void testJson2FhirDelegate() {

		String jsonString = "{\"resourceType\":\"Patient\",\"id\":\"1952\",\"meta\""
				+ ":{\"versionId\":\"4\",\"lastUpdated\":\"2017-06-08T08:08:45.000+00:00\"},"
				+ "\"text\":{\"status\":\"generated\",\"div\":\"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\"><p><b>Patient</b></p>"
				+ "<p><b>Name</b>: Timonen, Kimmo</p><p><b>DOB</b>: 1975-03-18</p><p><b>Sex</b>: Male</p><p><b>Status</b>: Active</p></div>\"}"
				+ ",\"identifier\":[{\"use\":\"usual\","
				+ "\"type\":{\"coding\":[{\"system\":\"http://hl7.org/fhir/v2/0203\",\"code\":\"MR\",\"display\":\"Medical record number\","
				+ "\"userSelected\":false}],\"text\":\"MRN\"},\"system\":\"urn:oid:1.1.1.1.1.1\",\"value\":\"10002805\",\"period\":"
				+ "{\"start\":\"2017-02-02T17:17:23.000Z\"}}],\"active\":true,\"name\":[{\"use\":\"official\",\"text\":\"Timonen, Kimmo\",\"family\""
				+ ":[\"Timonen\"],\"given\":[\"Kimmo\"],\"prefix\":[\"Mr.\"],\"period\":{\"start\":\"2017-02-02T17:17:29.000Z\"}}]"
				+ ",\"gender\":\"Male\",\"birthDate\":\"1975-03-18\",\"communication\":[{\"language\":{\"text\":\"English\"}}]}";

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("fhirVersion", "FHIR2");
		execution.setParameter("json", jsonString);

		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNotNull(execution.getResult("fhirResource"));
	}*/

	@Test(expected = WorkItemHandlerException.class)
	public void testJson2FhirDelegateJsonStringMissing() {
		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("fhirVersion", "FHIR2");

		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNotNull(execution.getResult("fhirResource"));
	}

}
