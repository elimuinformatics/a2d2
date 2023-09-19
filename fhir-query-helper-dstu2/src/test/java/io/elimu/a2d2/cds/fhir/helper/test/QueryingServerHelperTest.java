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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import io.elimu.a2d2.cds.fhir.helper.FhirFuture;
import io.elimu.a2d2.cds.fhir.helper.FhirResponse;
import io.elimu.a2d2.cds.fhir.helper.QueryBuilder;
import io.elimu.a2d2.cds.fhir.helper.QueryingServerHelper;

//@Ignore("No valid public FHIR servers available to test this")
public class QueryingServerHelperTest {

	private static QueryingServerHelper queryingServerHelper = spy(QueryingServerHelper.class);

	private static final String FHIR2_URL = "http://test/baseDstu2";

	private static Patient patient;

	private static Observation observation;

	private static final String IDENTIFIER = "identifier=urn:oid:0.1.2.3.4.5.6.7|123456";

	private static final String SUBJECT = "subject:Patient";

	private static List<IBaseResource> iBaseList = new ArrayList<IBaseResource>();

	private static FhirResponse<List<IBaseResource>> fhirResponse = null;
	
	private static FhirResponse<IBaseBundle> fhirResponse2 = null;
	
	private static FhirResponse<IBaseBundle> fhirResponse3 = null;

	private static FhirResponse<IBaseResource> fhirResponseObservation = null;

	private static FhirResponse<IBaseResource> fhirResponsePatient = null;

	@BeforeClass
	public static void setup() {
		FhirContext.forDstu2();
		queryingServerHelper.setFhirUrl(FHIR2_URL);
		patient = returnFHIR2TestPatient();
		observation = returnFHIR2TestObservation();
		fhirResponsePatient = new FhirResponse<>(patient, 200, "responseInfo");
		fhirResponseObservation = new FhirResponse<>(observation, 200, "responseInfo");
		iBaseList.add(patient);
		iBaseList.add(observation);
		fhirResponse = new FhirResponse<>(iBaseList, 200, "responseInfo");
		Bundle bundle = new Bundle();
		bundle.addLink().setRelation("next").setUrl("http://fhir2/someOtherUrl");
		fhirResponse2 = new FhirResponse<>(bundle, 200, "responseInfo");
		fhirResponse3 = new FhirResponse<>(new Bundle(), 200, "responseInfo2");
	}

	@Test 
	public void testQSH() throws Exception {
		String url = "https://hapi.fhir.org/baseDstu2";
		String patientId = "131";
		/*GenericClient client = (GenericClient) FhirContext.forDstu2().newRestfulGenericClient(url);
		client.setDontValidateConformance(true);
		client.setEncoding(EncodingEnum.JSON);*/
		QueryingServerHelper qsh = new QueryingServerHelper(url);
		FhirResponse<?> resp = qsh.getResourceByIdInPathResponse("Patient", patientId);
		Assert.assertEquals(200, resp.getResponseStatusCode());
		System.out.println(resp.getResult());
		//client.registerInterceptor(new SimpleRequestHeaderInterceptor("Accept: */*"));
		//client.registerInterceptor(new SimpleRequestHeaderInterceptor("Accept-Encoding: *;q=0"));
		//client.registerInterceptor(new SimpleRequestHeaderInterceptor("Authorization: Basic dXNlcjpwYXNz"));
		//Patient p = client.fetchResourceFromUrl(Patient.class, url + "/Patient/" + patientId);
		//Assert.assertNotNull(p);
		/*CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpGet get = new HttpGet(url + "/Patient/" + patientId);
		get.setHeader("Accept", "*//*");
		get.setHeader("Accept-Encoding", "*;q=0");
		CloseableHttpResponse resp = client.execute(get);
		Assert.assertEquals(200, resp.getStatusLine().getStatusCode());*/
	}
	
	@Test
	public void testQueryBuilder() {
		doReturn(fhirResponse).when(queryingServerHelper).queryServer(eq(FHIR2_URL + "/Patient?_count=1&_sort=-_lastUpdated"), 
				eq(new QueryBuilder().resourceType("Patient").count(1).withParam("_sort", "-_lastUpdated").paging(false)));
		doReturn(fhirResponse).when(queryingServerHelper).queryServer(eq(FHIR2_URL + "/Observation?_count=2&_sort=-date"), 
				eq(new QueryBuilder().paging(true).resourceType("Observation").count(2).withParam("_sort", "-date")));
		FhirResponse<List<IBaseResource>> retval = queryingServerHelper.query(
					new QueryBuilder().resourceType("Patient").
					count(1).withParam("_sort", "-_lastUpdated").paging(false));
		Assert.assertNotNull(retval);
		Assert.assertEquals(200, retval.getResponseStatusCode());
		Assert.assertNotNull(retval.getResult());

		retval = queryingServerHelper.query(
				new QueryBuilder().resourceType("Observation").
				count(2).withParam("_sort", "-date"));
		
		Assert.assertNotNull(retval);
		Assert.assertEquals(200, retval.getResponseStatusCode());
		Assert.assertNotNull(retval.getResult());
	}
	
