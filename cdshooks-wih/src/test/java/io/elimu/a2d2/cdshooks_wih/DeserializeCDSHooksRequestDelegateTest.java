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

package io.elimu.a2d2.cdshooks_wih;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.utils.KieHelper;

import ca.uhn.fhir.model.api.IResource;
import io.elimu.a2d2.cdshookswih.DeserializeCDSHooksRequestDelegate;
import io.elimu.a2d2.exception.WorkItemHandlerException;

public class DeserializeCDSHooksRequestDelegateTest {

	private static WorkItemManager workItemManager;
	private static final String FHIR2_SERVER_URL = "http://fhir2/baseDstu2";
	private static final String FHIR3_SERVER_URL = "http://fhir3/baseDstu3";
	private static WorkItemHandler wih;
	private static final String REQUEST_JSON_FOR_VERSION_TEST = "{\"hook\":\"patient-view\",\"hookInstance\":\"d1577c69-dfbe-44ad-ba6d-3e05e953b2ea\",\"fhirServer\":\""+FHIR3_SERVER_URL+"\",\"user\":\"Practitioner/COREPRACTITIONER1\",\"fhirAuthorization\":{\"accessToken\":\"cn389ncoiwuencr\",\"tokenType\":\"Bearer\",\"expiresIn\":1000,\"scope\":\"wide\",\"subject\":\"OAuth 2.0\"},\"context\":{\"patientId\":\"SMART-1288992\"},\"prefetch\":{\"patient\":{\"resourceType\":\"Patient\",\"gender\":\"male\",\"birthDate\":\"1925-12-23\",\"id\":\"SMART-1288992\",\"active\":true}}}";

	@BeforeClass
	public static void initResourcesForTest() {
		KieHelper kieHelper = new KieHelper();
		KieBase kieBase = kieHelper.build();
		KieSession ksession = kieBase.newKieSession();
		wih = (WorkItemHandler) new DeserializeCDSHooksRequestDelegate();
		workItemManager = ksession.getWorkItemManager();
		workItemManager.registerWorkItemHandler("DeserializeCDSHooksRequestDelegate", wih);
	}

	@Test
	public void testDeserializeCDSHooksRequestDelegateForType() {

		String requestJson = "{\"hook\":\"patient-view\",\"hookInstance\":\"d1577c69-dfbe-44ad-ba6d-3e05e953b2ea\",\"fhirServer\":\""+FHIR2_SERVER_URL+"\",\"user\":\"Practitioner/COREPRACTITIONER1\",\"fhirAuthorization\":{\"accessToken\":\"cn389ncoiwuencr\",\"tokenType\":\"Bearer\",\"expiresIn\":1000,\"scope\":\"wide\",\"subject\":\"OAuth 2.0\"},\"context\":{\"patientId\":\"SMART-1288992\"},\"prefetch\":{\"patient\":{\"resourceType\":\"Patient\",\"gender\":\"male\",\"birthDate\":\"1925-12-23\",\"id\":\"SMART-1288992\",\"active\":true}}}";

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("requestJson", requestJson);
		wih.executeWorkItem(execution, workItemManager);
		Object patient=execution.getResult("prefetchResource_patient");
		Assert.assertTrue(patient instanceof IResource);
		Assert.assertEquals(execution.getResult("prefetchResource_patient").getClass().getName(),
				"ca.uhn.fhir.model.dstu2.resource.Patient");
	}

