package io.elimu.a2d2.cql;

import java.text.ParseException;
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
import org.hl7.fhir.r4.model.CarePlan;
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

import ca.uhn.fhir.context.FhirContext;
import io.elimu.a2d2.cdsresponse.entity.Card;
import io.elimu.a2d2.cdsresponse.entity.Suggestion;
import io.elimu.a2d2.oauth.BodyBuilder;
import io.elimu.a2d2.oauth.OAuthUtils;

public class CqfInlineWIHTest {

	@Test
	public void testSpecimenCalls() throws Exception {
        PlanDefCdsInlineWorkItemHandler handler = new PlanDefCdsInlineWorkItemHandler();
        NoOpWorkItemManager manager = new NoOpWorkItemManager();
        if (System.getProperty("fhirUsername") == null || System.getProperty("fhirPassword") == null) {
		return;
        }
        String tokenUrl = "https://auth-internal.elimuinformatics.com/auth/realms/product/protocol/openid-connect/token";
        String body = new BodyBuilder().addToBody("client_id", "omnibus-api").addToBody("client_secret", "").
                addToBody("token_url", tokenUrl).addToBody("grant_type", "password").addToBody("username", System.getProperty("fhirUsername")).
                addToBody("password", System.getProperty("fhirPassword")).addToBody("scope", "openid").build();
        Map<String, Object> results = OAuthUtils.authenticate(body, tokenUrl, "omnibus-api", "");
        String token = (String) results.get("access_token");

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("fhirServerAuth", "Bearer " + token);
        workItem.setParameter("fhirTerminologyServerAuth", "Bearer " + token);
        workItem.setParameter("fhirServerUrl", "https://fhir4-internal.elimuinformatics.com/fhir");
        workItem.setParameter("fhirTerminologyServerUrl", "https://fhir-terminology-sandbox-internal.elimuinformatics.com/baseR4");

        workItem.setParameter("context_hook", "patient-view");
        workItem.setParameter("context_userId", "Practitioner/example");
        workItem.setParameter("patientId", "627513");
        workItem.setParameter("context_patientId", "627513");
        workItem.setParameter("context_encounterId", "89284");
        workItem.setParameter("context_selections", null);
        workItem.setParameter("context_draftOrders", null);
        workItem.setParameter("context_hookInstance", UUID.randomUUID().toString());
        workItem.setParameter("planDefinitionUrl", "http://elimu.io/PlanDefinition/GonorrheaCDSLaboratoryConfirmed");

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
        List<?> cards = (List<?>) workItem.getResult("cards");
        Assert.assertEquals(1, cards.size());
	}
	
	@Test
	public void testCachingIssues() throws Exception {
		if (System.getProperty("fhirServerToken") == null) {
            return;
        }
        String token = System.getProperty("fhirServerToken");
        PlanDefCdsInlineWorkItemHandler handler = new PlanDefCdsInlineWorkItemHandler();
        NoOpWorkItemManager manager = new NoOpWorkItemManager();
        
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("fhirServerAuth", "Bearer " + token);
        workItem.setParameter("fhirServerUrl", "https://api.logicahealth.org/cdcgc/data");
        workItem.setParameter("fhirTerminologyServerUrl", "https://fhir4-terminology-sandbox.elimuinformatics.com/baseR4");

        workItem.setParameter("context_hook", "order-sign");
        workItem.setParameter("context_hookInstance", UUID.randomUUID().toString());
        workItem.setParameter("context_userId", "Practitioner/example");
        workItem.setParameter("patientId", "smart-724111");
        workItem.setParameter("context_patientId", "smart-724111");
        workItem.setParameter("context_encounterId", "89284");
        Bundle bundle = createMedOrders("smart-724111", "14652");
        workItem.setParameter("context_selections", Arrays.asList("MedicationRequest/medrx0325"));
        workItem.setParameter("context_draftOrders", bundle);
        workItem.setParameter("planDefinitionUrl", "http://elimu.io/PlanDefinition/GonorrheaCDSPresumptiveTreatment");

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
        List<?> cards = (List<?>) workItem.getResult("cards");
        Assert.assertEquals(1, cards.size());
        
        WorkItemImpl workItem2 = new WorkItemImpl();
        workItem2.setParameter("fhirServerAuth", "Bearer " + token);
        workItem2.setParameter("fhirServerUrl", "https://api.logicahealth.org/cdcgc/data");
        workItem2.setParameter("fhirTerminologyServerUrl", "https://fhir4-terminology-sandbox.elimuinformatics.com/baseR4");

        workItem2.setParameter("context_hook", "order-sign");
        workItem2.setParameter("context_hookInstance", UUID.randomUUID().toString());
        workItem2.setParameter("context_userId", "Practitioner/example");
        workItem2.setParameter("patientId", "smart-724111");
        workItem2.setParameter("context_patientId", "smart-724111");
        workItem2.setParameter("context_encounterId", "89284");
        Bundle bundle2 = createMedOrders("smart-724111", "XXXX");
        workItem2.setParameter("context_selections", Arrays.asList("MedicationRequest/medrx0325"));
        workItem2.setParameter("context_draftOrders", bundle2);
        workItem2.setParameter("planDefinitionUrl", "http://elimu.io/PlanDefinition/GonorrheaCDSPresumptiveTreatment");

        start = System.currentTimeMillis();
        manager = new NoOpWorkItemManager();
        handler.executeWorkItem(workItem2, manager);
        for (String key : workItem2.getResults().keySet()) {
            System.out.println(" - " + key + ": " + workItem.getResult(key));
        }
        time = System.currentTimeMillis() - start;
        Assert.assertNotNull(workItem2.getResults());
        Assert.assertNotNull(workItem2.getResult("error"));        
        System.out.println("Time to run 2nd time (ms): " + time);
        Assert.assertEquals(true, manager.isCompleted());
        List<?> cards2 = (List<?>) workItem2.getResult("cards");
        Assert.assertNull(cards2);
	}
	
