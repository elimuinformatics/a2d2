package io.elimu.a2d2.fhir_resource_wih;

import java.util.HashMap;
import java.util.Map;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import io.elimu.a2d2.exception.WorkItemHandlerException;
import io.elimu.a2d2.fhirresourcewih.OperationOutcomeWorkItemHandler;

public class OpOutcomeWIHTest {

	@Test
	public void testEmptyMap() {
		Map<String, Object> params = new HashMap<>();
		Map<String, Object> a = OperationOutcomeWorkItemHandler.extractIssueDetails(params, "a");
		Map<String, Object> a1 = OperationOutcomeWorkItemHandler.extractIssueDetails(params, "a1");
		Assert.assertNotNull(a);
		Assert.assertTrue(a.isEmpty());
		Assert.assertNotNull(a1);
		Assert.assertTrue(a1.isEmpty());
	}

	@Test
	public void testExtractionFromMap() {
		Map<String, Object> params = new HashMap<>();
		params.put("issue_a_code", "1234");
		params.put("issue_a_diagnostics", "something");
		params.put("issue_a1_details", "abcde");
		params.put("issue_a1_detailsCode", "abcde");
		params.put("issue_a1_detailsSystem", "abcde");
		params.put("issue_a1_location0", "somewhere");
		params.put("issue_a1_location1", "overtherainbow");
		Map<String, Object> a = OperationOutcomeWorkItemHandler.extractIssueDetails(params, "a");
		Map<String, Object> a1 = OperationOutcomeWorkItemHandler.extractIssueDetails(params, "a1");
		Assert.assertNotNull(a);
		Assert.assertTrue(a.containsKey("code"));
		Assert.assertTrue(a.containsKey("diagnostics"));
		Assert.assertNotNull(a1);
		Assert.assertTrue(a1.containsKey("details"));
		Assert.assertTrue(a1.containsKey("location0"));
		Assert.assertTrue(a1.containsKey("location1"));
	}
	
	@Test
	public void testGenerateOutcome() {
		WorkItemImpl workItem = new WorkItemImpl();
		Map<String, Object> params = workItem.getParameters();
		params.put("issue_a_code", "informational");
		params.put("issue_a_diagnostics", "something");
		params.put("issue_a_severity", "warning");
		params.put("issue_a1_details", "abcde");
		params.put("issue_a1_detailsCode", "abcde");
		params.put("issue_a1_detailsSystem", "abcde");
		params.put("issue_a_location", "somewhere");;
		params.put("issue_a1_location0", "somewhere");
		params.put("issue_a1_location1", "overtherainbow");
		OperationOutcomeWorkItemHandler handler = new OperationOutcomeWorkItemHandler();
		WorkItemManager manager = new DoNothingWorkItemManager();
		handler.executeWorkItem(workItem, manager);
		Map<String, Object> results = workItem.getResults();
		Assert.assertNotNull(results);
		Assert.assertNotNull(results.get("outcome"));
		Assert.assertNotNull(results.get("outcomeJson"));
		Assert.assertTrue(results.get("outcomeJson") instanceof String);
		Assert.assertTrue(results.get("outcome") instanceof OperationOutcome);
		OperationOutcome oo = (OperationOutcome) results.get("outcome");
		Assert.assertNotNull(oo.getIssue());
		Assert.assertEquals(2, oo.getIssue().size());
		OperationOutcomeIssueComponent issue1 = (OperationOutcomeIssueComponent) oo.getIssue().get(0);
		OperationOutcomeIssueComponent issue2 = (OperationOutcomeIssueComponent) oo.getIssue().get(1);
		Assert.assertNotNull(issue1);
		Assert.assertNotNull(issue2);
	}

	@Test
	public void testGenerateOutcomeFHIR4() {
		WorkItemImpl workItem = new WorkItemImpl();
		Map<String, Object> params = workItem.getParameters();
		params.put("fhirVersion", "FHIR4");
		params.put("issue_a_code", "informational");
		OperationOutcomeWorkItemHandler handler = new OperationOutcomeWorkItemHandler();
		WorkItemManager manager = new DoNothingWorkItemManager();
		handler.executeWorkItem(workItem, manager);
		Map<String, Object> results = workItem.getResults();
		Assert.assertNotNull(results);
		Assert.assertNotNull(results.get("outcome"));
		Assert.assertNotNull(results.get("outcomeJson"));
	}

	@Test
	public void testGenerateOutcomeR4() {
		WorkItemImpl workItem = new WorkItemImpl();
		Map<String, Object> params = workItem.getParameters();
		params.put("fhirVersion", "R4");
		params.put("issue_a_code", "informational");
		OperationOutcomeWorkItemHandler handler = new OperationOutcomeWorkItemHandler();
		WorkItemManager manager = new DoNothingWorkItemManager();
		handler.executeWorkItem(workItem, manager);
		Map<String, Object> results = workItem.getResults();
		Assert.assertNotNull(results);
		Assert.assertNotNull(results.get("outcome"));
		Assert.assertNotNull(results.get("outcomeJson"));
	}

	@Test
	public void testGenerateOutcomeDSTU3() {
		WorkItemImpl workItem = new WorkItemImpl();
		Map<String, Object> params = workItem.getParameters();
		params.put("fhirVersion", "DSTU3");
		params.put("issue_a_code", "informational");
		OperationOutcomeWorkItemHandler handler = new OperationOutcomeWorkItemHandler();
		WorkItemManager manager = new DoNothingWorkItemManager();
		handler.executeWorkItem(workItem, manager);
		Map<String, Object> results = workItem.getResults();
		Assert.assertNotNull(results);
		Assert.assertNotNull(results.get("outcome"));
		Assert.assertNotNull(results.get("outcomeJson"));
	}
	
