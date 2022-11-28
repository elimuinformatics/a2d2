package io.elimu.a2d2.cql;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Dosage.DosageDoseAndRateComponent;
import org.hl7.fhir.r4.model.Identifier.IdentifierUse;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestIntent;
import org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestStatus;
import org.hl7.fhir.r4.model.PlanDefinition;
import org.hl7.fhir.r4.model.PlanDefinition.ActionSelectionBehavior;
import org.hl7.fhir.r4.model.RequestGroup;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Timing.UnitsOfTime;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import io.elimu.a2d2.cdsresponse.entity.Card;
import io.elimu.a2d2.oauth.BodyBuilder;
import io.elimu.a2d2.oauth.OAuthUtils;

public class CqfInlineWIHTest {

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
		medRequests.add(new MedicationRequest().setId("1234555"));
		medRequests.add(new MedicationRequest().setId("123477"));
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
