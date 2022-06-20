package io.elimu.a2d2.cql;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import io.elimu.a2d2.cdsresponse.entity.Card;

public class CqlClientWIHTest {

	@Test
	public void testMeasureEvaluate() {
		String fhirServerUrl = System.getProperty("fhirServerUrl");
		if (fhirServerUrl == null) {
			return;
		}
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setParameter("fhirServerUrl", fhirServerUrl);
		workItem.setParameter("measureId", "ColorectalCancerScreeningCQM");
		workItem.setParameter("periodStart", "2022-01-01");
		workItem.setParameter("periodEnd", "2022-06-20");
		new CqlClientWorkItemHandler().executeWorkItem(workItem, new WorkItemManager() {
			@Override public void abortWorkItem(long id) { }
			@Override public void completeWorkItem(long id, Map<String, Object> results) { }
			@Override public void registerWorkItemHandler(String workItemName, WorkItemHandler handler) { }
		});
		Assert.assertEquals(workItem.getResult("group_0_population_denominator"), Integer.valueOf(4));
		Assert.assertEquals(workItem.getResult("group_0_measureScore"), BigDecimal.valueOf(0.5));
	}
	
	@Test
	public void testCdsHook() {
		String fhirServerUrl = System.getProperty("fhirServerUrl");
		if (fhirServerUrl == null) {
			return;
		}
		
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setParameter("fhirServerUrl", fhirServerUrl);
		workItem.setParameter("planDefinitionId", "ColorectalCancerScreeningCDS");
		workItem.setParameter("context_patientId", "should-screen-ccs");
		workItem.setParameter("context_userId", "Practitioner/COREPRACTITIONER1");
		new PlanDefCdsWorkItemHandler().executeWorkItem(workItem, new WorkItemManager() {
			@Override public void abortWorkItem(long id) { }
			@Override public void completeWorkItem(long id, Map<String, Object> results) { }
			@Override public void registerWorkItemHandler(String workItemName, WorkItemHandler handler) { }
		});
		Assert.assertNotNull(workItem.getResult("cards"));
		List<?> cards = (List<?>) workItem.getResult("cards");
		Assert.assertEquals(1, cards.size());
		Card card = (Card) cards.iterator().next();
		Assert.assertNotNull(card);
		Assert.assertEquals("Recommend appropriate colorectal cancer screening", card.getSummary());
		Assert.assertEquals("warning", card.getIndicator());
		Assert.assertNotNull(card.getSource());
	}
}