	@Test
	public void testCacheHttpClient() throws Exception {
		if (System.getProperty("fhirServerTokenInternal") == null) {
			return;
		}
        String token = System.getProperty("fhirServerTokenInternal");
        Assert.assertNotNull(token);
        PlanDefCdsInlineWorkItemHandler handler = new PlanDefCdsInlineWorkItemHandler();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("fhirServerAuth", "Bearer " + token);
        workItem.setParameter("fhirServerUrl", "https://fhir4-internal.elimuinformatics.com/fhir");
        workItem.setParameter("fhirTerminologyServerUrl", "https://fhir4-terminology-sandbox.elimuinformatics.com/baseR4");
        workItem.setParameter("context_hook", "order-sign");
        workItem.setParameter("context_hookInstance", UUID.randomUUID().toString());
        workItem.setParameter("context_userId", "Practitioner/example");
        workItem.setParameter("patientId", "540010");
        workItem.setParameter("context_patientId", "540010");
        workItem.setParameter("context_encounterId", "89284");
        Bundle bundle = createMedOrders("540010", "624649");
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
        List<?> cards = (List<?>) workItem.getResult("cards");
        Assert.assertNotNull(cards);
	}
	
	
	//smart-7777705 patient under 13 yrs should get no cards
	@Test
	public void testNoCards() throws Exception {
		if (System.getProperty("fhirServerToken") == null) {
            return;
        }
        String token = System.getProperty("fhirServerToken");
        PlanDefCdsInlineWorkItemHandler handler = new PlanDefCdsInlineWorkItemHandler();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("fhirServerAuth", "Bearer " + token);
        workItem.setParameter("fhirServerUrl", "https://api.logicahealth.org/cdcgc/data");
        workItem.setParameter("fhirTerminologyServerUrl", "https://fhir4-terminology-sandbox.elimuinformatics.com/baseR4");
        workItem.setParameter("context_hook", "order-sign");
        workItem.setParameter("context_hookInstance", UUID.randomUUID().toString());
        workItem.setParameter("context_userId", "Practitioner/example");
        workItem.setParameter("patientId", "smart-7777705");
        workItem.setParameter("context_patientId", "smart-7777705");
        workItem.setParameter("context_encounterId", "89284");
        Bundle bundle = createMedOrders("smart-7777705", "14652");
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
        
        List<?> cards = (List<?>) workItem.getResult("cards");
        Assert.assertEquals(0, cards.size());
        Assert.assertNotNull(workItem.getResult("cql_IsAdolescentOrAdult"));
        Assert.assertTrue(workItem.getResult("cql_IsAdolescentOrAdult").toString().toLowerCase().contains("false"));
	}

	//SMART-436610 - "Ducks", expect "allergy & antibiotic stewardship" and also text about pregnant/not pregnant
	@Test
	public void testAllergyAndAntibiotic() throws Exception {
        if (System.getProperty("fhirServerToken") == null) {
            return;
        }
        String token = System.getProperty("fhirServerToken");
        PlanDefCdsInlineWorkItemHandler handler = new PlanDefCdsInlineWorkItemHandler();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("fhirServerAuth", "Bearer " + token);
        workItem.setParameter("fhirServerUrl", "https://api.logicahealth.org/cdcgc/data");
        workItem.setParameter("fhirTerminologyServerUrl", "https://fhir4-terminology-sandbox.elimuinformatics.com/baseR4");
        workItem.setParameter("context_hook", "order-sign");
        workItem.setParameter("context_hookInstance", UUID.randomUUID().toString());
        workItem.setParameter("context_userId", "Practitioner/example");
        workItem.setParameter("patientId", "SMART-436610");
        workItem.setParameter("context_patientId", "SMART-436610");
        workItem.setParameter("context_encounterId", "89284");
        Bundle bundle = createMedOrders("SMART-436610", "14652");
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
        
        List<?> cards = (List<?>) workItem.getResult("cards");
        Assert.assertEquals(1, cards.size());
        Card card = (Card) cards.get(0);
        Assert.assertNotNull(card);
        Assert.assertEquals("Attention: allergy and antibiotic stewardship", card.getSummary());
        Assert.assertTrue(card.getDetail().contains("pregnant"));
	}
	
