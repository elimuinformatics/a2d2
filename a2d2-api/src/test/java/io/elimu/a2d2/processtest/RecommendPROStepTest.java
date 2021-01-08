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

package io.elimu.a2d2.processtest;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;

import io.elimu.a2d2.cdsresponse.entity.CDSRequest;
import io.elimu.a2d2.cdsresponse.entity.CDSResponse;
import io.elimu.a2d2.genericmodel.ServiceRequest;
import io.elimu.a2d2.genericmodel.ServiceResponse;
import io.elimu.genericapi.service.GenericKieBasedService;

public class RecommendPROStepTest {

	@Test @Ignore("SAP-925 until we change the kJAR")
	public void testKieServiceDef() throws Exception {
		GenericKieBasedService service = new GenericKieBasedService("io.elimu.a2d2:pro-recommend:0.0.2", "elimu");
		Assert.assertNotNull(service);
		//start it to see if it works
		CDSRequest request = new CDSRequest();
		request.setFhirServer("https://fhir-34.219.13.163:9092/baseDstu2");
		String json = "{\"prefetch\": { \"patient\": { {\"resourceType\" : \"Patient\", \"gender\" : \"male\", \"birthDate\" : \"1925-12-23\","
				+ "\"id\" : \"1288992\", \"active\" : true }}}}";
		ServiceRequest serviceRequest = new ServiceRequest();
		serviceRequest.setBody(json);
		serviceRequest.addHeaderValue("Content-Type", "application/json");
		ServiceResponse response = (ServiceResponse) service.execute(serviceRequest);
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody()	);
		System.out.println(response.getBody());
		CDSResponse cdsresp = new Gson().fromJson(response.getBody(), CDSResponse.class);
		Assert.assertNotNull(cdsresp.getCards());
		Assert.assertEquals(2, cdsresp.getCards().size());
	}
}
