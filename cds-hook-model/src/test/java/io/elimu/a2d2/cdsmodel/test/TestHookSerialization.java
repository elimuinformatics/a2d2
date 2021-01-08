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

package io.elimu.a2d2.cdsmodel.test;

import static org.junit.Assert.*;

import org.junit.Test;

import io.elimu.a2d2.cdsresponse.entity.CDSHookRequest;
import com.google.gson.Gson;

public class TestHookSerialization {

	@Test
	public void test() {
		String hookRequestStr = "{\"hook\":\"patient-view\",\"hookInstance\":\"d1577c69-dfbe-44ad-ba6d-3e05e953b2ea\",\"fhirServer\":\"https://fhir-ehr.sandboxcerner.com/dstu2/0b8a0111-e8e6-4c26-a91c-5069cbc6b1ca\",\"user\":\"Practitioner/COREPRACTITIONER1\",\"patient\":\"4342008\",\"context\":{\"patientId\":\"4342008\"},\"fhirAuthorization\":{\"access_token\":\"Bearer 1234\",\"token_type\":\"Bearer\",\"expires_in\":570,\"scope\":\"patient/Patient.read patient/Observation.read\",\"subject\":\"cds-service4\"},\"prefetch\":{}}";
		Gson gson = new Gson();
		CDSHookRequest request = gson.fromJson(hookRequestStr, CDSHookRequest.class);
		assertNotNull(request.getHook());
		assertNotNull(request.getFhirAuthorization().getAccessToken());
		assertEquals(request.getFhirAuthorization().getAccessToken(), "Bearer 1234");
	}

}