	@Test
	public void testDeserializeForTypeofPatientResourceType() {

		String requestJson = "{\"hookInstance\":\"8ce4bd47-13bc-4a0e-be15-dd3663b3c834\",\"hook\":\"patient-view\",\"fhirServer\":\""+FHIR2_SERVER_URL+"\",\"user\":\"Practitioner/COREPRACTITIONER1\",\"patient\":\"2502\",\"context\":{\"patientId\":\"2502\"},\"prefetch\":{\"patient\":{\"resourceType\":\"Patient\",\"id\":\"2502\",\"meta\":{\"versionId\":\"10\",\"lastUpdated\":\"2017-10-03T19:52:22.000+00:00\"},\"text\":{\"status\":\"generated\",\"div\":\"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\"><div class=\\\"hapiHeaderText\\\">Mr. Paul <b>WELLES </b></div><table class=\\\"hapiPropertyTable\\\"><tbody><tr><td>Identifier</td><td>98201232</td></tr><tr><td>Address</td><td><span>1 North LaSalle Street </span><br/><span>Chicago </span><span>IL </span><span>USA </span></td></tr><tr><td>Date of birth</td><td><span>02 July 1986</span></td></tr></tbody></table></div>\"},\"identifier\":[{\"use\":\"usual\",\"type\":{\"coding\":[{\"system\":\"http://hl7.org/fhir/v2/0203\",\"code\":\"MR\"}]},\"value\":\"98201232\"},{\"use\":\"usual\",\"type\":{\"coding\":[{\"system\":\"http://hl7.org/fhir/v2/0203\",\"code\":\"PLAC\"}]},\"value\":\"67890\"}],\"active\":true,\"name\":[{\"use\":\"official\",\"family\":[\"Welles\"],\"given\":[\"Paul\"],\"prefix\":[\"Mr.\"]}],\"telecom\":[{\"system\":\"phone\",\"value\":\"(312) 555-2967\",\"use\":\"mobile\"},{\"system\":\"phone\",\"use\":\"home\"}],\"gender\":\"Male\",\"birthDate\":\"1955-07-06\",\"deceasedBoolean\":false,\"address\":[{\"use\":\"home\",\"line\":[\"1 North LaSalle Street\"],\"city\":\"Chicago\",\"state\":\"IL\",\"postalCode\":\"60602\",\"country\":\"USA\"}],\"maritalStatus\":{\"coding\":[{\"system\":\"http://hl7.org/fhir/v3/MaritalStatus\",\"code\":\"M\",\"display\":\"Married\"}]},\"multipleBirthBoolean\":false,\"contact\":[{\"relationship\":[{\"coding\":[{\"system\":\"http://hl7.org/fhir/patient-contact-relationship\",\"code\":\"parent\",\"display\":\"Parent\"}]}],\"name\":{\"use\":\"usual\",\"text\":\"Mary Welles\"},\"telecom\":[{\"system\":\"phone\",\"value\":\"217-555-2016\",\"use\":\"home\"}]}],\"communication\":[{\"language\":{\"text\":\"English(US)\"}}]}}}";

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("requestJson", requestJson);
		wih.executeWorkItem(execution, workItemManager);
		Object patient=execution.getResult("prefetchResource_patient");
		Assert.assertTrue(patient instanceof IBaseResource);
	}

