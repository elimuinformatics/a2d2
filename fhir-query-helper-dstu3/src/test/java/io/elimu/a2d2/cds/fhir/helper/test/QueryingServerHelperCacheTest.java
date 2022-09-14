// Copyright 2018-2020 Elimu Informatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.elimu.a2d2.cds.fhir.helper.test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import io.elimu.a2d2.cds.fhir.cache.CacheUtil;
import io.elimu.a2d2.cds.fhir.helper.FhirResponse;
import io.elimu.a2d2.cds.fhir.helper.dstu3.QueryingServerHelper;

@SuppressWarnings("static-access")
public class QueryingServerHelperCacheTest {

	public static QueryingServerHelper queryingServerHelper = spy(QueryingServerHelper.class);

	//mock data - user 1
	private static final String user_1 = "user_22";
	private static final String pass_1 = "pass_23";
	private static final String fhirUrl_1 = "https://fhirUrl_1";
	private static final String resourceQuery_1 = "https://fhirUrl_1?esourceQuery_mock_1";
	private static final String responseStatusInfo_1 = "responseStatusInfo - response first user_1";

	private static final String resourceType_1 = "MedicationAdministration";
	private static final String subjectId_1 =  "4342012";
	private static final String subjectRefAttribute_1 = "subject:Patient";
	private static final String fhirQuery_1 = null;
	FhirResponse<List<IBaseResource>> retval_1 = null;

	//mock data - user 2
	private static final String user_2 = "user2";
	private static final String pass_2 = "pass2";
	private static final String fhirUrl_2 = "https://fhirUrl_2";
	private static final String resourceQuery_2 = "https://fhirUrl_1?esourceQuery_mock_2";
	private static final String responseStatusInfo_2 = "responseStatusInfo - response first user_2";

	private static final String resourceType_2 = "Observation";
	private static final String subjectId_2 =  "2";
	private static final String subjectRefAttribute_2 = "subject:Patient";
	private static final String fhirQuery_2 = null;
	FhirResponse<List<IBaseResource>> retval_2 = null;

	//mock data - 401 error
	private static final String fhirUrl_3 = "https://fhirUrl_3";
	private static final String resourceQuery_3 = "https://fhirUrl_1?esourceQuery_mock_3";
	private static final String responseStatusInfo_3 = "responseStatusInfo - response first user_3";

	private static final String resourceType_3 = "Observation_3";
	private static final String subjectId_3 =  "3";
	private static final String subjectRefAttribute_3 = "subject:Patient_3";
	private static final String fhirQuery_3 = null;
	FhirResponse<List<IBaseResource>> retval_3 = null;

	@Before
	public void setUp() throws Exception {
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
		retval_3 = new FhirResponse<>(iBaseList, 401, responseStatusInfo_3);


		doReturn(resourceQuery_1).when(queryingServerHelper).getResourceQuery(resourceType_1, subjectId_1, subjectRefAttribute_1, fhirQuery_1);
		doReturn(retval_1).when(queryingServerHelper).queryServer(resourceQuery_1, true);

		doReturn(resourceQuery_2).when(queryingServerHelper).getResourceQuery(resourceType_2, subjectId_2, subjectRefAttribute_2, fhirQuery_2);
		doReturn(retval_2).when(queryingServerHelper).queryServer(resourceQuery_2, true);

		doReturn(resourceQuery_3).when(queryingServerHelper).getResourceQuery(resourceType_3, subjectId_3, subjectRefAttribute_3, fhirQuery_3);
		doReturn(retval_3).when(queryingServerHelper).queryServer(resourceQuery_3, true);
	}

