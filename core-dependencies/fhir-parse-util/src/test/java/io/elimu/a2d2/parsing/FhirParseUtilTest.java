package io.elimu.a2d2.parsing;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.Assert;
import org.junit.Test;

import io.elimu.a2d2.parsing.FhirParseUtil.FormatType;

public class FhirParseUtilTest {

	@Test
	public void testJsonResourceParsing() {
		Bundle bundle = new Bundle();
		bundle.setId("1234");
		String json = FhirParseUtil.getInstance().encodeJsonResource(FormatType.FHIR4, bundle);
		Assert.assertNotNull(json);
		System.out.println("FhirParseUtilTest JSON = " + json);
		
		/*ca.uhn.fhir.model.dstu2.resource.Bundle bundle2 = new ca.uhn.fhir.model.dstu2.resource.Bundle();
		bundle.setId("1234");
		json = FhirParseUtil.getInstance().encodeJsonResource(FormatType.FHIR2, bundle2);
		Assert.assertNotNull(json);
		System.out.println("FhirParseUtilTest JSON = " + json);*/
	}
	
	@Test
	public void testXmlResourceParsing() {
		Bundle bundle = new Bundle();
		bundle.setId("1234");
		String json = FhirParseUtil.getInstance().encodeXmlResource(FormatType.FHIR4, bundle);
		Assert.assertNotNull(json);
		System.out.println("FhirParseUtilTest XML = " + json);
	}
	
}