	@Test
	public void testDeserializeForTypeofQuestionnaireResponseResourceType() {

		String requestJson = "{\"hookInstance\":\"d1577c69-dfbe-44ad-ba6d-3e05e953b2ea\",\"hook\":\"questionnaire\",\"user\":\"Practitioner/123\",\"patient\":\"4591\",\"prefetch\":{\"patient\":{\"resource\":{\"resourceType\":\"Patient\",\"id\":\"4591\",\"meta\":{\"versionId\":\"3\",\"lastUpdated\":\"2017-12-04T17:30:42.000+00:00\"},\"text\":{\"status\":\"generated\",\"div\":\"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\"><div class=\\\"hapiHeaderText\\\">Mr. Al <b>GOODE </b></div><table class=\\\"hapiPropertyTable\\\"><tbody><tr><td>Identifier</td><td>9539012</td></tr><tr><td>Address</td><td></td></tr><tr><td>Date of birth</td><td><span>14 July 1965</span></td></tr></tbody></table></div>\"},\"identifier\":[{\"use\":\"usual\",\"type\":{\"coding\":[{\"system\":\"http://hl7.org/fhir/v2/0203\",\"code\":\"MR\"}]},\"system\":\"urn:oid:0.1.2.3.4.5.6.7\",\"value\":\"9539012\"}],\"active\":true,\"name\":[{\"use\":\"official\",\"family\":[\"Goode\"],\"given\":[\"Al\"],\"prefix\":[\"Mr.\"]}],\"telecom\":[{\"system\":\"phone\",\"use\":\"mobile\"},{\"system\":\"phone\",\"use\":\"home\"}],\"gender\":\"Male\",\"birthDate\":\"1965-07-14\",\"deceasedBoolean\":false,\"address\":[{\"use\":\"home\"}],\"maritalStatus\":{\"coding\":[{\"system\":\"http://hl7.org/fhir/v3/MaritalStatus\",\"code\":\"M\",\"display\":\"Married\"}]},\"multipleBirthBoolean\":false,\"contact\":[{\"relationship\":[{\"coding\":[{\"system\":\"http://hl7.org/fhir/patient-contact-relationship\",\"code\":\"partner\",\"display\":\"Partner\"}]}],\"name\":{\"use\":\"usual\",\"text\":\"Ms. Goode\"},\"telecom\":[{\"system\":\"phone\",\"use\":\"home\"}]}],\"communication\":[{\"language\":{\"text\":\"English\"}}]}}},\"context\":{\"questionnaireResponse\":{\"resourceType\":\"QuestionnaireResponse\",\"status\":\"completed\",\"authored\":\"2018-06-14T17:38:49+05:30\",\"group\":{\"linkId\":\"centor-score\",\"title\":\"centor-score\",\"question\":[{\"linkId\":\"tonsillar-exudate-boolean\",\"answer\":[{\"valueBoolean\":false}]},{\"linkId\":\"lack-of-cough-boolean\",\"answer\":[{\"valueBoolean\":false}]},{\"linkId\":\"fever-present-boolean\",\"answer\":[{\"valueBoolean\":false}]},{\"linkId\":\"swollen-nodes-boolean\",\"answer\":[{\"valueBoolean\":false}]}]}}}}";

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("requestJson", requestJson);
		wih.executeWorkItem(execution, workItemManager);
		Object patient=execution.getResult("contextResource_questionnaireResponse");
		Assert.assertTrue(patient instanceof IBaseResource);
	}

	@Test
	public void testDeserializeCDSHooksRequestDelegate() {

	    String requestJson = "{\"hook\":\"patient-view\",\"hookInstance\":\"d1577c69-dfbe-44ad-ba6d-3e05e953b2ea\",\"fhirServer\":\""+FHIR2_SERVER_URL+"\",\"user\":\"Practitioner/COREPRACTITIONER1\",\"fhirAuthorization\":{\"accessToken\":\"cn389ncoiwuencr\",\"tokenType\":\"Bearer\",\"expiresIn\":1000,\"scope\":\"wide\",\"subject\":\"OAuth 2.0\"},\"context\":{\"patientId\":\"SMART-1288992\"},\"prefetch\":{\"patient\":{\"resourceType\":\"Patient\",\"gender\":\"male\",\"birthDate\":\"1925-12-23\",\"id\":\"SMART-1288992\",\"active\":true}}}";

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("requestJson", requestJson);

		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNotNull(execution.getResult("cdsHookRequest"));
		Assert.assertNotNull(execution.getResult("hook"));
		Assert.assertNotNull(execution.getResult("hookInstance"));
		Assert.assertNotNull(execution.getResult("fhirServer"));
		Assert.assertNotNull(execution.getResult("fhirAuthorization"));
		Assert.assertNotNull(execution.getResult("user"));
		Assert.assertNotNull(execution.getResult("context_patientId"));
	}

	@Test(expected = WorkItemHandlerException.class)
	public void testDeserializeCDSHooksRequestDelegateMissingParam() {

		String requestJson = "{\"hook\":\"patient-view\",\"fhirServer\":"
				+ "\""+FHIR3_SERVER_URL+"\",\"context\":{\"proStartOffset\":1,\"patientId\":"
				+ "\"SMART-1288992\"},\"prefetch\":{}}";

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("requestJson", requestJson);

		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNotNull(execution.getResult("cdsHookRequest"));
	}