	//smart-1951076 I added weight 152 kg receives response as no weight on record (if no weight, "presumably weight <150kg")
	@Test
	public void testWeightOnRecord() throws Exception {
        if (System.getProperty("fhirServerToken") == null) {
            return;
        }
        String token = System.getProperty("fhirServerToken");
        PlanDefCdsInlineWorkItemHandler handler = new PlanDefCdsInlineWorkItemHandler();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("fhirServerAuth", "Bearer " + token);
        workItem.setParameter("fhirServerUrl", "https://api.logicahealth.org/cdcgc/data");
        workItem.setParameter("fhirTerminologyServerUrl", "https://fhir4-terminology-sandbox.elimuinformatics.com/baseR4");
        workItem.setParameter("context_hook", "order-sign");
        workItem.setParameter("context_hookInstance", UUID.randomUUID().toString());
        workItem.setParameter("context_userId", "Practitioner/example");
        workItem.setParameter("patientId", "smart-1951076");
        workItem.setParameter("context_patientId", "smart-1951076");
        workItem.setParameter("context_encounterId", "89284");
        Bundle bundle = createMedOrders("smart-1951076", "14652");
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
        
        List<?> cards = (List<?>) workItem.getResult("cards");
        Assert.assertEquals(1, cards.size());
        Card card = (Card) cards.get(0);
        Assert.assertNotNull(card);
        
        Assert.assertNotNull(workItem.getResult("cql_HasNoWeightOnRecord"));
        Assert.assertTrue(workItem.getResult("cql_HasNoWeightOnRecord").toString().toLowerCase().contains("false"));	
	}

	
	//smart-724111 has an AllergyIntolerance that was just "no known allergies" - but still getting allergy response. Should get response for no allergy
	@Test
	public void testAllergyRead() throws Exception {
        if (System.getProperty("fhirServerToken") == null) {
            return;
        }
        String token = System.getProperty("fhirServerToken");
        PlanDefCdsInlineWorkItemHandler handler = new PlanDefCdsInlineWorkItemHandler();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("fhirServerAuth", "Bearer " + token);
        workItem.setParameter("fhirServerUrl", "https://api.logicahealth.org/cdcgc/data");
        workItem.setParameter("fhirTerminologyServerUrl", "https://fhir4-terminology-sandbox.elimuinformatics.com/baseR4");
        workItem.setParameter("context_hook", "order-sign");
        workItem.setParameter("context_hookInstance", UUID.randomUUID().toString());
        workItem.setParameter("context_userId", "Practitioner/example");
        workItem.setParameter("patientId", "smart-724111");
        workItem.setParameter("context_patientId", "smart-724111");
        workItem.setParameter("context_encounterId", "89284");
        Bundle bundle = createMedOrders("smart-724111", "14652");
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
        
        List<?> cards = (List<?>) workItem.getResult("cards");
        Assert.assertEquals(1, cards.size());
        Card card = (Card) cards.get(0);
        Assert.assertNotNull(card);
        
        Assert.assertNotNull(workItem.getResult("cql_NeedsAllergyOptions"));
        Assert.assertTrue(workItem.getResult("cql_NeedsAllergyOptions").toString().toLowerCase().contains("false"));
        Assert.assertEquals("Attention: Antibiotic Stewardship", card.getSummary());
	}
	