	@Test
	public void testGenerateOutcomeV3() {
		WorkItemImpl workItem = new WorkItemImpl();
		Map<String, Object> params = workItem.getParameters();
		params.put("fhirVersion", "Fhir3");
		params.put("issue_a_code", "informational");
		params.put("issue_a_diagnostics", "something");
		params.put("issue_a_location", "somewhere");;
		params.put("issue_a_severity", "warning");
		params.put("issue_a1_details", "abcde");
		params.put("issue_a1_detailsCode", "abcde");
		params.put("issue_a1_detailsSystem", "abcde");
		params.put("issue_a1_location0", "somewhere");
		params.put("issue_a1_location1", "overtherainbow");
		OperationOutcomeWorkItemHandler handler = new OperationOutcomeWorkItemHandler();
		WorkItemManager manager = new DoNothingWorkItemManager();
		handler.executeWorkItem(workItem, manager);
		Map<String, Object> results = workItem.getResults();
		Assert.assertNotNull(results);
		Assert.assertNotNull(results.get("outcome"));
		Assert.assertNotNull(results.get("outcomeJson"));
		Assert.assertTrue(results.get("outcomeJson") instanceof String);
		Assert.assertTrue(results.get("outcome") instanceof org.hl7.fhir.dstu3.model.OperationOutcome);
		org.hl7.fhir.dstu3.model.OperationOutcome oo = (org.hl7.fhir.dstu3.model.OperationOutcome) results.get("outcome");
		Assert.assertNotNull(oo.getIssue());
		Assert.assertEquals(2, oo.getIssue().size());
		org.hl7.fhir.dstu3.model.OperationOutcome.OperationOutcomeIssueComponent issue1 = 
				(org.hl7.fhir.dstu3.model.OperationOutcome.OperationOutcomeIssueComponent) oo.getIssue().get(0);
		org.hl7.fhir.dstu3.model.OperationOutcome.OperationOutcomeIssueComponent issue2 = 
				(org.hl7.fhir.dstu3.model.OperationOutcome.OperationOutcomeIssueComponent) oo.getIssue().get(1);
		Assert.assertNotNull(issue1);
		Assert.assertNotNull(issue2);
	}

	@Test(expected = WorkItemHandlerException.class)
	public void testGenerateOutcomeV9() {
		WorkItemImpl workItem = new WorkItemImpl();
		Map<String, Object> params = workItem.getParameters();
		params.put("fhirVersion", "Fhir9000");
		OperationOutcomeWorkItemHandler handler = new OperationOutcomeWorkItemHandler();
		WorkItemManager manager = new DoNothingWorkItemManager();
		handler.executeWorkItem(workItem, manager);
	}
	
	@Test
	public void testGenerateOutcomeV2() {
		WorkItemImpl workItem = new WorkItemImpl();
		Map<String, Object> params = workItem.getParameters();
		params.put("fhirVersion", "Fhir2");
		params.put("issue_a_code", "informational");
		params.put("issue_a_diagnostics", "something");
		params.put("issue_a_severity", "warning");
		params.put("issue_a_location", "somewhere");;
		params.put("issue_a1_details", "abcde");
		params.put("issue_a1_detailsCode", "abcde");
		params.put("issue_a1_detailsSystem", "abcde");
		params.put("issue_a1_location0", "somewhere");
		params.put("issue_a1_location1", "overtherainbow");
		OperationOutcomeWorkItemHandler handler = new OperationOutcomeWorkItemHandler();
		WorkItemManager manager = new DoNothingWorkItemManager();
		handler.executeWorkItem(workItem, manager);
		Map<String, Object> results = workItem.getResults();
		Assert.assertNotNull(results);
		Assert.assertNotNull(results.get("outcome"));
		Assert.assertNotNull(results.get("outcomeJson"));
		Assert.assertTrue(results.get("outcomeJson") instanceof String);
		Assert.assertTrue(results.get("outcome") instanceof ca.uhn.fhir.model.dstu2.resource.OperationOutcome);
		ca.uhn.fhir.model.dstu2.resource.OperationOutcome oo = (ca.uhn.fhir.model.dstu2.resource.OperationOutcome) results.get("outcome");
		Assert.assertNotNull(oo.getIssue());
		Assert.assertEquals(2, oo.getIssue().size());
		ca.uhn.fhir.model.dstu2.resource.OperationOutcome.Issue issue1 =  (ca.uhn.fhir.model.dstu2.resource.OperationOutcome.Issue) oo.getIssue().get(0);
		ca.uhn.fhir.model.dstu2.resource.OperationOutcome.Issue issue2 = (ca.uhn.fhir.model.dstu2.resource.OperationOutcome.Issue) oo.getIssue().get(1);
		Assert.assertNotNull(issue1);
		Assert.assertNotNull(issue2);
	}

	@Test
	public void testGenerateOutcomeDSTU2() {
		WorkItemImpl workItem = new WorkItemImpl();
		Map<String, Object> params = workItem.getParameters();
		params.put("fhirVersion", "DSTU2");
		params.put("issue_a_code", "informational");
		OperationOutcomeWorkItemHandler handler = new OperationOutcomeWorkItemHandler();
		WorkItemManager manager = new DoNothingWorkItemManager();
		handler.executeWorkItem(workItem, manager);
		Map<String, Object> results = workItem.getResults();
		Assert.assertNotNull(results);
		Assert.assertNotNull(results.get("outcome"));
		Assert.assertNotNull(results.get("outcomeJson"));
	}
	
	public static class DoNothingWorkItemManager implements WorkItemManager {
		@Override public void completeWorkItem(long id, Map<String, Object> results) { }
		@Override public void abortWorkItem(long id) { }
		@Override public void registerWorkItemHandler(String workItemName, WorkItemHandler handler) { }
	}
}