	@Test
	public void testDeserializeCDSHooksRequestDelegateMissingOptionalParam() {

	    String requestJson = "{\"hook\":\"patient-view\",\"hookInstance\":\"d1577c69-dfbe-44ad-ba6d-3e05e953b2ea\",\"fhirServer\":\""+FHIR2_SERVER_URL+"\",\"user\":\"Practitioner/COREPRACTITIONER1\",\"context\":{\"patientId\":\"SMART-1288992\"},\"prefetch\":{\"patient\":{\"resourceType\":\"Patient\",\"gender\":\"male\",\"birthDate\":\"1925-12-23\",\"id\":\"SMART-1288992\",\"active\":true}}}";

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("requestJson", requestJson);

		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNotNull(execution.getResult("cdsHookRequest"));
		Assert.assertNotNull(execution.getResult("hook"));
		Assert.assertNotNull(execution.getResult("hookInstance"));
		Assert.assertNotNull(execution.getResult("fhirServer"));
		Assert.assertNull(execution.getResult("fhirAuthorization"));
		Assert.assertNotNull(execution.getResult("user"));
		Assert.assertNotNull(execution.getResult("context_patientId"));
	}

	@Test
	public void testDeserializeCDSHooksRequestWithInteger() {

	    String requestJson = "{\"hookInstance\":\"8ce4bd47-13bc-4a0e-be15-dd3663b3c834\",\"hook\":\"medication-prescribe\",\"fhirServer\":\""+FHIR2_SERVER_URL+"\",\"user\":\"Practitioner/COREPRACTITIONER1\",\"patient\":\"2502\",\"context\":{\"patientId\":\"2502\",\"medications\":{\"resourceType\":\"Bundle\",\"entry\":[{\"resource\":{\"resourceType\":\"MedicationOrder\",\"id\":\"smart-MedicationOrder-103\",\"status\":\"draft\",\"patient\":{\"reference\":\"Patient/2502\"},\"medicationCodeableConcept\":{\"coding\":[{\"system\":\"http://www.nlm.nih.gov/research/umls/rxnorm\",\"code\":\"617993\",\"display\":\"Amoxicillin 120 MG/ML / clavulanate potassium 8.58 MG/ML Oral Suspension\"}],\"text\":\"Amoxicillin 120 MG/ML / clavulanate potassium 8.58 MG/ML Oral Suspension\"},\"dosageInstruction\":[{\"text\":\"5 mL bid x 10 days\",\"timing\":{\"repeat\":{\"boundsPeriod\":{\"start\":\"2005-01-04\"},\"frequency\":2,\"period\":1,\"periodUnits\":\"d\"}},\"doseQuantity\":{\"value\":5,\"unit\":\"mL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"mL\"}}],\"dispenseRequest\":{\"numberOfRepeatsAllowed\":1,\"quantity\":{\"value\":1,\"unit\":\"mL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"mL\"},\"expectedSupplyDuration\":{\"value\":10,\"unit\":\"days\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"d\"}}}},{\"resource\":{\"resourceType\":\"MedicationOrder\",\"id\":\"smart-MedicationOrder-104\",\"status\":\"draft\",\"patient\":{\"reference\":\"Patient/2502\"},\"medicationCodeableConcept\":{\"coding\":[{\"system\":\"http://www.nlm.nih.gov/research/umls/rxnorm\",\"code\":\"211307\",\"display\":\"Azithromycin 20 MG/ML Oral Suspension [Zithromax]\"}],\"text\":\"Azithromycin 20 MG/ML Oral Suspension [Zithromax]\"},\"dosageInstruction\":[{\"text\":\"15 mL daily x 3 days\",\"timing\":{\"repeat\":{\"boundsPeriod\":{\"start\":\"2005-01-18\"},\"frequency\":1,\"period\":1,\"periodUnits\":\"d\"}},\"doseQuantity\":{\"value\":15,\"unit\":\"mL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"mL\"}}],\"dispenseRequest\":{\"numberOfRepeatsAllowed\":1,\"quantity\":{\"value\":1,\"unit\":\"mL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"mL\"},\"expectedSupplyDuration\":{\"value\":3,\"unit\":\"days\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"d\"}}}}]}},\"prefetch\":{\"patient\":{\"resourceType\":\"Patient\",\"id\":\"2502\",\"text\":{\"status\":\"generated\",\"div\":\"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\"><div class=\\\"hapiHeaderText\\\">Mr. Paul <b>WELLES </b></div><table class=\\\"hapiPropertyTable\\\"><tbody><tr><td>Identifier</td><td>98201232</td></tr><tr><td>Address</td><td><span>1 North LaSalle Street </span><br/><span>Chicago </span><span>IL </span><span>USA </span></td></tr><tr><td>Date of birth</td><td><span>02 July 1986</span></td></tr></tbody></table></div>\"},\"identifier\":[{\"use\":\"usual\",\"type\":{\"coding\":[{\"system\":\"http://hl7.org/fhir/v2/0203\",\"code\":\"MR\"}]},\"value\":\"98201232\"},{\"use\":\"usual\",\"type\":{\"coding\":[{\"system\":\"http://hl7.org/fhir/v2/0203\",\"code\":\"PLAC\"}]},\"value\":\"67890\"}],\"active\":true,\"name\":[{\"use\":\"official\",\"family\":[\"Welles\"],\"given\":[\"Paul\"],\"prefix\":[\"Mr.\"]}],\"telecom\":[{\"system\":\"phone\",\"value\":\"(312) 555-2967\",\"use\":\"mobile\"},{\"system\":\"phone\",\"use\":\"home\"}],\"gender\":\"Male\",\"birthDate\":\"1955-07-06\",\"deceasedBoolean\":false,\"address\":[{\"use\":\"home\",\"line\":[\"1 North LaSalle Street\"],\"city\":\"Chicago\",\"state\":\"IL\",\"postalCode\":\"60602\",\"country\":\"USA\"}],\"maritalStatus\":{\"coding\":[{\"system\":\"http://hl7.org/fhir/v3/MaritalStatus\",\"code\":\"M\",\"display\":\"Married\"}]},\"multipleBirthBoolean\":false,\"contact\":[{\"relationship\":[{\"coding\":[{\"system\":\"http://hl7.org/fhir/patient-contact-relationship\",\"code\":\"parent\",\"display\":\"Parent\"}]}],\"name\":{\"use\":\"usual\",\"text\":\"Mary Welles\"},\"telecom\":[{\"system\":\"phone\",\"value\":\"217-555-2016\",\"use\":\"home\"}]}],\"communication\":[{\"language\":{\"text\":\"English(US)\"}}]}}}";
		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("requestJson", requestJson);

		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNotNull(execution.getResult("cdsHookRequest"));
		Assert.assertNotNull(execution.getResult("hook"));
		Assert.assertNotNull(execution.getResult("hookInstance"));
		Assert.assertNotNull(execution.getResult("fhirServer"));
		Assert.assertNull(execution.getResult("fhirAuthorization"));
		Assert.assertNotNull(execution.getResult("user"));
		Assert.assertNotNull(execution.getResult("context_patientId"));
	}


