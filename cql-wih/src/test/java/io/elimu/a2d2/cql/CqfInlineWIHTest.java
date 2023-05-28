package io.elimu.a2d2.cql;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Dosage.DosageDoseAndRateComponent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Identifier.IdentifierUse;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestIntent;
import org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestStatus;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.PlanDefinition;
import org.hl7.fhir.r4.model.PlanDefinition.ActionSelectionBehavior;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RequestGroup;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Timing.UnitsOfTime;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import io.elimu.a2d2.cdsresponse.entity.Card;
import io.elimu.a2d2.cdsresponse.entity.Suggestion;
import io.elimu.a2d2.oauth.BodyBuilder;
import io.elimu.a2d2.oauth.OAuthUtils;

public class CqfInlineWIHTest {
	
	@Test
	public void testCqlLibraryLoading() throws Exception {
		if (System.getProperty("fhirServerToken") == null) {
			return;
		}
		String token = System.getProperty("fhirServerToken");
		PlanDefCdsInlineWorkItemHandler handler = new PlanDefCdsInlineWorkItemHandler();
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setParameter("fhirServerAuth", "Bearer " + token);
		workItem.setParameter("fhirServerUrl", "https://api.logicahealth.org/cdcgc/data");
		workItem.setParameter("fhirTerminologyServerUrl", "https://fhir4-terminology-sandbox.elimuinformatics.com/baseR4");
		workItem.setParameter("context_hook", "order-select");
		workItem.setParameter("context_hookInstance", UUID.randomUUID().toString());
		workItem.setParameter("context_userId", "Practitioner/example");
		workItem.setParameter("patientId", "SMART-436610");
		workItem.setParameter("context_patientId", "SMART-436610");
		workItem.setParameter("context_encounterId", "89284");
		Bundle bundle = new Bundle();
		bundle.setType(Bundle.BundleType.COLLECTION);
		MedicationRequest mreq = new MedicationRequest();
		mreq.setId("medrx0325");
		mreq.setStatus(MedicationRequest.MedicationRequestStatus.DRAFT);
		mreq.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);
		mreq.addCategory().setText("Inpatient").addCoding().setCode("inpatient").setSystem("http://terminology.hl7.org/CodeSystem/medicationrequest-category").setDisplay("Inpatient");
		mreq.setMedication(new Reference().setReference("Medication/14652").setDisplay("CEFTRIAXONE 250 MG SOLUTION FOR INJECTION"));
		mreq.setSubject(new Reference().setReference("Patient/eCWvPpzzlvY3RVsspc7TKiw3").setDisplay("Zzzrsh, Gonotwentyfour"));
		mreq.setEncounter(new Reference().setDisplay("Office Visit").setReference("Encounter/eKhmYI-wOnGOK1xPgpVID7Q3").
				setIdentifier(new Identifier().setUse(Identifier.IdentifierUse.USUAL).setSystem("urn:oid:1.2.840.114350.1.13.301.3.7.3.698084.8").setValue("80399662")));
		mreq.setRequester(new Reference().setType("Practitioner").setDisplay("Physician Family Medicine, MD"));
		mreq.setRecorder(new Reference().setType("Practitioner").setDisplay("Physician Family Medicine, MD"));
		Dosage dos1 = mreq.addDosageInstruction();
		SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dos1.getTiming().getRepeat().setBounds(new Period().setStart(FORMAT.parse("2023-03-03T14:00:00Z"))).setFrequency(1).setPeriod(1).setPeriodUnit(UnitsOfTime.D);
		dos1.getTiming().getCode().setText("Daily");
		dos1.getRoute().addCoding().setCode("78421000").setSystem("http://snomed.info/sct").setDisplay("Intramuscular route (qualifier value)");
		dos1.getRoute().addCoding().setCode("6").setSystem("urn:oid:1.2.840.114350.1.13.301.3.7.4.798268.7025").setDisplay("Intramuscular");
		dos1.getRoute().setText("Intramuscular");
		dos1.addDoseAndRate().setType(new CodeableConcept(new Coding("http://epic.com/CodeSystem/dose-rate-type", "admin-amount", "admin-amount")).setText("admin-amount")).
			getDoseQuantity().setValue(1).setUnit("mL").setCode("mL").setSystem("http://unitsofmeasure.org");
		dos1.addDoseAndRate().setType(new CodeableConcept(new Coding("http://epic.com/CodeSystem/dose-rate-type", "calculated", "calculated")).setText("calculated")).
			getDoseQuantity().setValue(250).setUnit("mg").setCode("mg").setSystem("http://unitsofmeasure.org");
		dos1.addDoseAndRate().setType(new CodeableConcept(new Coding("http://epic.com/CodeSystem/dose-rate-type", "ordered", "ordered")).setText("ordered")).
			getDoseQuantity().setValue(250).setUnit("mg").setCode("mg").setSystem("http://unitsofmeasure.org");
		bundle.addEntry().setResource(mreq);
		workItem.setParameter("context_selections", Arrays.asList("MedicationRequest/medrx0325"));
		workItem.setParameter("context_draftOrders", bundle);
		workItem.setParameter("planDefinitionUrl", "http://elimu.io/PlanDefinition/GonorrheaCDSPresumptiveTreatment");
		NoOpWorkItemManager manager = new NoOpWorkItemManager();
		long start = System.currentTimeMillis();
		handler.executeWorkItem(workItem, manager);
		for (String key : workItem.getResults().keySet()) {
			System.out.println(" - " + key + ": " + workItem.getResult(key));
		}
		long time = System.currentTimeMillis() - start;
		Assert.assertNotNull(workItem.getResults());
		Assert.assertNull(workItem.getResult("error"));		
		Assert.assertNotNull(workItem.getResult("cardsJson"));
		System.out.println("Time to run 1st time (ms): " + time);
		Assert.assertEquals(true, manager.isCompleted());

