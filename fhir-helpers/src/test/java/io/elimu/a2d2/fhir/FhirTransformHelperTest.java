package io.elimu.a2d2.fhir;

import java.io.InputStream;

import org.hl7.fhir.r4.model.Resource;
import org.junit.Assert;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.BaseResource;

public class FhirTransformHelperTest {

	@Test
	public void testTransformProcedureV2toV4() throws Exception {
		InputStream input = getClass().getResourceAsStream("/procedure-1.json");
		BaseResource resv2 = (BaseResource) FhirContext.forDstu2().newJsonParser().parseResource(input);
		Resource resv4 = FhirTransformHelper.transformDstu2Resource(resv2);
		Assert.assertNotNull(resv4);

		InputStream input2 = getClass().getResourceAsStream("/procedure-2.json");
		resv2 = (BaseResource) FhirContext.forDstu2().newJsonParser().parseResource(input2);
		resv4 = FhirTransformHelper.transformDstu2Resource(resv2);
		Assert.assertNotNull(resv4);
}
	
	@Test
	public void testTransformAllergyIntoleranceV2toV4() throws Exception {
		InputStream input = getClass().getResourceAsStream("/allergy-intolerance.json");
		BaseResource resv2 = (BaseResource) FhirContext.forDstu2().newJsonParser().parseResource(input);
		Resource resv4 = FhirTransformHelper.transformDstu2Resource(resv2);
		Assert.assertNotNull(resv4);
	}

	@Test
	public void testTransformMedicationOrderV2toV4() {
		InputStream input = getClass().getResourceAsStream("/medication-order.json");
		BaseResource resv2 = (BaseResource) FhirContext.forDstu2().newJsonParser().parseResource(input);
		Resource resv4 = FhirTransformHelper.transformDstu2Resource(resv2);
		Assert.assertNotNull(resv4);
	}

	@Test
	public void testTransformConditionV2toV4() {
		InputStream input = getClass().getResourceAsStream("/condition.json");
		BaseResource resv2 = (BaseResource) FhirContext.forDstu2().newJsonParser().parseResource(input);
		Resource resv4 = FhirTransformHelper.transformDstu2Resource(resv2);
		Assert.assertNotNull(resv4);

	}

	@Test
	public void testTransformPatientV2toV4() {
		InputStream input = getClass().getResourceAsStream("/patient.json");
		BaseResource resv2 = (BaseResource) FhirContext.forDstu2().newJsonParser().parseResource(input);
		Resource resv4 = FhirTransformHelper.transformDstu2Resource(resv2);
		Assert.assertNotNull(resv4);
	}

	@Test
	public void testTransformQuestionnaireResponseV3toV4() {
		InputStream input = getClass().getResourceAsStream("/questionnaire-response.json");
		BaseResource resv2 = (BaseResource) FhirContext.forDstu2().newJsonParser().parseResource(input);
		Resource resv4 = FhirTransformHelper.transformDstu2Resource(resv2);
		Assert.assertNotNull(resv4);

	}

}