	@Test
	public void testDeserializeCDSHooksRequestWithException() {

	    String requestJson = "{\"hookInstance\":\"8ce4bd47-13bc-4a0e-be15-dd3663b3c834\",\"fhirServer\":\""+FHIR2_SERVER_URL+"\",\"user\":\"Practitioner/COREPRACTITIONER1\",\"patient\":\"2502\",\"context\":{\"patientId\":\"2502\",\"medications\":{\"resourceType\":\"Bundle\",\"entry\":[{\"resource\":{\"resourceType\":\"MedicationOrder\",\"id\":\"smart-MedicationOrder-103\",\"status\":\"draft\",\"patient\":{\"reference\":\"Patient/2502\"},\"medicationCodeableConcept\":{\"coding\":[{\"system\":\"http://www.nlm.nih.gov/research/umls/rxnorm\",\"code\":\"617993\",\"display\":\"Amoxicillin 120 MG/ML / clavulanate potassium 8.58 MG/ML Oral Suspension\"}],\"text\":\"Amoxicillin 120 MG/ML / clavulanate potassium 8.58 MG/ML Oral Suspension\"},\"dosageInstruction\":[{\"text\":\"5 mL bid x 10 days\",\"timing\":{\"repeat\":{\"boundsPeriod\":{\"start\":\"2005-01-04\"},\"frequency\":2,\"period\":1,\"periodUnits\":\"d\"}},\"doseQuantity\":{\"value\":5,\"unit\":\"mL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"mL\"}}],\"dispenseRequest\":{\"numberOfRepeatsAllowed\":1,\"quantity\":{\"value\":1,\"unit\":\"mL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"mL\"},\"expectedSupplyDuration\":{\"value\":10,\"unit\":\"days\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"d\"}}}},{\"resource\":{\"resourceType\":\"MedicationOrder\",\"id\":\"smart-MedicationOrder-104\",\"status\":\"draft\",\"patient\":{\"reference\":\"Patient/2502\"},\"medicationCodeableConcept\":{\"coding\":[{\"system\":\"http://www.nlm.nih.gov/research/umls/rxnorm\",\"code\":\"211307\",\"display\":\"Azithromycin 20 MG/ML Oral Suspension [Zithromax]\"}],\"text\":\"Azithromycin 20 MG/ML Oral Suspension [Zithromax]\"},\"dosageInstruction\":[{\"text\":\"15 mL daily x 3 days\",\"timing\":{\"repeat\":{\"boundsPeriod\":{\"start\":\"2005-01-18\"},\"frequency\":1,\"period\":1,\"periodUnits\":\"d\"}},\"doseQuantity\":{\"value\":15,\"unit\":\"mL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"mL\"}}],\"dispenseRequest\":{\"numberOfRepeatsAllowed\":1,\"quantity\":{\"value\":1,\"unit\":\"mL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"mL\"},\"expectedSupplyDuration\":{\"value\":3,\"unit\":\"days\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"d\"}}}}]}},\"prefetch\":{\"patient\":{\"resourceType\":\"Patient\",\"id\":\"2502\",\"text\":{\"status\":\"generated\",\"div\":\"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\"><div class=\\\"hapiHeaderText\\\">Mr. Paul <b>WELLES </b></div><table class=\\\"hapiPropertyTable\\\"><tbody><tr><td>Identifier</td><td>98201232</td></tr><tr><td>Address</td><td><span>1 North LaSalle Street </span><br/><span>Chicago </span><span>IL </span><span>USA </span></td></tr><tr><td>Date of birth</td><td><span>02 July 1986</span></td></tr></tbody></table></div>\"},\"identifier\":[{\"use\":\"usual\",\"type\":{\"coding\":[{\"system\":\"http://hl7.org/fhir/v2/0203\",\"code\":\"MR\"}]},\"value\":\"98201232\"},{\"use\":\"usual\",\"type\":{\"coding\":[{\"system\":\"http://hl7.org/fhir/v2/0203\",\"code\":\"PLAC\"}]},\"value\":\"67890\"}],\"active\":true,\"name\":[{\"use\":\"official\",\"family\":[\"Welles\"],\"given\":[\"Paul\"],\"prefix\":[\"Mr.\"]}],\"telecom\":[{\"system\":\"phone\",\"value\":\"(312) 555-2967\",\"use\":\"mobile\"},{\"system\":\"phone\",\"use\":\"home\"}],\"gender\":\"Male\",\"birthDate\":\"1955-07-06\",\"deceasedBoolean\":false,\"address\":[{\"use\":\"home\",\"line\":[\"1 North LaSalle Street\"],\"city\":\"Chicago\",\"state\":\"IL\",\"postalCode\":\"60602\",\"country\":\"USA\"}],\"maritalStatus\":{\"coding\":[{\"system\":\"http://hl7.org/fhir/v3/MaritalStatus\",\"code\":\"M\",\"display\":\"Married\"}]},\"multipleBirthBoolean\":false,\"contact\":[{\"relationship\":[{\"coding\":[{\"system\":\"http://hl7.org/fhir/patient-contact-relationship\",\"code\":\"parent\",\"display\":\"Parent\"}]}],\"name\":{\"use\":\"usual\",\"text\":\"Mary Welles\"},\"telecom\":[{\"system\":\"phone\",\"value\":\"217-555-2016\",\"use\":\"home\"}]}],\"communication\":[{\"language\":{\"text\":\"English(US)\"}}]}}}";
		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("requestJson", requestJson);
		try {
			wih.executeWorkItem(execution, workItemManager);
		}
		catch (WorkItemHandlerException ex) {
			Assert.assertEquals(ex.getMessage(), "Missing parameter hook");
		}
	}