	@Test
	public void testQueryPage() {
		doReturn(fhirResponse2).when(queryingServerHelper).fetchServer(eq("Bundle"), eq(FHIR2_URL + "/Patient?_count=50"));
		doReturn(fhirResponse3).when(queryingServerHelper).fetchServer(eq("Bundle"), eq("http://fhir2/someOtherUrl"));
		FhirResponse<IBaseBundle> retval = queryingServerHelper.queryPageByLink(new QueryBuilder().resourceType("Patient"));
		Assert.assertNotNull(retval);
		Assert.assertEquals(200, retval.getResponseStatusCode());
		Bundle b = (Bundle) retval.getResult();
		Assert.assertNotNull(b);
		Assert.assertNotNull(b.getLink("next"));
		FhirResponse<IBaseBundle> retval2 = queryingServerHelper.queryPageByLink(b.getLink("next").getUrl());
		Assert.assertEquals(200, retval2.getResponseStatusCode());
		Assert.assertEquals("responseInfo2", retval2.getResponseStatusInfo());
		Bundle b2 = (Bundle) retval2.getResult();
		Assert.assertNotNull(b2);
		Assert.assertNull(b2.getLink("next"));
	}
	
	@Test
	public void fhirQueryAsync() {
		doReturn(fhirResponse).when(queryingServerHelper).queryServer(any(), any());
		doReturn(fhirResponseObservation).when(queryingServerHelper).getResourceByIdResponse(any(), any());
		long from = System.currentTimeMillis();
		FhirFuture<FhirResponse<List<IBaseResource>>> f = queryingServerHelper.queryResourcesAsync("Observation",
				patient.getId().getIdPart(), SUBJECT, IDENTIFIER, "test-1");
		long to = System.currentTimeMillis();
		Assert.assertTrue(to - from < 100); // less than a second should be what this takes to run (should be less, but
											// this is so any env can run this test successfuly)
		Assert.assertNotNull(f);
		from = System.currentTimeMillis();
		when(queryingServerHelper.getResourceByIdResponse(any(), any())).thenReturn(fhirResponseObservation);
		FhirFuture<FhirResponse<IBaseResource>> f2 = queryingServerHelper.getResourceByIdAsync("Observation",
				observation.getId().getIdPart(), "test-2");
		to = System.currentTimeMillis();
		Assert.assertTrue(to - from < 100); // less than a second should be what this takes to run (should be less, but
											// this is so any env can run this test successfuly)
		Assert.assertNotNull(f2);
	}

	@Test
	public void fhirQueryNoAuth() {
		doReturn(fhirResponse).when(queryingServerHelper).queryServer(any(), any());
		QueryingServerHelper qshNoAuth = new QueryingServerHelper("http://testURL/baseDstu2");
		Assert.assertNotNull(qshNoAuth);
		FhirResponse<List<IBaseResource>> iResourceList = queryingServerHelper.queryResourcesResponse("Observation",
				patient.getId().getIdPart(), SUBJECT, IDENTIFIER);
		Assert.assertNotNull(iResourceList);
	}

	@Test
	public void fhirQueryAndQueryPartTest() {
		doReturn(fhirResponse).when(queryingServerHelper).queryServer(any(), any());
		FhirResponse<List<IBaseResource>> iResourceList = queryingServerHelper.queryResourcesResponse("Observation",patient.getId().getIdPart(), null, IDENTIFIER);
		Assert.assertNotNull(iResourceList);
		doReturn(fhirResponsePatient).when(queryingServerHelper).getResourceByIdResponse(any(), any());
		FhirResponse<IBaseResource> iResource = queryingServerHelper.getResourceByIdResponse("Patient", patient.getId().getIdPart());
		Assert.assertNotNull(iResource.getResult());
	}