	@Test
	public void testCacheSameUserDifferentUrl() throws UnsupportedEncodingException {
		clearCache();
		//USER1 (user, pass)
		queryingServerHelper.setFhirUrl(fhirUrl_1);
		queryingServerHelper.addAuthentication(CacheUtil.BASIC, user_1, pass_1);
		FhirResponse<List<IBaseResource>> retval = queryingServerHelper.queryResourcesResponse(resourceType_1, subjectId_1, subjectRefAttribute_1, null);
		retValAssert(retval, 200, responseStatusInfo_1);
		assertLength(1l);

		//USER1: doing a new request with the same user and different url
		clearingQueryServerHelper(user_1, pass_1, fhirUrl_2);
		FhirResponse<List<IBaseResource>> retval2 = queryingServerHelper.queryResourcesResponse(resourceType_2, subjectId_2, subjectRefAttribute_2, fhirQuery_2);
		retValAssert(retval2, 200, responseStatusInfo_2);
		assertLength(2l);

		Assert.assertNotEquals(retval, retval2);
	}

	@Test
	public void testCacheSameUrlDifferentUsers() {
		clearCache();
		//user1 url1
		queryingServerHelper.setFhirUrl(fhirUrl_1);
		queryingServerHelper.addAuthentication(CacheUtil.BASIC, user_1, pass_1);
		FhirResponse<List<IBaseResource>> retval = queryingServerHelper.queryResourcesResponse(resourceType_1, subjectId_1, subjectRefAttribute_1, null);
		retValAssert(retval, 200, responseStatusInfo_1);
		assertLength(1l);


		//user1 url1
		clearingQueryServerHelper(user_1, pass_1, fhirUrl_1);
		FhirResponse<List<IBaseResource>> retval1 = queryingServerHelper.queryResourcesResponse(resourceType_1, subjectId_1, subjectRefAttribute_1, fhirQuery_1);
		retValAssert(retval1, 200, responseStatusInfo_1);
		assertLength(1l);

		//user2 url2
		clearingQueryServerHelper(user_2, pass_2, fhirUrl_2);
		FhirResponse<List<IBaseResource>> retval2 = queryingServerHelper.queryResourcesResponse(resourceType_2, subjectId_2, subjectRefAttribute_2, fhirQuery_2);
		retValAssert(retval2, 200, responseStatusInfo_2);
		assertLength(2l);

		//user2 url2
		clearingQueryServerHelper(user_2, pass_2, fhirUrl_2);
		FhirResponse<List<IBaseResource>> retval3 = queryingServerHelper.queryResourcesResponse(resourceType_2, subjectId_2, subjectRefAttribute_2, fhirQuery_2);
		retValAssert(retval3, 200, responseStatusInfo_2);
		assertLength(2l);
	}

	@Test
	public void testCache401Error() throws UnsupportedEncodingException {
		clearCache();
		//USER1 (user, pass)
		queryingServerHelper.setFhirUrl(fhirUrl_1);
		queryingServerHelper.addAuthentication(CacheUtil.BASIC, user_1, pass_1);
		FhirResponse<List<IBaseResource>> retval = queryingServerHelper.queryResourcesResponse(resourceType_1, subjectId_1, subjectRefAttribute_1, null);
		retValAssert(retval, 200, responseStatusInfo_1);
		assertLength(1l);


		clearingQueryServerHelper(user_1, pass_1, fhirUrl_3);
		FhirResponse<List<IBaseResource>> retval2 = queryingServerHelper.queryResourcesResponse(resourceType_3, subjectId_3, subjectRefAttribute_3, fhirQuery_3);
		retValAssert(retval2, 401, responseStatusInfo_3);
		assertLength(1l);

	}



	private void clearingQueryServerHelper(String user, String pass, String fhirUrl) {
		queryingServerHelper.resetInterceptors();
		queryingServerHelper.addAuthentication(CacheUtil.BASIC, user, pass);
		queryingServerHelper.setFhirUrl(fhirUrl);
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
		if (queryingServerHelper.getCacheService() != null) {
			Assert.assertEquals(expectedLength, queryingServerHelper.getCacheService().length());
		}
	}

	//cacheService can be null when the cache was not configured
	private void clearCache() {
		if (queryingServerHelper.getCacheService() != null) {
			queryingServerHelper.getCacheService().deleteAll();
		}
	}

}