	@Test
	public void testDeserializeCDSHooksRequestWithInteger2() {

	    String requestJson = "{\"hookInstance\":\"8ce4bd47-13bc-4a0e-be15-dd3663b3c834\",\"hook\":\"medication-prescribe\",\"fhirServer\":\""+FHIR2_SERVER_URL+"\",\"user\":\"Practitioner/COREPRACTITIONER1\",\"patient\":\"2502\",\"context\":{\"patientId\":\"2502\",\"medications\":{\"resourceType\":\"Bundle\",\"entry\":[{\"resource\":{\"resourceType\":\"MedicationOrder\",\"id\":\"smart-MedicationOrder-103\",\"status\":\"draft\",\"patient\":{\"reference\":\"Patient/2502\"},\"medicationCodeableConcept\":{\"coding\":[{\"system\":\"http://www.nlm.nih.gov/research/umls/rxnorm\",\"code\":\"617993\",\"display\":\"Amoxicillin 120 MG/ML / clavulanate potassium 8.58 MG/ML Oral Suspension\"}],\"text\":\"Amoxicillin 120 MG/ML / clavulanate potassium 8.58 MG/ML Oral Suspension\"},\"dosageInstruction\":[{\"text\":\"5 mL bid x 10 days\",\"timing\":{\"repeat\":{\"boundsPeriod\":{\"start\":\"2005-01-04\"},\"frequency\":2,\"period\":1,\"periodUnits\":\"d\"}},\"doseQuantity\":{\"value\":5,\"unit\":\"mL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"mL\"}}],\"dispenseRequest\":{\"quantity\":{\"value\":1,\"unit\":\"mL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"mL\"},\"expectedSupplyDuration\":{\"value\":10,\"unit\":\"days\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"d\"}}}},{\"resource\":{\"resourceType\":\"MedicationOrder\",\"id\":\"smart-MedicationOrder-104\",\"status\":\"draft\",\"patient\":{\"reference\":\"Patient/2502\"},\"medicationCodeableConcept\":{\"coding\":[{\"system\":\"http://www.nlm.nih.gov/research/umls/rxnorm\",\"code\":\"211307\",\"display\":\"Azithromycin 20 MG/ML Oral Suspension [Zithromax]\"}],\"text\":\"Azithromycin 20 MG/ML Oral Suspension [Zithromax]\"},\"dosageInstruction\":[{\"text\":\"15 mL daily x 3 days\",\"timing\":{\"repeat\":{\"boundsPeriod\":{\"start\":\"2005-01-18\"},\"periodUnits\":\"d\"}},\"doseQuantity\":{\"value\":15,\"unit\":\"mL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"mL\"}}],\"dispenseRequest\":{\"quantity\":{\"value\":1,\"unit\":\"mL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"mL\"},\"expectedSupplyDuration\":{\"value\":3,\"unit\":\"days\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"d\"}}}}]}},\"prefetch\":{\"patient\":{\"resourceType\":\"Patient\",\"id\":\"2502\",\"text\":{\"status\":\"generated\",\"div\":\"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\"><div class=\\\"hapiHeaderText\\\">Mr. Paul <b>WELLES </b></div><table class=\\\"hapiPropertyTable\\\"><tbody><tr><td>Identifier</td><td>98201232</td></tr><tr><td>Address</td><td><span>1 North LaSalle Street </span><br/><span>Chicago </span><span>IL </span><span>USA </span></td></tr><tr><td>Date of birth</td><td><span>02 July 1986</span></td></tr></tbody></table></div>\"},\"identifier\":[{\"use\":\"usual\",\"type\":{\"coding\":[{\"system\":\"http://hl7.org/fhir/v2/0203\",\"code\":\"MR\"}]},\"value\":\"98201232\"},{\"use\":\"usual\",\"type\":{\"coding\":[{\"system\":\"http://hl7.org/fhir/v2/0203\",\"code\":\"PLAC\"}]},\"value\":\"67890\"}],\"active\":true,\"name\":[{\"use\":\"official\",\"family\":[\"Welles\"],\"given\":[\"Paul\"],\"prefix\":[\"Mr.\"]}],\"telecom\":[{\"system\":\"phone\",\"value\":\"(312) 555-2967\",\"use\":\"mobile\"},{\"system\":\"phone\",\"use\":\"home\"}],\"gender\":\"Male\",\"birthDate\":\"1955-07-06\",\"deceasedBoolean\":false,\"address\":[{\"use\":\"home\",\"line\":[\"1 North LaSalle Street\"],\"city\":\"Chicago\",\"state\":\"IL\",\"postalCode\":\"60602\",\"country\":\"USA\"}],\"maritalStatus\":{\"coding\":[{\"system\":\"http://hl7.org/fhir/v3/MaritalStatus\",\"code\":\"M\",\"display\":\"Married\"}]},\"multipleBirthBoolean\":false,\"contact\":[{\"relationship\":[{\"coding\":[{\"system\":\"http://hl7.org/fhir/patient-contact-relationship\",\"code\":\"parent\",\"display\":\"Parent\"}]}],\"name\":{\"use\":\"usual\",\"text\":\"Mary Welles\"},\"telecom\":[{\"system\":\"phone\",\"value\":\"217-555-2016\",\"use\":\"home\"}]}],\"communication\":[{\"language\":{\"text\":\"English(US)\"}}]}}}";
		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("requestJson", requestJson);

		wih.executeWorkItem(execution, workItemManager);
		Assert.assertNotNull(execution.getResult("contextResource_medications"));
	}
	