		Assert.assertNotNull(workItem.getResults());
		Assert.assertNotNull(workItem.getResult("cards"));
		List<?> cards = (List<?>) workItem.getResult("cards");
		Assert.assertEquals(1, cards.size());
		Card card = (Card) cards.get(0);
		Assert.assertNotNull(card);
		Assert.assertEquals("Attention: Antibiotic Stewardship", card.getSummary());
		Assert.assertEquals("If the current ceftriaxone order is presumptive treatment for gonorrhea, please note that the CDC\u0027s updated "
				+ "guidance is that for patients (presumably) weighing \u003c150 kg and no beta-lactam allergy, the recommended dose is "
				+ "ceftriaxone 500 mg IM x1.", card.getDetail());
		Assert.assertEquals("warning", card.getIndicator());
		Assert.assertEquals("any", card.getSelectionBehavior());
		Assert.assertNotNull(card.getOverrideReasons());
		Assert.assertEquals(3, card.getOverrideReasons().size());
		Assert.assertNotNull(card.getSuggestions());
		Assert.assertEquals(2, card.getSuggestions().size());
	}

	@Test
	public void testSecondAzizPlanWithNoContextMeds() throws Exception {
		if (System.getProperty("fhirUsername") == null || System.getProperty("fhirPassword") == null) {
			return;
		}
		PlanDefCdsInlineWorkItemHandler handler = new PlanDefCdsInlineWorkItemHandler();
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setParameter("fhirServerUrl", "https://fhir4-internal.elimuinformatics.com/fhir");
		workItem.setParameter("fhirTerminologyServerUrl", "https://fhir4-terminology-internal.elimuinformatics.com/fhir");
		String clientSecret = "";
		String clientId = "fhir4-terminology-api";
		String tokenUrl = "https://auth-internal.elimuinformatics.com/auth/realms/product/protocol/openid-connect/token";
		String body = new BodyBuilder().addToBody("client_id", clientId).addToBody("client_secret", clientSecret).
				addToBody("token_url", tokenUrl).addToBody("grant_type", "password").addToBody("username", System.getProperty("fhirUsername")).
				addToBody("password", System.getProperty("fhirPassword")).addToBody("scope", "offline_access").build();
		Map<String, Object> results = OAuthUtils.authenticate(body, tokenUrl, clientId, clientSecret);
		String token = (String) results.get("access_token");
		workItem.setParameter("fhirTerminologyServerAuth", "Bearer " + token);
		workItem.setParameter("fhirServerAuth", "Bearer " + token);
		workItem.setParameter("planDefinitionId", "12056");
		workItem.setParameter("patientId", "436610");
		workItem.setParameter("context_patientId", "436610");
		NoOpWorkItemManager manager = new NoOpWorkItemManager();
		long start = System.currentTimeMillis();
		handler.executeWorkItem(workItem, manager);
		long time = System.currentTimeMillis() - start;
		Assert.assertNotNull(workItem.getResults());
		Assert.assertNull(workItem.getResult("error"));		
		Assert.assertNotNull(workItem.getResult("cardsJson"));
		System.out.println("Time to run 1st time (ms): " + time);
		Assert.assertEquals(true, manager.isCompleted());
		Assert.assertNotNull(workItem.getResults());
		Assert.assertNotNull(workItem.getResult("cards"));
		List<?> cards = (List<?>) workItem.getResult("cards");
		Assert.assertEquals(2, cards.size());
		System.out.println("CARDS JSON");
		System.out.println(workItem.getResult("cardsJson"));
		
		for (Object o : cards) {
			Card card = (Card) o;
			if ("Is Ceftriaxone being prescribed".equals(card.getSummary())) {
				Assert.assertEquals("No, Ceftriaxone is not being prescribed", card.getDetail());
			}
		}
	}
	
	@Test
	public void testSecondAzizPlan() throws Exception {
		if (System.getProperty("fhirUsername") == null || System.getProperty("fhirPassword") == null) {
			return;
		}
		PlanDefCdsInlineWorkItemHandler handler = new PlanDefCdsInlineWorkItemHandler();
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setParameter("fhirServerUrl", "https://fhir4-internal.elimuinformatics.com/fhir");
		workItem.setParameter("fhirTerminologyServerUrl", "https://fhir4-terminology-internal.elimuinformatics.com/fhir");
		String clientSecret = "";
		String clientId = "fhir4-terminology-api";
		String tokenUrl = "https://auth-internal.elimuinformatics.com/auth/realms/product/protocol/openid-connect/token";
		String body = new BodyBuilder().addToBody("client_id", clientId).addToBody("client_secret", clientSecret).
				addToBody("token_url", tokenUrl).addToBody("grant_type", "password").addToBody("username", System.getProperty("fhirUsername")).
				addToBody("password", System.getProperty("fhirPassword")).addToBody("scope", "offline_access").build();
		Map<String, Object> results = OAuthUtils.authenticate(body, tokenUrl, clientId, clientSecret);
		String token = (String) results.get("access_token");
		workItem.setParameter("fhirTerminologyServerAuth", "Bearer " + token);
		workItem.setParameter("fhirServerAuth", "Bearer " + token);
		workItem.setParameter("planDefinitionId", "12056");
		workItem.setParameter("patientId", "436610");
		workItem.setParameter("context_patientId", "436610");
		List<Resource> medRequests = new ArrayList<>();
		MedicationRequest medReq = new MedicationRequest();
		medReq.setId("mdrx0325");
		medReq.addIdentifier().setUse(IdentifierUse.OFFICIAL).setSystem("http://www.bmc.nl/portal/prescriptions").setValue("12345689");
		medReq.setStatus(MedicationRequestStatus.ONHOLD);
		medReq.setIntent(MedicationRequestIntent.ORDER);
		medReq.setMedication(new CodeableConcept());
		medReq.getMedicationCodeableConcept().addCoding().setSystem("http://www.nlm.nih.gov/research/umls/rxnorm").setCode("1665021").setDisplay("ceftriaxone 1000 MG Injection");
		medReq.getSubject().setReference("Patient/436610").setDisplay("Donald D");
		medReq.setAuthoredOn(new Date());
		medReq.getRequester().setReference("Practitioner/f007").setDisplay("Patrick Pump");
		Dosage dosage = medReq.addDosageInstruction();
		dosage.setSequence(1).setText("Apply to affected areas four times daily");
		dosage.getTiming().getRepeat().setFrequency(4).setPeriod(1).setPeriodUnit(UnitsOfTime.D);
		DosageDoseAndRateComponent rate = dosage.addDoseAndRate();
		rate.getType().addCoding().setSystem("http://terminology.hl7.org/CodeSystem/dose-rate-type").setCode("ordered").setDisplay("Ordered");
		rate.getDoseQuantity().setValue(1).setUnit("ea").setSystem("http://terminology.hl7.org/CodeSystem/v3-orderableDrugForm").setCode("OINT");
		medRequests.add(medReq);
		workItem.setParameter("context_MedicationRequestsBeingPlaced", medRequests);
		NoOpWorkItemManager manager = new NoOpWorkItemManager();
		long start = System.currentTimeMillis();
		handler.executeWorkItem(workItem, manager);
		long time = System.currentTimeMillis() - start;
		Assert.assertNotNull(workItem.getResults());
		Assert.assertNull(workItem.getResult("error"));		
		Assert.assertNotNull(workItem.getResult("cardsJson"));
		System.out.println("Time to run 1st time (ms): " + time);
		Assert.assertEquals(true, manager.isCompleted());
		Assert.assertNotNull(workItem.getResults());
		Assert.assertNotNull(workItem.getResult("cards"));
		List<?> cards = (List<?>) workItem.getResult("cards");
		Assert.assertEquals(2, cards.size());
		System.out.println("CARDS JSON");
		System.out.println(workItem.getResult("cardsJson"));
		
		for (Object o : cards) {
			Card card = (Card) o;
			if ("Is Ceftriaxone being prescribed".equals(card.getSummary())) {
				Assert.assertNotEquals("No, Ceftriaxone is not being prescribed", card.getDetail());
			}
		}
		

	}
	
	@Test
	public void testAzizPlan() throws Exception {
		if (System.getProperty("fhirUsername") == null || System.getProperty("fhirPassword") == null) {
			return;
		}
		PlanDefCdsInlineWorkItemHandler handler = new PlanDefCdsInlineWorkItemHandler();
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setParameter("fhirServerUrl", "https://fhir4-internal.elimuinformatics.com/fhir");
		workItem.setParameter("fhirTerminologyServerUrl", "https://fhir4-terminology-internal.elimuinformatics.com/fhir");
		String clientSecret = "";
		String clientId = "fhir4-terminology-api";
		String tokenUrl = "https://auth-internal.elimuinformatics.com/auth/realms/product/protocol/openid-connect/token";
		String body = new BodyBuilder().addToBody("client_id", clientId).addToBody("client_secret", clientSecret).
				addToBody("token_url", tokenUrl).addToBody("grant_type", "password").addToBody("username", System.getProperty("fhirUsername")).
				addToBody("password", System.getProperty("fhirPassword")).addToBody("scope", "offline_access").build();
		Map<String, Object> results = OAuthUtils.authenticate(body, tokenUrl, clientId, clientSecret);
		String token = (String) results.get("access_token");
		workItem.setParameter("fhirTerminologyServerAuth", "Bearer " + token);
		workItem.setParameter("fhirServerAuth", "Bearer " + token);
		workItem.setParameter("planDefinitionId", "657");
		workItem.setParameter("patientId", "436610");
		workItem.setParameter("context_patientId", "436610");
		NoOpWorkItemManager manager = new NoOpWorkItemManager();
		long start = System.currentTimeMillis();
		handler.executeWorkItem(workItem, manager);
		long time = System.currentTimeMillis() - start;
		Assert.assertNotNull(workItem.getResults());
		Assert.assertNull(workItem.getResult("error"));		
		Assert.assertNotNull(workItem.getResult("cardsJson"));
		System.out.println("Time to run 1st time (ms): " + time);
		Assert.assertEquals(true, manager.isCompleted());
		Assert.assertNotNull(workItem.getResults());
		Assert.assertNotNull(workItem.getResult("cards"));
		List<?> cards = (List<?>) workItem.getResult("cards");
		Assert.assertEquals(1, cards.size());
		System.out.println("CARDS JSON");
		System.out.println(workItem.getResult("cardsJson"));
		
		workItem.setParameter("patientId", "141188");
		workItem.setParameter("context_patientId", "141188");
		workItem.setResults(new HashMap<>());
		manager = new NoOpWorkItemManager();
		start = System.currentTimeMillis();
		handler.executeWorkItem(workItem, manager);
		time = System.currentTimeMillis() - start;
		Assert.assertNotNull(workItem.getResults());
		Assert.assertNull(workItem.getResult("error"));		
		Assert.assertNotNull(workItem.getResult("cardsJson"));
		System.out.println("Time to run 2nd time (ms): " + time);
		Assert.assertEquals(true, manager.isCompleted());
		Assert.assertNotNull(workItem.getResults());
		Assert.assertNotNull(workItem.getResult("cards"));
		cards = (List<?>) workItem.getResult("cards");
		Assert.assertEquals(0, cards.size());
		
	}
	
	@Test
	@Ignore //Ignoring because its failing the builds, someone is working on it. So, we are going to ignore it for now 
	public void testInlineGcPlanDef() throws Exception {
		PlanDefCdsInlineWorkItemHandler handler = new PlanDefCdsInlineWorkItemHandler();
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setParameter("fhirServerUrl", "https://hapi.fhir.org/baseR4");
		workItem.setParameter("fhirTerminologyServerUrl", "https://hapi.fhir.org/baseR4");
		workItem.setParameter("planDefinitionUrl", "http://example.org/hello-cds-world");
		workItem.setParameter("patientId", "should-screen-ccs");
		workItem.setParameter("context_patientId", "should-screen-ccs");
		NoOpWorkItemManager manager = new NoOpWorkItemManager();
		long start = System.currentTimeMillis();
		handler.executeWorkItem(workItem, manager);
		long time = System.currentTimeMillis() - start;
		System.out.println("Execution time: " + time);
		Assert.assertNotNull(workItem.getResults());
		Assert.assertNull(workItem.getResult("error"));		
		Assert.assertNotNull(workItem.getResult("cardsJson"));
		System.out.println("Time to run 1st time (ms): " + time);
		Assert.assertEquals(true, manager.isCompleted());
		Assert.assertNotNull(workItem.getResults());
	}

	@Test
	public void testReflection() throws Exception {
		ClassLoader cl = getClass().getClassLoader();
		PlanDefinition.PlanDefinitionActionComponent action = new PlanDefinition.PlanDefinitionActionComponent();
		action.setSelectionBehavior(ActionSelectionBehavior.ANY);
		Object act = new RequestGroup.RequestGroupActionComponent();
		Class<?> asbClass = cl.loadClass("org.hl7.fhir.r4.model.PlanDefinition$ActionSelectionBehavior");
		Class<?> rgasbClass = cl.loadClass("org.hl7.fhir.r4.model.RequestGroup$ActionSelectionBehavior");
		Object actSelBehavior = action.getClass().getMethod("getSelectionBehavior").invoke(action);
		Object actSelBehaviorName = asbClass.getMethod("name").invoke(actSelBehavior);
		Object selBehavior = rgasbClass.getMethod("valueOf", String.class).invoke(null, actSelBehaviorName);
		act.getClass().getMethod("setSelectionBehavior", rgasbClass).invoke(act, selBehavior);
	}
	
	@Test
	public void testProvidedPlanDefJson() throws Exception {
		if (System.getProperty("fhirServerToken") == null) {
			return;
		}
		String token = System.getProperty("fhirServerToken");
		PlanDefCdsInlineWorkItemHandler handler = new PlanDefCdsInlineWorkItemHandler();
		WorkItemImpl workItem = new WorkItemImpl();
		
		workItem.setParameter("fhirServerAuth", "Bearer " + token);
		workItem.setParameter("fhirServerUrl", "https://api.logicahealth.org/cdcgc/data");
		//workItem.setParameter("fhirServerUrl", "https://fhir4-internal.elimuinformatics.com/fhir");
		//workItem.setParameter("fhirServer_header_Epic__Client__ID", "test-value-1");
		workItem.setParameter("fhirTerminologyServerUrl", "https://fhir4-terminology-sandbox.elimuinformatics.com/baseR4");
		//workItem.setParameter("fhirTerminologyServer_header_Epic__Client__ID", "test-value-2");
		workItem.setParameter("planDefinitionUrl", "http://elimu.io/PlanDefinition/SimpleGonorrheaCDS");
		Bundle bundle = new Bundle();
		bundle.setType(Bundle.BundleType.COLLECTION);
		MedicationRequest mreq = new MedicationRequest();
		mreq.setId("medrx0325");
		mreq.setStatus(MedicationRequest.MedicationRequestStatus.DRAFT);
		mreq.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);
		mreq.addCategory().setText("Inpatient").addCoding().setCode("inpatient").setSystem("http://terminology.hl7.org/CodeSystem/medicationrequest-category").setDisplay("Inpatient");
		mreq.setMedication(new Reference().setReference("Medication/eWZNFft0XQRiRjSYPyNsf8w3").setDisplay("CEFTRIAXONE 250 MG SOLUTION FOR INJECTION"));
		mreq.setSubject(new Reference().setReference("Patient/eCWvPpzzlvY3RVsspc7TKiw3").setDisplay("Zzzrsh, Gonotwentyfour"));
		mreq.setEncounter(new Reference().setDisplay("Office Visit").setReference("Encounter/eKhmYI-wOnGOK1xPgpVID7Q3").
				setIdentifier(new Identifier().setUse(Identifier.IdentifierUse.USUAL).setSystem("urn:oid:1.2.840.114350.1.13.301.3.7.3.698084.8").setValue("80399662")));
		mreq.setRequester(new Reference().setType("Practitioner").setDisplay("Physician Family Medicine, MD"));
		mreq.setRecorder(new Reference().setType("Practitioner").setDisplay("Physician Family Medicine, MD"));
		Dosage dos1 = mreq.addDosageInstruction();
		SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dos1.getTiming().getRepeat().setBounds(new Period().setStart(FORMAT.parse("2023-03-03T14:00:00Z"))).setFrequency(1).setPeriod(1).setPeriodUnit(UnitsOfTime.D);
		dos1.getTiming().getCode().setText("Daily");
		dos1.getRoute().addCoding().setCode("78421000").setSystem("http://snomed.info/sct").setDisplay("Intramuscular route (qualifier value)");
		dos1.getRoute().addCoding().setCode("6").setSystem("urn:oid:1.2.840.114350.1.13.301.3.7.4.798268.7025").setDisplay("Intramuscular");
		dos1.getRoute().setText("Intramuscular");
		dos1.addDoseAndRate().setType(new CodeableConcept(new Coding("http://epic.com/CodeSystem/dose-rate-type", "calculated", "calculated")).setText("calculated")).
			getDoseQuantity().setValue(250).setUnit("mg").setCode("mg").setSystem("http://unitsofmeasure.org");
		dos1.addDoseAndRate().setType(new CodeableConcept(new Coding("http://epic.com/CodeSystem/dose-rate-type", "admin-amount", "admin-amount")).setText("admin-amount")).
			getDoseQuantity().setValue(1).setUnit("mL").setCode("mL").setSystem("http://unitsofmeasure.org");
		dos1.addDoseAndRate().setType(new CodeableConcept(new Coding("http://epic.com/CodeSystem/dose-rate-type", "ordered", "ordered")).setText("ordered")).
			getDoseQuantity().setValue(250).setUnit("mg").setCode("mg").setSystem("http://unitsofmeasure.org");
		bundle.addEntry().setResource(mreq);
		workItem.setParameter("context_selections", Arrays.asList("MedicationRequest/medrx0325"));
		workItem.setParameter("context_draftOrders", bundle);
		workItem.setParameter("context_hook", "order-select");
		workItem.setParameter("context_hookInstance", UUID.randomUUID().toString());
		//workItem.setParameter("patientId", "smart-1288992");
		//workItem.setParameter("context_patientId", "smart-1288992");
		workItem.setParameter("context_encounterId", "89284");
		workItem.setParameter("context_patientId", "SMART-436610");
		workItem.setParameter("patientId", "SMART-436610");
		workItem.setParameter("context_userId", "Practitioner/example");

		NoOpWorkItemManager manager = new NoOpWorkItemManager();
		long start = System.currentTimeMillis();
		handler.executeWorkItem(workItem, manager);
		for (String key : workItem.getResults().keySet()) {
			System.out.println(" - " + key + ": " + workItem.getResult(key));
		}
		long time = System.currentTimeMillis() - start;
		Assert.assertNotNull(workItem.getResults());
		Assert.assertNull(workItem.getResult("error"));		
		Assert.assertNotNull(workItem.getResult("cardsJson"));
		System.out.println("Time to run 1st time (ms): " + time);
		Assert.assertEquals(true, manager.isCompleted());
		Assert.assertNotNull(workItem.getResults());
		Assert.assertNotNull(workItem.getResult("cards"));
		List<?> cards = (List<?>) workItem.getResult("cards");
		Assert.assertEquals(1, cards.size());
		Card card = (Card) cards.get(0);
		Assert.assertNotNull(card);
		Assert.assertEquals("Attention: Antibiotic Stewardship", card.getSummary());
		Assert.assertEquals("If the current ceftriaxone order is presumptive treatment for gonorrhea, please note that the CDC's "
				+ "updated guidance is that for patients weighing <150 kg (presumably) and with no beta-lactam allergy, the "
				+ "recommended dose is ceftriaxone 500 mg IM x1. Pt has a ceftriaxone order dose 250", card.getDetail());
		Assert.assertEquals("warning", card.getIndicator());
		Assert.assertEquals("any", card.getSelectionBehavior());
		Assert.assertNotNull(card.getOverrideReasons());
		Assert.assertEquals(2, card.getOverrideReasons().size());
		for (JSONObject ovReason : card.getOverrideReasons()) {
			Assert.assertNotNull(ovReason.get("display"));
			Assert.assertTrue(Arrays.asList("Patient refused", "Incorrect diagnosis").contains(ovReason.get("display")));
		}
		Assert.assertNotNull(card.getSuggestions());
		Assert.assertEquals(1, card.getSuggestions().size());
		System.out.println("CARDS JSON");
		System.out.println(workItem.getResult("cardsJson"));
		System.out.println("CQL OUTPUTS:"); 
		for (String key : workItem.getResults().keySet()) {
			if (key.startsWith("cql_")) {
				System.out.println(key + ": " + workItem.getResult(key));
			}
		}
	}
	
	@Test
	public void testRemoveAction() throws Exception {
		if (System.getProperty("fhirServerToken") == null) {
			return;
		}
		String token = System.getProperty("fhirServerToken");
		PlanDefCdsInlineWorkItemHandler handler = new PlanDefCdsInlineWorkItemHandler();
		WorkItemImpl workItem = new WorkItemImpl();
		
		workItem.setParameter("fhirServerAuth", "Bearer " + token);
		workItem.setParameter("fhirServerUrl", "https://api.logicahealth.org/cdcgc/data");
		//workItem.setParameter("fhirServerUrl", "https://fhir4-internal.elimuinformatics.com/fhir");
		workItem.setParameter("fhirTerminologyServerUrl", "https://fhir4-terminology-sandbox.elimuinformatics.com/baseR4");
		workItem.setParameter("planDefinitionJson", "{\n"
				+ "    \"resourceType\": \"PlanDefinition\",\n"
				+ "    \"id\": \"SimpleGonorrheaCDS\",\n"
				+ "    \"meta\": {\n"
				+ "        \"versionId\": \"24\",\n"
				+ "        \"lastUpdated\": \"2023-04-28T15:37:06.725+00:00\",\n"
				+ "        \"source\": \"#EoVfoqC0yWt6cO0w\"\n"
				+ "    },\n"
				+ "    \"url\": \"http://elimu.io/PlanDefinition/SimpleGonorrheaCDS\",\n"
				+ "    \"name\": \"SimpleGonorrheaCDS\",\n"
				+ "    \"title\": \"Simple Gonorrhea Management CDS for Hooks Testing\",\n"
				+ "    \"type\": {\n"
				+ "        \"coding\": [\n"
				+ "            {\n"
				+ "                \"system\": \"http://terminology.hl7.org/CodeSystem/plan-definition-type\",\n"
				+ "                \"code\": \"eca-rule\",\n"
				+ "                \"display\": \"ECA Rule\"\n"
				+ "            }\n"
				+ "        ]\n"
				+ "    },\n"
				+ "    \"status\": \"draft\",\n"
				+ "    \"experimental\": true,\n"
				+ "    \"date\": \"2022-10-03\",\n"
				+ "    \"publisher\": \"PHII and Elimu Informatics\",\n"
				+ "    \"description\": \"Propose gonorrhea management in conformance with latest guidelines\",\n"
				+ "    \"useContext\": [\n"
				+ "        {\n"
				+ "            \"code\": {\n"
				+ "                \"system\": \"http://terminology.hl7.org/CodeSystem/usage-context-type\",\n"
				+ "                \"code\": \"focus\",\n"
				+ "                \"display\": \"Clinical Focus\"\n"
				+ "            },\n"
				+ "            \"valueCodeableConcept\": {\n"
				+ "                \"coding\": [\n"
				+ "                    {\n"
				+ "                        \"system\": \"http://snomed.info/sct\",\n"
				+ "                        \"code\": \"15628003\",\n"
				+ "                        \"display\": \"Gonorrhea\"\n"
				+ "                    }\n"
				+ "                ]\n"
				+ "            }\n"
				+ "        }\n"
				+ "    ],\n"
				+ "    \"library\": [\n"
				+ "        \"http://elimu.io/Library/SimpleGonorrheaCDS2\"\n"
				+ "    ],\n"
				+ "    \"action\": [\n"
				+ "        {\n"
				+ "            \"documentation\": [\n"
				+ "                {\n"
				+ "                    \"type\": \"documentation\",\n"
				+ "                    \"display\": \"CDC's (STI) Treatment Guidelines, 2021\",\n"
				+ "                    \"url\": \"https://www.cdc.gov/std/treatment-guidelines/default.htm\"\n"
				+ "                }\n"
				+ "            ],\n"
				+ "            \"trigger\": [\n"
				+ "                {\n"
				+ "                    \"type\": \"named-event\",\n"
				+ "                    \"name\": \"order-sign\"\n"
				+ "                }\n"
				+ "            ],\n"
				+ "            \"condition\": [\n"
				+ "                {\n"
				+ "                    \"kind\": \"applicability\",\n"
				+ "                    \"expression\": {\n"
				+ "                        \"description\": \"New diagnosis of urogenital/anorectal gonorrhea and meets criteria for 500 mg dose of ceftriaxone\",\n"
				+ "                        \"language\": \"text/cql-identifier\",\n"
				+ "                        \"expression\": \"HasIncorrectDoseCard\"\n"
				+ "                    }\n"
				+ "                }\n"
				+ "            ],\n"
				+ "            \"participant\": [\n"
				+ "                {\n"
				+ "                    \"type\": \"practitioner\"\n"
				+ "                }\n"
				+ "            ],\n"
				+ "            \"type\": {\n"
				+ "                \"coding\": [\n"
				+ "                    {\n"
				+ "                        \"system\": \"http://terminology.hl7.org/CodeSystem/action-type\",\n"
				+ "                        \"code\": \"create\"\n"
				+ "                    }\n"
				+ "                ]\n"
				+ "            },\n"
				+ "            \"groupingBehavior\": \"logical-group\",\n"
				+ "            \"selectionBehavior\": \"any\",\n"
				+ "            \"dynamicValue\": [\n"
				+ "                {\n"
				+ "                    \"path\": \"action.title\",\n"
				+ "                    \"expression\": {\n"
				+ "                        \"language\": \"text/cql-identifier\",\n"
				+ "                        \"expression\": \"CardTitle\"\n"
				+ "                    }\n"
				+ "                },\n"
				+ "                {\n"
				+ "                    \"path\": \"action.description\",\n"
				+ "                    \"expression\": {\n"
				+ "                        \"language\": \"text/cql-identifier\",\n"
				+ "                        \"expression\": \"CardDetailTextGCOrderSign\"\n"
				+ "                    }\n"
				+ "                },\n"
				+ "                {\n"
				+ "                    \"path\": \"action.extension\",\n"
				+ "                    \"expression\": {\n"
				+ "                        \"language\": \"text/cql.identifier\",\n"
				+ "                        \"expression\": \"CardIndicatorCategory\"\n"
				+ "                    }\n"
				+ "                },\n"
				+ "                {\n"
				+ "                    \"path\": \"action.extension.override\",\n"
				+ "                    \"expression\": {\n"
				+ "                        \"language\": \"text/cql.identifier\",\n"
				+ "                        \"expression\": \"overrideReasonsForCard1\"\n"
				+ "                    }\n"
				+ "                }\n"
				+ "            ],\n"
				+ "            \"action\": [\n"
				+ "                {\n"
				+ "                    \"description\": \"Remove Ceftriaxone draft order\",\n"
				+ "                    \"type\": {\n"
				+ "                        \"coding\": [\n"
				+ "                            {\n"
				+ "                                \"system\": \"http://terminology.hl7.org/CodeSystem/action-type\",\n"
				+ "                                \"code\": \"remove\"\n"
				+ "                            }\n"
				+ "                        ]\n"
				+ "                    },\n"
				+ "                    \"dynamicValue\": [\n"
				+ "                        {\n"
				+ "                            \"path\": \"action.id\",\n"
				+ "                            \"expression\": {\n"
				+ "                                \"language\": \"text/cql.identifier\",\n"
				+ "                                \"expression\": \"DraftCetriaxoneOrderId\"\n"
				+ "                            }\n"
				+ "                        }\n"
				+ "                    ]\n"
				+ "                },\n"
				+ "                {\n"
				+ "                    \"description\": \"Ceftriaxone 500 mg order\",\n"
				+ "                    \"precheckBehavior\": \"yes\",\n"
				+ "                    \"definitionCanonical\": \"http://elimu.io/ActivityDefinition/Ceftriaxone500OrderProposal\"\n"
				+ "                },\n"
				+ "                {\n"
				+ "                    \"condition\": [\n"
				+ "                        {\n"
				+ "                            \"kind\": \"applicability\",\n"
				+ "                            \"expression\": {\n"
				+ "                                \"description\": \"Meets criteria for Infectious Disease referral\",\n"
				+ "                                \"language\": \"text/cql-identifier\",\n"
				+ "                                \"expression\": \"IsFemaleAndPregnant\"\n"
				+ "                            }\n"
				+ "                        }\n"
				+ "                    ],\n"
				+ "                    \"precheckBehavior\": \"no\",\n"
				+ "                    \"definitionCanonical\": \"http://elimu.io/ActivityDefinition/IDReferral\"\n"
				+ "                }\n"
				+ "            ]\n"
				+ "        }\n"
				+ "    ]\n"
				+ "}");
		Bundle bundle = new Bundle();
		bundle.setType(Bundle.BundleType.COLLECTION);
		MedicationRequest mreq = new MedicationRequest();
		mreq.setId("medrx0325");
		mreq.setStatus(MedicationRequest.MedicationRequestStatus.DRAFT);
		mreq.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);
		mreq.addCategory().setText("Inpatient").addCoding().setCode("inpatient").setSystem("http://terminology.hl7.org/CodeSystem/medicationrequest-category").setDisplay("Inpatient");
		mreq.setMedication(new Reference().setReference("Medication/14652").setDisplay("CEFTRIAXONE 250 MG SOLUTION FOR INJECTION"));
		mreq.setSubject(new Reference().setReference("Patient/eCWvPpzzlvY3RVsspc7TKiw3").setDisplay("Zzzrsh, Gonotwentyfour"));
		mreq.setEncounter(new Reference().setDisplay("Office Visit").setReference("Encounter/eKhmYI-wOnGOK1xPgpVID7Q3").
				setIdentifier(new Identifier().setUse(Identifier.IdentifierUse.USUAL).setSystem("urn:oid:1.2.840.114350.1.13.301.3.7.3.698084.8").setValue("80399662")));
		mreq.setRequester(new Reference().setType("Practitioner").setDisplay("Physician Family Medicine, MD"));
		mreq.setRecorder(new Reference().setType("Practitioner").setDisplay("Physician Family Medicine, MD"));
		Dosage dos1 = mreq.addDosageInstruction();
		SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dos1.getTiming().getRepeat().setBounds(new Period().setStart(FORMAT.parse("2023-03-03T14:00:00Z"))).setFrequency(1).setPeriod(1).setPeriodUnit(UnitsOfTime.D);
		dos1.getTiming().getCode().setText("Daily");
		dos1.getRoute().addCoding().setCode("78421000").setSystem("http://snomed.info/sct").setDisplay("Intramuscular route (qualifier value)");
		dos1.getRoute().addCoding().setCode("6").setSystem("urn:oid:1.2.840.114350.1.13.301.3.7.4.798268.7025").setDisplay("Intramuscular");
		dos1.getRoute().setText("Intramuscular");
		dos1.addDoseAndRate().setType(new CodeableConcept(new Coding("http://epic.com/CodeSystem/dose-rate-type", "admin-amount", "admin-amount")).setText("admin-amount")).
			getDoseQuantity().setValue(1).setUnit("mL").setCode("mL").setSystem("http://unitsofmeasure.org");
		dos1.addDoseAndRate().setType(new CodeableConcept(new Coding("http://epic.com/CodeSystem/dose-rate-type", "calculated", "calculated")).setText("calculated")).
		getDoseQuantity().setValue(250).setUnit("mg").setCode("mg").setSystem("http://unitsofmeasure.org");
		dos1.addDoseAndRate().setType(new CodeableConcept(new Coding("http://epic.com/CodeSystem/dose-rate-type", "ordered", "ordered")).setText("ordered")).
			getDoseQuantity().setValue(250).setUnit("mg").setCode("mg").setSystem("http://unitsofmeasure.org");
		bundle.addEntry().setResource(mreq);
		workItem.setParameter("context_selections", Arrays.asList("MedicationRequest/medrx0325"));
		workItem.setParameter("context_draftOrders", bundle);
		workItem.setParameter("patientId", "SMART-436610");
		workItem.setParameter("context_patientId", "SMART-436610");
		workItem.setParameter("context_userId", "Practitioner/example");
		workItem.setParameter("context_encounterId", "89284");
		NoOpWorkItemManager manager = new NoOpWorkItemManager();
		long start = System.currentTimeMillis();
		System.out.println("About to start call...");
		handler.executeWorkItem(workItem, manager);
		long time = System.currentTimeMillis() - start;
		System.out.println("Time to run 1st time (ms): " + time);
		System.out.println(workItem.getResult("cardsJson"));
		Assert.assertEquals(true, manager.isCompleted());
		Assert.assertNotNull(workItem.getResult("cards"));
		List<?> cards = (List<?>) workItem.getResult("cards");
		Assert.assertFalse(cards.isEmpty());
		boolean found = false;
		for (Object c : cards) {
			Card card = (Card) c;
			if ("Remove Ceftriaxone draft order".equals(card.getDetail())) {
				found = true;
				Assert.assertNotNull(card.getSuggestions());
				Assert.assertEquals(1, card.getSuggestions().size());
				Suggestion suggestion = card.getSuggestions().get(0);
				Assert.assertNotNull(suggestion.getLabel());
				Assert.assertEquals("Remove Ceftriaxone draft order", suggestion.getLabel());
				Assert.assertNotNull(suggestion.getUuid());
				Assert.assertNotNull(suggestion.getActions());
				List<JSONObject> actions = suggestion.getActions();
				Assert.assertFalse(actions.isEmpty());
				Assert.assertEquals(1, actions.size());
				JSONObject action = actions.get(0);
				Assert.assertNotNull(action.get("type"));
				Assert.assertEquals("delete", action.get("type").toString());
			}
		}
		Assert.assertTrue(found);

	}
	
	@Test
	@Ignore //Ignoring because its failing the builds, someone is working on it. So, we are going to ignore it for now 
	public void testInlineCall() throws Exception {
		PlanDefCdsInlineWorkItemHandler handler = new PlanDefCdsInlineWorkItemHandler();
		WorkItemImpl workItem = new WorkItemImpl();
		//workItem.setParameter("fhirServerUrl", "https://cqf-internal.elimuinformatics.com/fhir");
		//workItem.setParameter("fhirTerminologyServerUrl", "https://cqf-internal.elimuinformatics.com/fhir");
		workItem.setParameter("fhirServerUrl", "https://hapi.fhir.org/baseR4");
		workItem.setParameter("fhirTerminologyServerUrl", "https://hapi.fhir.org/baseR4");
		workItem.setParameter("planDefinitionId", "ColorectalCancerScreeningCDS");
		workItem.setParameter("patientId", "should-screen-ccs");
		/*Patient patient = new Patient();
		patient.setId("should-screen-ccs");
		patient.addExtension().setUrl("http://hl7.org/fhir/us/core/StructureDefinition/us-core-race").addExtension().setUrl("obmCategory").
			setValue(new Coding().setSystem("urn:oid:2.16.840.1.113883.6.238").setCode("2028-9").setDisplay("Asian"));
		patient.addExtension().setUrl("http://hl7.org/fhir/us/core/StructureDefinition/us-core-ethnicity").addExtension().setUrl("obmCategory").
			setValue(new Coding().setSystem("urn:oid:2.16.840.1.113883.6.238").setCode("2135-2").setDisplay("Hispanic or Latino"));
		CodeableConcept mrn = new CodeableConcept();
		mrn.addCoding().setSystem("http://terminology.hl7.org/CodeSystem/v2-0203").setCode("MR").setDisplay("Medical Record Number");
		patient.addIdentifier().setUse(IdentifierUse.USUAL).setType(mrn).setSystem("http://hospital.smarthealthit.org").setValue("999999992");
		patient.addName().setFamily("Dere").addGiven("Ben");
		patient.setGender(AdministrativeGender.MALE);
		patient.setBirthDate(new SimpleDateFormat("yyyy-MM-dd").parse("1968-01-01"));
		workItem.setParameter("prefetch_patient", patient);*/
		workItem.setParameter("context_patientId", "should-screen-ccs");
		NoOpWorkItemManager manager = new NoOpWorkItemManager();
		long start = System.currentTimeMillis();
		handler.executeWorkItem(workItem, manager);
		long time = System.currentTimeMillis() - start;
		System.out.println("Time to run 1st time (ms): " + time);
		Assert.assertEquals(true, manager.isCompleted());
		Assert.assertNotNull(workItem.getResults());
		Assert.assertNotNull(workItem.getResult("cardsJson"));
		Assert.assertNotNull(workItem.getResult("cards"));
		List<?> cards = (List<?>) workItem.getResult("cards");
		Assert.assertNotNull(cards);
		Assert.assertTrue(cards.size() > 0 );
		Card card = (Card) cards.get(0);
		Assert.assertNotNull(card);
		Assert.assertNotNull(card.getSummary());
		Assert.assertEquals("Recommend appropriate colorectal cancer screening", card.getSummary());
		Assert.assertNotNull(card.getIndicator());
		Assert.assertEquals("WARN", card.getIndicator());;
		Assert.assertNotNull(card.getDetail());
		Assert.assertEquals("Patient meets the inclusion criteria for appropriate colorectal cancer screening, but has no evidence of appropriate screening.", card.getDetail());
		Assert.assertNotNull(card.getSource());
		Assert.assertNotNull(((JSONObject) card.getSource()).get("label"));
		Assert.assertEquals("U.S. Preventive Services Task Force Final Recommendation Statement Colorectal Cancer: Screening", ((JSONObject) card.getSource()).get("label"));
		Assert.assertNotNull(((JSONObject) card.getSource()).get("url"));
		Assert.assertEquals("https://www.uspreventiveservicestaskforce.org/uspstf/recommendation/colorectal-cancer-screening", ((JSONObject) card.getSource()).get("url"));
		
		workItem.setParameter("patientId", "should-not-screen-ccs");
		workItem.getResults().clear();
		start = System.currentTimeMillis();
		handler.executeWorkItem(workItem, manager);
		time = System.currentTimeMillis() - start;
		System.out.println("Time to run 2nd time (ms): " + time);
		
		workItem.setParameter("context_patientId", "something-else");
		workItem.setParameter("patientId", "something-else");
		workItem.getResults().clear();
		handler.executeWorkItem(workItem, manager);
		Assert.assertNotNull(workItem.getResults());
		Assert.assertTrue(workItem.getResults().containsKey("error"));
    }
}
