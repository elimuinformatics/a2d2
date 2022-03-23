package io.elimu.a2d2.parsing;

import org.hl7.fhir.r4.model.Patient;
import org.junit.Assert;
import org.junit.Test;

import io.elimu.a2d2.parsing.FhirParseUtil.FormatType;

public class FhirParseUtilTest {

	@Test
	public void testJsonResourceParsing() {
		Patient patient = new Patient();
		patient.setId("1234");
		String json = FhirParseUtil.getInstance().encodeJsonResource(FormatType.FHIR4, patient);
		Assert.assertNotNull(json);
		System.out.println("FhirParseUtilTest JSON = " + json);
	}
	
	@Test
	public void testXmlResourceParsing() {
		Patient patient = new Patient();
		patient.setId("1234");
		String json = FhirParseUtil.getInstance().encodeXmlResource(FormatType.FHIR4, patient);
		Assert.assertNotNull(json);
		System.out.println("FhirParseUtilTest XML = " + json);
	}
	
}
