package io.elimu.a2d2.cds.fhir.helper.test;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import io.elimu.a2d2.cds.fhir.helper.FhirResponse;
import io.elimu.a2d2.cds.fhir.helper.r4.QueryingServerHelper;

public class FhirTest {

	@Test
	public void testFhirGetByPath() {
		Assume.assumeTrue(System.getProperty("fhirapikey") != null && System.getProperty("fhirurl") != null && System.getProperty("fhirpatientid") != null);
		String url = System.getProperty("fhirurl");
		String apikey = System.getProperty("fhirapikey");
		String patientId = System.getProperty("fhirpatientid");
		QueryingServerHelper helper = new QueryingServerHelper(url);
		helper.addHeader("x-api-key", apikey);
		FhirResponse<IBaseResource> resp1 = helper.getResourceByIdInPathResponse("Patient", patientId);
		Assert.assertNotNull(resp1);
		Assert.assertNotNull(resp1.getResult());
		Assert.assertEquals(200, resp1.getResponseStatusCode());
		
		FhirResponse<IBaseResource> resp2 = helper.getResourceByIdInPathResponse("Patient", patientId + System.currentTimeMillis());
		Assert.assertNotNull(resp2);
		Assert.assertNull(resp2.getResult());
		Assert.assertEquals(404, resp2.getResponseStatusCode());
	}
}
