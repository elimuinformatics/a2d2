package io.elimu.a2d2.cds.fhir.helper.test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.ArgumentMatchers.eq;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import io.elimu.a2d2.cds.fhir.cache.CacheUtil;
import io.elimu.a2d2.cds.fhir.helper.FhirResponse;
import io.elimu.a2d2.cds.fhir.helper.QueryBuilder;
import io.elimu.a2d2.cds.fhir.helper.r4.QueryingServerHelper;

public class FhirTest {

	public static QueryingServerHelper queryingServerHelper = spy(QueryingServerHelper.class);
	
	private static final String user_1 = "user_22";
	private static final String pass_1 = "pass_23";
	private static final String fhirUrl_1 = "https://fhirUrl_1";
	private static final String resourceQuery_1 = "https://fhirUrl_1/MedicationAdministration?_count=1&_sort=-_lastUpdated";
	private static final String responseStatusInfo_1 = "responseStatusInfo - response first user_1";
	FhirResponse<List<IBaseResource>> retval_1 = null;

	private static final String user_2 = "user2";
	private static final String pass_2 = "pass2";
	private static final String fhirUrl_2 = "https://fhirUrl_2";
	private static final String resourceQuery_2 = "https://fhirUrl_2/Observation?_count=2&_sort=-date";
	private static final String responseStatusInfo_2 = "responseStatusInfo - response first user_2";
	FhirResponse<List<IBaseResource>> retval_2 = null;

	@Before
	public void setup() {
		List<IBaseResource> iBaseList = new ArrayList<IBaseResource>();
		StructureDefinition iBase = new StructureDefinition();
		//iBase.setId(new IdDt("testId"));
		iBase.setUrl("test_url");
		Meta meta = new Meta();
		InstantType instantType = new InstantType(Calendar.getInstance());
		meta.setLastUpdatedElement(instantType);
		iBase.setMeta(meta);
		iBaseList.add(iBase);

		retval_1 = new FhirResponse<>(iBaseList, 200, responseStatusInfo_1);
		retval_2 = new FhirResponse<>(iBaseList, 200, responseStatusInfo_2);

		doReturn(retval_1).when(queryingServerHelper).queryServer(eq(resourceQuery_1), eq(new QueryBuilder().
				resourceType("MedicationAdministration").
				count(1).withParam("_sort", "-_lastUpdated").paging(false)));
		doReturn(retval_2).when(queryingServerHelper).queryServer(resourceQuery_2, new QueryBuilder().paging(true));
		doReturn(retval_2).when(queryingServerHelper).queryServer(eq(resourceQuery_2), eq(new QueryBuilder().
				resourceType("Observation").count(2).withParam("_sort", "-date")));
	}
	
	@Test
	public void testQueryWithQueryHelper() {
		clearCache();
		//user1 url1
		queryingServerHelper.setFhirUrl(fhirUrl_1);
		queryingServerHelper.addAuthentication(CacheUtil.BASIC, user_1, pass_1);
		FhirResponse<List<IBaseResource>> retval = queryingServerHelper.query(
				new QueryBuilder().resourceType("MedicationAdministration").
				count(1).withParam("_sort", "-_lastUpdated").paging(false));
		retValAssert(retval, 200, responseStatusInfo_1);
		assertLength(1l);

		//user1 url1
		clearCache();
		queryingServerHelper.setFhirUrl(fhirUrl_2);
		queryingServerHelper.addAuthentication(CacheUtil.BASIC, user_2, pass_2);
		FhirResponse<List<IBaseResource>> retval1 = queryingServerHelper.query(
				new QueryBuilder().resourceType("Observation").
				count(2).withParam("_sort", "-date"));
		retValAssert(retval1, 200, responseStatusInfo_2);
		assertLength(1l);

	}
	
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
	
	private void clearCache() {
		if (QueryingServerHelper.getCacheService() != null) {
			QueryingServerHelper.getCacheService().deleteAll();
		}
	}

	private void retValAssert(FhirResponse<List<IBaseResource>> retval, int expectedStatus, String expectedResponseStatusInfo) {
		Assert.assertNotNull(retval);
		Assert.assertNotNull(retval.getResult());
		List<IBaseResource> iBaseList = retval.getResult();
		Assert.assertTrue(iBaseList.get(0) instanceof StructureDefinition);
		StructureDefinition s = (StructureDefinition) iBaseList.get(0);
		Assert.assertEquals("test_url", s.getUrl());
		Assert.assertEquals(expectedStatus, retval.getResponseStatusCode());
		Assert.assertEquals(expectedResponseStatusInfo, retval.getResponseStatusInfo());
	}

	//cacheService can be null when the cache was not configured
	private void assertLength(long expectedLength) {
		if (QueryingServerHelper.getCacheService() != null) {
			Assert.assertEquals(expectedLength, QueryingServerHelper.getCacheService().length());
		}
	}
}
