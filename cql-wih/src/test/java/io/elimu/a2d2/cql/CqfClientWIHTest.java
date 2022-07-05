package io.elimu.a2d2.cql;

import java.util.List;
import java.util.Map;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.junit.Assert;
import org.junit.Test;

import io.elimu.a2d2.cdsresponse.entity.Card;

public class CqfClientWIHTest {

	@Test
	public void testClientCall() throws Exception {
		PlanDefCdsWorkItemHandler handler = new PlanDefCdsWorkItemHandler();
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setParameter("fhirServerUrl", "https://cqf-internal.elimuinformatics.com/fhir");
		workItem.setParameter("planDefinitionId", "ColorectalCancerScreeningCDS");
		workItem.setParameter("patientId", "should-screen-ccs");
		workItem.setParameter("context_patientId", "should-screen-ccs");
		NoOpWorkItemManager manager = new NoOpWorkItemManager();
		handler.executeWorkItem(workItem, manager);
		Assert.assertEquals(true, manager.isCompleted());
		Assert.assertNotNull(workItem.getResults());
		Assert.assertNotNull(workItem.getResult("cards"));
		List<?> cards = (List<?>) workItem.getResult("cards");
		Assert.assertNotNull(cards);
		Assert.assertTrue(cards.size() > 0 );
		Assert.assertNotNull(cards.get(0));
		Assert.assertTrue(cards.get(0) instanceof Card);
		Card card = (Card) cards.get(0);
		Assert.assertNotNull(card);
		Assert.assertNotNull(card.getDetail());
		Assert.assertNotNull(card.getIndicator());
		Assert.assertNotNull(card.getSource());
		Assert.assertTrue(card.getSource() instanceof Map);
		Map<?,?> sourceMap = (Map<?,?>) card.getSource();
		Assert.assertNotNull(sourceMap.get("label"));
		Assert.assertNotNull(sourceMap.get("url"));
		Assert.assertNotNull(card.getSummary());
	}
}