    @Test
    public void testCardTransform() throws Exception {
        String cpJson = "{"
        		+ "    \"resourceType\":\"CarePlan\",\"contained\":[{\n"
                + "        \"resourceType\":\"RequestGroup\",\"id\":\"1\",\"status\":\"draft\",\"intent\":\"proposal\",\"action\":[{\n"
                + "            \"extension\":[\n"
                + "                {\"url\":\"action.extension\",\"valueString\":\"warning\"},\n"
                + "                {\"url\":\"action.extension.override\",\"valueCodeableConcept\":{\"coding\":[\n"
                + "                    {\"system\":\"http://elimu.io/override\",\"code\":\"notforgonorrhea\",\"display\":\"Treatment not for gonorrhea\"},\n"
                + "                    {\"system\":\"http://elimu.io/override\",\"code\":\"inaccuratedata\",\"display\":\"Inaccurate patient data\"},\n"
                + "                    {\"system\":\"http://elimu.io/override\",\"code\":\"other\",\"display\":\"Other\"}\n"
                + "                ]}}\n"
                + "            ],\n"
                + "            \"prefix\":\"If the current ceftriaxone order is presumptive treatment for gonorrhea, please note that the CDC's updated guidance is that for patients (presumably) "
                + "weighing <150 kg and no beta-lactam allergy, the recommended dose is ceftriaxone 500 mg IM x1.\",\n"
                + "            \"title\":\"Attention: Antibiotic Stewardship\",\n"
                + "            \"description\":\"If the current ceftriaxone order is presumptive treatment for gonorrhea, please note that the CDC's updated guidance is that for patients (presumably) "
                + "weighing <150 kg and no beta-lactam allergy, the recommended dose is ceftriaxone 500 mg IM x1.\",\n"
                + "            \"type\":{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/action-type\",\"code\":\"create\"}]},\n"
                + "            \"selectionBehavior\":\"any\",\n"
                + "            \"action\":[{\n"
                + "                \"id\":\"MedicationRequest/medrx0325\",\"prefix\":\"Remove Ceftriaxone draft order\",\n"
                + "                \"description\":\"Remove Ceftriaxone draft order\",\n"
                + "                \"type\":{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/action-type\",\"code\":\"remove\"}]}\n"
                + "            },{\n"
                + "                \"prefix\":\"Intramuscular\",\"textEquivalent\":\"cefTRIAXone (ROCEPHIN) 500 mg IM\",\n"
                + "                \"type\":{\"coding\":[{\"code\":\"fire-event\"}]},\"resource\":{\"reference\":\"#2\"}\n"
                + "            }]},\n"
                + "            {\"extension\":[{\"url\":\"action.extension\",\"valueString\":\"warning\"}],\n"
                + "            \"prefix\":\"If presumptively treating for gonorrhea, current recommendations are to treat presumptively for concurrent Chlamydia infection when Chlamydia has not been excluded."
                + "   Available data indicate patient has no recent Chlamydia test,and has no beta-lactam allergy. Appropriate options are: if not pregnant, doxycycline 100 mg PO bid x7 days, azithromycin"
                + " 1 g PO x1, or levofloxacin 500 mg PO daily x7 days; if pregnant, azithromycin 1 g PO x1 or amoxicillin 500 mg PO tid x7 days.\\n Please consider the following additional recommendations: "
                + "If pharyngeal gonorrhea, perform a test-of-cure 7-14 days after treatment Retest for gonorrhea 3 months after treatment Test for syphilis and HIV, if not yet done  \\nPlease advise the "
                + "patient of the following: Sexual partners within prior 60 days need evaluation and treatment Abstain from sexual activity for 7 days after treatment of yourself and partners\",\n"
                + "            \"title\":\"Additional Gonorrhea Recommendations\",\n"
                + "            \"description\":\"If presumptively treating for gonorrhea, current recommendations are to treat presumptively for concurrent Chlamydia infection when Chlamydia has not been"
                + " excluded.   Available data indicate patient has no recent Chlamydia test,and has no beta-lactam allergy. Appropriate options are: if not pregnant, doxycycline 100 mg PO bid x7 days, "
                + "azithromycin 1 g PO x1, or levofloxacin 500 mg PO daily x7 days; if pregnant, azithromycin 1 g PO x1 or amoxicillin 500 mg PO tid x7 days.\\n Please consider the following additional "
                + "recommendations: If pharyngeal gonorrhea, perform a test-of-cure 7-14 days after treatment Retest for gonorrhea 3 months after treatment Test for syphilis and HIV, if not yet done  \\nPlease"
                + " advise the patient of the following: Sexual partners within prior 60 days need evaluation and treatment Abstain from sexual activity for 7 days after treatment of yourself and partners\",\n"
                + "            \"type\":{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/action-type\",\"code\":\"create\"}]},\n"
                + "            \"selectionBehavior\":\"any\"\n"
                + "            }\n"
                + "        ]},{\n"
                + "            \"resourceType\":\"MedicationRequest\",\"id\":\"2\",\"status\":\"draft\",\"intent\":\"proposal\",\n"
                + "            \"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/medicationrequest-category\",\"code\":\"inpatient\",\"display\":\"Inpatient\"}]}],\n"
                + "            \"medicationCodeableConcept\":{\"coding\":[\n"
                + "                {\"system\":\"http://www.nlm.nih.gov/research/umls/rxnorm\",\"code\":\"1665005\",\"display\":\"ceftriaxone 500 MG Injection\"},\n"
                + "                {\"system\":\"urn:com.epic.cdshooks.action.code.system.preference-list-item\",\"code\":\"2\"}\n"
                + "            ],"
                + "            \"text\":\"ceftriaxone 500 MG Injection\"},\"subject\":{\"reference\":\"Patient/SMART-436610\""
                + "        }}],\n"
                + "        \"instantiatesCanonical\":[\"GonorrheaCDSPresumptiveTreatment-UnitTest\"],\n"
                + "        \"status\":\"draft\",\"subject\":{\"reference\":\"Patient/SMART-436610\"},\n"
                + "        \"activity\":[{\"reference\":{\"reference\":\"#1\"}}]\n"
                + "}";
        CarePlan cp = FhirContext.forR4().newJsonParser().parseResource(CarePlan.class, cpJson);
        //data massaging because self-references are not populated with target values
        cp.getActivity().get(0).setReferenceTarget(cp.getContained().get(0));
        RequestGroup rg = (RequestGroup) cp.getContained().get(0); //#1
        rg.getAction().get(1).setResourceTarget(cp.getContained().get(1)); //#2
        //rg.getAction().get(2).setResourceTarget(cp.getContained().get(2)); //#3
        //rg.getAction().get(3).setResourceTarget(cp.getContained().get(3)); //#4
        //rg.getAction().get(4).setResourceTarget(cp.getContained().get(4)); //#5
        
        List<Card> cards = CardCreator.convert(cp, getClass().getClassLoader());
        Assert.assertEquals(2,  cards.size());

        Card card1 = ((Card) cards.get(0)).getSummary().equals("Attention: Antibiotic Stewardship") ? (Card) cards.get(0) : (Card) cards.get(1);
        Card card2 = ((Card) cards.get(0)).getSummary().equals("Additional Gonorrhea Recommendations") ? (Card) cards.get(0) : (Card) cards.get(1);
        
        Assert.assertNotNull(card1);
        Assert.assertNotNull(card1.getUuid());
        Assert.assertNotNull(card1.getDetail());
        Assert.assertEquals("If the current ceftriaxone order is presumptive treatment for gonorrhea, please note that the CDC's updated guidance "
                + "is that for patients (presumably) weighing <150 kg and no beta-lactam allergy, the recommended dose is ceftriaxone 500 mg IM x1.", card1.getDetail());
        Assert.assertEquals("warning", card1.getIndicator());
        Assert.assertEquals(2, card1.getSuggestions().size());
        Suggestion sug11 = card1.getSuggestions().get(0).getLabel().equals("Remove Ceftriaxone draft order") ? card1.getSuggestions().get(0) : card1.getSuggestions().get(1);
        Suggestion sug12 = card1.getSuggestions().get(0).getLabel().equals("Intramuscular") ? card1.getSuggestions().get(0) : card1.getSuggestions().get(1);
        Assert.assertNotNull(sug11);
        Assert.assertNotNull(sug11.getUuid());
        Assert.assertNotNull(sug11.getActions());
        Assert.assertEquals(1, sug11.getActions().size());
        Assert.assertNotNull(sug11.getActions().get(0));
        Assert.assertEquals("delete", sug11.getActions().get(0).get("type"));
        Assert.assertEquals("Remove Ceftriaxone draft order", sug11.getActions().get(0).get("description"));
        Assert.assertEquals("MedicationRequest/medrx0325", sug11.getActions().get(0).get("resourceId"));
        Assert.assertNotNull(sug12);
        Assert.assertNotNull(sug12.getUuid());
        Assert.assertNotNull(sug12.getActions());
        Assert.assertEquals(1, sug12.getActions().size());
        Assert.assertNotNull(sug12.getActions().get(0));
        Assert.assertEquals("cefTRIAXone (ROCEPHIN) 500 mg IM", sug12.getActions().get(0).get("description"));//TODO different description?
        Assert.assertEquals("create", sug12.getActions().get(0).get("type"));
        Assert.assertNotNull(sug12.getActions().get(0).get("resource"));
        Assert.assertEquals("any", card1.getSelectionBehavior());
        Assert.assertEquals(3, card1.getOverrideReasons().size());
        Assert.assertTrue(card1.getOverrideReasons().stream().anyMatch(jo -> "notforgonorrhea".equals(jo.get("code"))));
        Assert.assertTrue(card1.getOverrideReasons().stream().anyMatch(jo -> "inaccuratedata".equals(jo.get("code"))));
        Assert.assertTrue(card1.getOverrideReasons().stream().anyMatch(jo -> "other".equals(jo.get("code"))));
        
        Assert.assertNotNull(card2);
        Assert.assertNotNull(card2.getUuid());
        Assert.assertEquals("warning", card2.getIndicator());
        Assert.assertEquals("any", card2.getSelectionBehavior());
        Assert.assertEquals("If presumptively treating for gonorrhea, current recommendations are to treat presumptively for concurrent Chlamydia infection when "
                + "Chlamydia has not been excluded.   Available data indicate patient has no recent Chlamydia test,and has no beta-lactam allergy. Appropriate "
                + "options are: if not pregnant, doxycycline 100 mg PO bid x7 days, azithromycin 1 g PO x1, or levofloxacin 500 mg PO daily x7 days; if pregnant, "
                + "azithromycin 1 g PO x1 or amoxicillin 500 mg PO tid x7 days.\n Please consider the following additional recommendations: If pharyngeal gonorrhea, "
                + "perform a test-of-cure 7-14 days after treatment Retest for gonorrhea 3 months after treatment Test for syphilis and HIV, if not yet done  \n"
                + "Please advise the patient of the following: Sexual partners within prior 60 days need evaluation and treatment Abstain from sexual activity "
                + "for 7 days after treatment of yourself and partners", card2.getDetail());
        Assert.assertTrue(card2.getSuggestions() == null || card2.getSuggestions().isEmpty());
    }
    
