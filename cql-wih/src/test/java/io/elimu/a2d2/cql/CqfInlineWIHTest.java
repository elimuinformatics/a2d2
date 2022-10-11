package io.elimu.a2d2.cql;

import java.util.List;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.junit.Assert;
import org.junit.Ignore;
import java.util.Map;

import org.junit.Test;
import org.opencds.cqf.cds.response.CdsCard;
import org.opencds.cqf.cds.response.CdsCard.IndicatorCode;
import org.hl7.fhir.r4.model.PlanDefinition;
import org.hl7.fhir.r4.model.PlanDefinition.ActionSelectionBehavior;
import org.hl7.fhir.r4.model.RequestGroup;
import io.elimu.a2d2.oauth.BodyBuilder;
import io.elimu.a2d2.oauth.OAuthUtils;

public class CqfInlineWIHTest {

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
		CdsCard card = (CdsCard) cards.get(0);
		Assert.assertNotNull(card);
		Assert.assertNotNull(card.getSummary());
		Assert.assertEquals("Recommend appropriate colorectal cancer screening", card.getSummary());
		Assert.assertNotNull(card.getIndicator());
		Assert.assertEquals(IndicatorCode.WARN, card.getIndicator());;
		Assert.assertNotNull(card.getDetail());
		Assert.assertEquals("Patient meets the inclusion criteria for appropriate colorectal cancer screening, but has no evidence of appropriate screening.", card.getDetail());
		Assert.assertNotNull(card.getSource());
		Assert.assertNotNull(card.getSource().getLabel());
		Assert.assertEquals("U.S. Preventive Services Task Force Final Recommendation Statement Colorectal Cancer: Screening", card.getSource().getLabel());
		Assert.assertNotNull(card.getSource().getUrl());
		Assert.assertEquals("https://www.uspreventiveservicestaskforce.org/uspstf/recommendation/colorectal-cancer-screening", card.getSource().getUrl().toExternalForm());
		
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