	@Test
	public void testFhirVersionDSTU2() {
		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("requestJson", REQUEST_JSON_FOR_VERSION_TEST);
		execution.setParameter("fhirVersion", "DSTU2");
		wih.executeWorkItem(execution, workItemManager);
		Assert.assertEquals(execution.getResult("prefetchResource_patient").getClass().getName(),
				"ca.uhn.fhir.model.dstu2.resource.Patient");
	}

	@Test
	public void testFhirVersionSTU3() {
		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("requestJson", REQUEST_JSON_FOR_VERSION_TEST);
		execution.setParameter("fhirVersion", "stu3");
		wih.executeWorkItem(execution, workItemManager);
		Assert.assertEquals(execution.getResult("prefetchResource_patient").getClass().getName(),
				"org.hl7.fhir.dstu3.model.Patient");
	}

	@Test
	public void testFhirVersionR4() {
		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("requestJson", REQUEST_JSON_FOR_VERSION_TEST);
		execution.setParameter("fhirVersion", "r4");
		wih.executeWorkItem(execution, workItemManager);
		Assert.assertEquals(execution.getResult("prefetchResource_patient").getClass().getName(),
				"org.hl7.fhir.r4.model.Patient");
	}

	@Test
	public void testFhirVersionNotExist() {
		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("requestJson", REQUEST_JSON_FOR_VERSION_TEST);
		execution.setParameter("fhirVersion", "notexist");
		wih.executeWorkItem(execution, workItemManager);
		Assert.assertEquals(execution.getResult("prefetchResource_patient").getClass().getName(),
				"ca.uhn.fhir.model.dstu2.resource.Patient");
	}

}