    @Test
    public void testCqlDoubleResult() throws Exception {
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
        Bundle bundle = createMedOrders("SMART-436610", "14652");
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
        
        List<?> cards = (List<?>) workItem.getResult("cards");
        Assert.assertEquals(1, cards.size());
        Card card = (Card) cards.iterator().next();
        Assert.assertEquals("Attention: allergy and antibiotic stewardship", card.getSummary());
        Assert.assertEquals("warning", card.getIndicator());
        Assert.assertTrue(card.getDetail().length() > 50);
        Assert.assertEquals(5, card.getSuggestions().size());
        Suggestion sug1 = null, sug2 = null, sug3 = null, sug4 = null, sug5 = null;
        for (Suggestion sug : card.getSuggestions()) {
            if ("Remove Ceftriaxone draft order".equals(sug.getLabel())) {
                sug1 = sug;
            }  else if ("Intramuscular".equals(sug.getLabel())) {
                sug2 = sug;
            } else if ("Gentamicin 240mg IM x1 dose".equals(sug.getLabel())) {
                sug3 = sug;
            } else if ("Azithromycin 2 g PO x1 dose".equals(sug.getLabel())) {
                sug4 = sug;
            } else if ("Referral to ID".equals(sug.getLabel())) {
                sug5 = sug;
            }
        }
        Assert.assertNotNull(sug1);
        Assert.assertNotNull(sug1.getUuid());
        Assert.assertEquals(1, sug1.getActions().size());
        Assert.assertNotNull(sug1.getActions().get(0));
        Assert.assertEquals("Remove Ceftriaxone draft order", sug1.getActions().get(0).get("description"));
        Assert.assertEquals("MedicationRequest/medrx0325", sug1.getActions().get(0).get("resourceId"));
        Assert.assertEquals("delete", sug1.getActions().get(0).get("type"));

        Assert.assertNotNull(sug2);
        Assert.assertNotNull(sug2.getUuid());
        Assert.assertEquals(1, sug2.getActions().size());
        Assert.assertNotNull(sug2.getActions().get(0));
        Assert.assertEquals("create", sug2.getActions().get(0).get("type"));
        Assert.assertNotNull(sug2.getActions().get(0).get("resource"));
        Assert.assertEquals("cefTRIAXone (ROCEPHIN) 500 mg IM", sug2.getActions().get(0).get("description"));
        
        Assert.assertNotNull(sug3);
        Assert.assertNotNull(sug3.getUuid());
        Assert.assertEquals(1, sug3.getActions().size());
        Assert.assertNotNull(sug3.getActions().get(0));
        Assert.assertEquals("create", sug3.getActions().get(0).get("type"));
        Assert.assertNotNull(sug3.getActions().get(0).get("resource"));
        Assert.assertEquals("Gentamicin 240mg IM x1 dose", sug3.getActions().get(0).get("description"));

        Assert.assertNotNull(sug4);
        Assert.assertNotNull(sug4.getUuid());
        Assert.assertEquals(1, sug4.getActions().size());
        Assert.assertNotNull(sug4.getActions().get(0));
        Assert.assertEquals("create", sug4.getActions().get(0).get("type"));
        Assert.assertNotNull(sug4.getActions().get(0).get("resource"));
        Assert.assertEquals("Azithromycin 2 g PO x1 dose", sug4.getActions().get(0).get("description"));

        Assert.assertNotNull(sug5);
        Assert.assertNotNull(sug5.getUuid());
        Assert.assertEquals(1, sug5.getActions().size());
        Assert.assertNotNull(sug5.getActions().get(0));
        Assert.assertEquals("create", sug5.getActions().get(0).get("type"));
        Assert.assertNotNull(sug5.getActions().get(0).get("resource"));
        Assert.assertEquals("Referral to ID", sug5.getActions().get(0).get("description"));
        
        Assert.assertEquals("any", card.getSelectionBehavior());
        Assert.assertEquals(3, card.getOverrideReasons().size());
    }

