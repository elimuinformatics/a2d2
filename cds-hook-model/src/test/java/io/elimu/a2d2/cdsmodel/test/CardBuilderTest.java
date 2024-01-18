package io.elimu.a2d2.cdsmodel.test;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;

import io.elimu.a2d2.cdsresponse.entity.Card;
import io.elimu.a2d2.cdsresponse.entity.CardBuilder;

public class CardBuilderTest {

	@Test
	public void testCardBuilderWithExtension() {
		List dataList = List.of(
				Map.of("x", "Usual Care", "y", 3.1),
				Map.of("x", "DPP Lifestyle", "y", 1.6),
				Map.of("x", "Metformin", "y", 1.2)
				);
		Map ext = Map.of("type", "bar", "yAxisTitle", "Risk of Diabetes", "yAxisUnit", "%", "data", dataList);
		Card card = CardBuilder.create().withIndicator("warning").withSummary("Test card").
				withExtension("io.elimu.sapphire.chart", ext).build();
	    
		String cardJson = new Gson().toJson(card);
		Assert.assertNotNull(cardJson);
		Card card2 = new Gson().fromJson(cardJson, Card.class);
		Assert.assertNotNull(card2);
		Assert.assertNotNull(card2.getSummary());
		Assert.assertNotNull(card2.getIndicator());
		Assert.assertNotNull(card2.getExtension());
		Assert.assertEquals(1, card2.getExtension().size());
		Assert.assertNotNull(card2.getExtension().get("io.elimu.sapphire.chart"));
		Assert.assertNotNull(card2.getExtension().get("io.elimu.sapphire.chart").get("type"));
		Assert.assertNotNull(card2.getExtension().get("io.elimu.sapphire.chart").get("yAxisTitle"));
		Assert.assertNotNull(card2.getExtension().get("io.elimu.sapphire.chart").get("yAxisUnit"));
		Assert.assertNotNull(card2.getExtension().get("io.elimu.sapphire.chart").get("data"));
	}
}
