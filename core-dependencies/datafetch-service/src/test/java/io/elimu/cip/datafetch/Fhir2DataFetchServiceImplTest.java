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

package io.elimu.cip.datafetch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class Fhir2DataFetchServiceImplTest {

	private static final String FHIR2_SERVER_URL = "http://hapi.fhir.org/baseDstu2";
	private static final String MOCK_TOKEN = "12345-6789-1234567";

	@Ignore("Broken, fix later")
	@Test
	public void testFetchNoAuth() throws Exception {
		Fhir2DataFetchServiceImpl service = new Fhir2DataFetchServiceImpl();
		service.setBaseUrl(FHIR2_SERVER_URL);
		Assert.assertNotNull(service.fetchPatient("654321"));
		List<Object> medOrders = service.fetchMedicationOrders("654321");
		Assert.assertNotNull(medOrders);
		Assert.assertFalse(medOrders.isEmpty());
	}
    
	@Ignore("Broken, fix later")
	@Test
	public void testFetchBearerAuth() throws Exception {
		Fhir2DataFetchServiceImpl service = new Fhir2DataFetchServiceImpl();
		service.setBaseUrl(FHIR2_SERVER_URL);
		Map<String, Object> auth = new HashMap<>();
		auth.put("access_token", MOCK_TOKEN);
		service.setAuth(auth );
		//Assert.assertNotNull(service.fetchPatient("654321"));
		//List<Object> medOrders = service.fetchMedicationOrders("654321");
		//Assert.assertNotNull(medOrders);
		//Assert.assertFalse(medOrders.isEmpty());
	}
}