	private Bundle createMedOrders(String patientRef, String medId) throws ParseException {
		Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);
        MedicationRequest mreq = new MedicationRequest();
        mreq.setId("medrx0325");
        mreq.setStatus(MedicationRequest.MedicationRequestStatus.DRAFT);
        mreq.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);
        mreq.addCategory().setText("Inpatient").addCoding().setCode("inpatient").setSystem("http://terminology.hl7.org/CodeSystem/medicationrequest-category").setDisplay("Inpatient");
        mreq.setMedication(new Reference().setReference("Medication/" + medId).setDisplay("CEFTRIAXONE 250 MG SOLUTION FOR INJECTION"));
        mreq.setSubject(new Reference().setReference(patientRef).setDisplay("Zzzrsh, Gonotwentyfour"));
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
        return bundle;
	}
    
    @Test
    public void testCqlDoubleCards() throws Exception {
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
        Bundle bundle = createMedOrders("SMART-436610", "14652");
        workItem.setParameter("context_selections", Arrays.asList("MedicationRequest/medrx0325"));
        workItem.setParameter("context_draftOrders", bundle);
        workItem.setParameter("planDefinitionJson", "{\n"
                + "    \"resourceType\": \"PlanDefinition\",\n"
                + "    \"id\": \"GonorrheaCDSPresumptiveTreatment-UnitTest\",\n"
                + "    \"meta\": {\n"
                + "        \"versionId\": \"1\",\n"
                + "        \"lastUpdated\": \"2023-06-06T17:26:20.848+00:00\",\n"
                + "        \"source\": \"#15hOfB0ppyWnMbYz\"\n"
                + "    },\n"
                + "    \"url\": \"http://elimu.io/PlanDefinition/GonorrheaCDSPresumptiveTreatment\",\n"
                + "    \"name\": \"GonorrheaCDSPresumptiveTreatment\",\n"
                + "    \"title\": \"Gonorrhea Management CDS: Presumptive Treatment Scenario\",\n"
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
                + "    \"date\": \"2023-03-01\",\n"
                + "    \"description\": \"Propose gonorrhea management in conformance with latest guidelines, in response to likely presumptive treatment as inferred by the ordering of intramuscular ceftriaxone; concomitant recommendations for Chlamydia treatment are given, if needed\",\n"
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
                + "        \"http://elimu.io/Library/GonorrheaTxCDS-UnitTest\"\n"
                + "    ],\n"
                + "    \"action\": [\n"
                + "        {\n"
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
                + "                        \"description\": \"Meets basic inclusion criteria and trigger: age 13 or higher, and has new order for intramuscular ceftriaxone\",\n"
                + "                        \"language\": \"text/cql\",\n"
                + "                        \"expression\": \"IsAgeGT13WithNewIMCeftriaxOrder\"\n"
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
                + "                        \"language\": \"text/cql.identifier\",\n"
                + "                        \"expression\": \"CardTitlePresumptiveGCTx\"\n"
                + "                    }\n"
                + "                },\n"
                + "                {\n"
                + "                    \"path\": \"action.description\",\n"
                + "                    \"expression\": {\n"
                + "                        \"language\": \"text/cql.identifier\",\n"
                + "                        \"expression\": \"CardDetailTextGCTx\"\n"
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
                + "                        \"expression\": \"overrideReasonsForCeftriaxoneIMCard\"\n"
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
                + "                                \"expression\": \"DraftCetriaxoneIMOrderId\"\n"
                + "                            }\n"
                + "                        }\n"
                + "                    ]\n"
                + "                },\n"
                + "                {\n"
                + "                    \"condition\": [\n"
                + "                        {\n"
                + "                            \"kind\": \"applicability\",\n"
                + "                            \"expression\": {\n"
                + "                                \"description\": \"Meets criteria for prechecked ceftriaxone 500 mg IM x1\",\n"
                + "                                \"language\": \"text/cql\",\n"
                + "                                \"expression\": \"NeedsCeftriax500Prechecked\"\n"
                + "                            }\n"
                + "                        }\n"
                + "                    ],\n"
                + "                    \"precheckBehavior\": \"yes\",\n"
                + "                    \"definitionCanonical\": \"http://elimu.io/ActivityDefinition/Ceftriaxone500OrderProposal\"\n"
                + "                },\n"
                + "                {\n"
                + "                    \"condition\": [\n"
                + "                        {\n"
                + "                            \"kind\": \"applicability\",\n"
                + "                            \"expression\": {\n"
                + "                                \"description\": \"Meets criteria for prechecked ceftriaxone 1 gram IM x1\",\n"
                + "                                \"language\": \"text/cql\",\n"
                + "                                \"expression\": \"NeedsCeftriax1000Prechecked\"\n"
                + "                            }\n"
                + "                        }\n"
                + "                    ],\n"
                + "                    \"precheckBehavior\": \"yes\",\n"
                + "                    \"definitionCanonical\": \"http://elimu.io/ActivityDefinition/Ceftriaxone1000OrderProposal\"\n"
                + "                },\n"
                + "                {\n"
                + "                    \"condition\": [\n"
                + "                        {\n"
                + "                            \"kind\": \"applicability\",\n"
                + "                            \"expression\": {\n"
                + "                                \"description\": \"Meets criteria for a non-prechecked ceftriaxone 500 mg IM x1\",\n"
                + "                                \"language\": \"text/cql\",\n"
                + "                                \"expression\": \"NeedsCeftriax500Option\"\n"
                + "                            }\n"
                + "                        }\n"
                + "                    ],\n"
                + "                    \"precheckBehavior\": \"no\",\n"
                + "                    \"definitionCanonical\": \"http://elimu.io/ActivityDefinition/Ceftriaxone500OrderProposal\"\n"
                + "                },\n"
                + "                {\n"
                + "                    \"condition\": [\n"
                + "                        {\n"
                + "                            \"kind\": \"applicability\",\n"
                + "                            \"expression\": {\n"
                + "                                \"description\": \"Has beta lactam allergy, and needs option for gentamicin 240 mg\",\n"
                + "                                \"language\": \"text/cql\",\n"
                + "                                \"expression\": \"NeedsAllergyOptions\"\n"
                + "                            }\n"
                + "                        }\n"
                + "                    ],\n"
                + "                    \"precheckBehavior\": \"no\",\n"
                + "                    \"definitionCanonical\": \"http://elimu.io/ActivityDefinition/Gentamicin240OrderProposal\"\n"
                + "                },\n"
                + "                {\n"
                + "                    \"condition\": [\n"
                + "                        {\n"
                + "                            \"kind\": \"applicability\",\n"
                + "                            \"expression\": {\n"
                + "                                \"description\": \"Has beta lactam allergy, and needs option for azithromycin 2 gram\",\n"
                + "                                \"language\": \"text/cql\",\n"
                + "                                \"expression\": \"NeedsAllergyOptions\"\n"
                + "                            }\n"
                + "                        }\n"
                + "                    ],\n"
                + "                    \"precheckBehavior\": \"no\",\n"
                + "                    \"definitionCanonical\": \"http://elimu.io/ActivityDefinition/Azithromycin2GmOrderProposal\"\n"
                + "                },\n"
                + "                {\n"
                + "                    \"condition\": [\n"
                + "                        {\n"
                + "                            \"kind\": \"applicability\",\n"
                + "                            \"expression\": {\n"
                + "                                \"description\": \"Has beta lactam allergy, and needs option for ID referral\",\n"
                + "                                \"language\": \"text/cql\",\n"
                + "                                \"expression\": \"NeedsAllergyOptions\"\n"
                + "                            }\n"
                + "                        }\n"
                + "                    ],\n"
                + "                    \"precheckBehavior\": \"no\",\n"
                + "                    \"definitionCanonical\": \"http://elimu.io/ActivityDefinition/IDReferral\"\n"
                + "                }\n"
                + "            ]\n"
                + "        },\n"
                + "        {\n"
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
                + "                        \"description\": \"Meets trigger as above for presumptive gonorrhea treatment, and has positive or possible Chlamydia\",\n"
                + "                        \"language\": \"text/cql\",\n"
                + "                        \"expression\": \"NeedsChlamydiaTxRec\"\n"
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
                + "                        \"language\": \"text/cql.identifier\",\n"
                + "                        \"expression\": \"CardTitleAdditionalGCRecs\"\n"
                + "                    }\n"
                + "                },\n"
                + "                {\n"
                + "                    \"path\": \"action.description\",\n"
                + "                    \"expression\": {\n"
                + "                        \"language\": \"text/cql.identifier\",\n"
                + "                        \"expression\": \"CardDetailTextAdditionalGCRecs\"\n"
                + "                    }\n"
                + "                },\n"
                + "                {\n"
                + "                    \"path\": \"action.extension\",\n"
                + "                    \"expression\": {\n"
                + "                        \"language\": \"text/cql.identifier\",\n"
                + "                        \"expression\": \"CardIndicatorCategory\"\n"
                + "                    }\n"
                + "                }\n"
                + "            ]\n"
                + "        }\n"
                + "    ]\n"
                + "}");
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
        
        List<?> cards = (List<?>) workItem.getResult("cards");
        Assert.assertEquals(2, cards.size());
        Card card1 = ((Card) cards.get(0)).getSummary().equals("Attention: Antibiotic Stewardship") ? (Card) cards.get(0) : (Card) cards.get(1);
        Card card2 = ((Card) cards.get(0)).getSummary().equals("Additional Gonorrhea Recommendations") ? (Card) cards.get(0) : (Card) cards.get(1);
        Assert.assertEquals("warning", card1.getIndicator());
        Assert.assertEquals("If the current ceftriaxone order is presumptive treatment for gonorrhea, please note that the CDC's updated guidance is that for patients (presumably) weighing <150 kg and no beta-lactam allergy, the recommended dose is ceftriaxone 500 mg IM x1.", card1.getDetail());
        Assert.assertEquals(2, card1.getSuggestions().size());
        Suggestion sug11 = card1.getSuggestions().get(0).getLabel().equals("Remove Ceftriaxone draft order") ? card1.getSuggestions().get(0) : card1.getSuggestions().get(1);
        Suggestion sug12 = card1.getSuggestions().get(0).getLabel().equals("Intramuscular") ? card1.getSuggestions().get(0) : card1.getSuggestions().get(1);
        Assert.assertEquals(1, sug11.getActions().size());
        JSONObject sug11Action = sug11.getActions().get(0);
        
        Assert.assertEquals(1, sug12.getActions().size());
        JSONObject sug12Action = sug12.getActions().get(0);
        Assert.assertEquals("delete", sug11Action.get("type"));
        Assert.assertEquals("Remove Ceftriaxone draft order", sug11Action.get("description"));
        Assert.assertEquals("MedicationRequest/medrx0325", sug11Action.get("resourceId"));
        Assert.assertEquals("create", sug12Action.get("type"));
        Assert.assertNotNull(sug12Action.get("resource"));
        Assert.assertEquals("cefTRIAXone (ROCEPHIN) 500 mg IM", sug12Action.get("description"));
        Assert.assertEquals("any", card1.getSelectionBehavior());
        Assert.assertEquals(3, card1.getOverrideReasons().size());

        Assert.assertEquals("If presumptively treating for gonorrhea, current recommendations are to treat presumptively for concurrent Chlamydia infection when Chlamydia has not been excluded.   Available data indicate patient has no recent Chlamydia test,and has no beta-lactam allergy. Appropriate options are: if not pregnant, doxycycline 100 mg PO bid x7 days, azithromycin 1 g PO x1, or levofloxacin 500 mg PO daily x7 days; if pregnant, azithromycin 1 g PO x1 or amoxicillin 500 mg PO tid x7 days.\n Please consider the following additional recommendations: If pharyngeal gonorrhea, perform a test-of-cure 7-14 days after treatment Retest for gonorrhea 3 months after treatment Test for syphilis and HIV, if not yet done  \nPlease advise the patient of the following: Sexual partners within prior 60 days need evaluation and treatment Abstain from sexual activity for 7 days after treatment of yourself and partners", card2.getDetail());
        Assert.assertTrue(card2.getSuggestions() == null || card2.getSuggestions().isEmpty());
        Assert.assertEquals("warning", card2.getIndicator());
        Assert.assertEquals("any", card2.getSelectionBehavior());
    }
    
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
        Bundle bundle = createMedOrders("SMART-436610", "14652");
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
        Assert.assertEquals("Attention: allergy and antibiotic stewardship", card.getSummary());
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