	@Test
	public void fhirQueryTest() {
		doReturn(fhirResponse).when(queryingServerHelper).queryServer(any(), any());
		FhirResponse<List<IBaseResource>> iResourceList = queryingServerHelper.queryResourcesResponse("Patient", null, null, IDENTIFIER);
		Assert.assertNotNull(iResourceList);
	}

	@Test
	public void queryPartTest() {
		List<IBaseResource> NullIBaseResource = new ArrayList<IBaseResource>();
		FhirResponse<List<IBaseResource>> fhirResponseNull = new FhirResponse<List<IBaseResource>>(NullIBaseResource, 200, "testing");
		doReturn(fhirResponseNull).when(queryingServerHelper).queryServer(any(), any());
		FhirResponse<List<IBaseResource>> iResourceList = queryingServerHelper.queryResourcesResponse("Observation",patient.getId().getIdPart(), SUBJECT, null);
		Assert.assertNotNull(iResourceList);
	}

	@Test
	public void fhirQueryAndQueryPartTestWithNullFhirVersion() {
		doReturn(fhirResponse).when(queryingServerHelper).queryServer(any(), any());
		doReturn(fhirResponseObservation).when(queryingServerHelper).getResourceByIdResponse(any(), any());
		FhirResponse<List<IBaseResource>> iResourceList = queryingServerHelper.queryResourcesResponse("Observation",patient.getId().getIdPart(), SUBJECT, IDENTIFIER);
		Assert.assertNotNull(iResourceList);
		FhirResponse<IBaseResource> iResource = queryingServerHelper.getResourceByIdResponse("Observation", observation.getId().getIdPart());
		Assert.assertNotNull(iResource);
	}

	@Test
	public void testCernerQueryNoAuth() {
		FhirResponse<?> fhirResponseCerner = new FhirResponse<Object>( null , 401, "Unauthorized");
		doReturn(fhirResponseCerner).when(queryingServerHelper).getResourceByIdResponse(any(), any());
		FhirResponse<IBaseResource> ires = queryingServerHelper.getResourceByIdResponse("Patient", "4342012");
		Assert.assertNotNull(ires);
		Assert.assertNull(ires.getResult());
		Assert.assertEquals(401, ires.getResponseStatusCode());
	}

	@Test
	public void testCernerQuery2NoAuth() {
		FhirResponse<?> fhirResponseCerner = new FhirResponse<Object>( null , 401, "Unauthorized");
		doReturn(fhirResponseCerner).when(queryingServerHelper).queryResourcesResponse(any(), any(),any(),any());
		FhirResponse<List<IBaseResource>> ires = queryingServerHelper.queryResourcesResponse("MedicationAdministration",
				null, SUBJECT, "patient=4342012");
		Assert.assertNotNull(ires);
		Assert.assertNull(ires.getResult());
		Assert.assertEquals(401, ires.getResponseStatusCode());
	}

	@Test
	public void queryPartTestWith_countQuery() throws UnsupportedEncodingException {
		String query = queryingServerHelper.getResourceQuery("Observation", "2", SUBJECT, "_count=10");
		Assert.assertNotNull(query);
		Assert.assertFalse(query.contains("&_count=50"));
	}

	@Test
	public void queryPartTestWithout_countQuery() throws UnsupportedEncodingException {
		String query = queryingServerHelper.getResourceQuery("Observation", "2", "SUBJECT", "patient_count=10");
		Assert.assertNotNull(query);
		Assert.assertTrue(query.contains("&_count=50"));
	}

	@Test
	public void queryPartTestWithNot_countQuery() throws UnsupportedEncodingException {
		String query = queryingServerHelper.getResourceQuery("Observation", "2", SUBJECT, "count=10");
		Assert.assertNotNull(query);
		Assert.assertTrue(query.contains("&_count=50"));
	}

	public static Patient returnFHIR2TestPatient() {
		Patient testpatient = new Patient();
		testpatient.addIdentifier().setSystem("urn:oid:0.1.2.3.4.5.6.7").setValue("123456");
		testpatient.addName().addFamily("Snow").addGiven("John");
		testpatient.setGender(AdministrativeGenderEnum.MALE);
		testpatient.setId("1");
		return (Patient) testpatient;
	}

	public static Observation returnFHIR2TestObservation() {
		Observation testobservation = new Observation();
		testobservation.setSubject(new ResourceReferenceDt(patient.getId().getValue()));
		testobservation.addIdentifier().setSystem("urn:oid:0.1.2.3.4.5.6.7").setValue("123456");
		testobservation.setId("2");
		return (Observation) testobservation;
	}
}
