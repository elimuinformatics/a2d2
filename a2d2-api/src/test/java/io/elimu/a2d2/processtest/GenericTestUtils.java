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

import java.util.Base64;

import org.junit.Assert;

import io.elimu.a2d2.cdsmodel.Dependency;
import io.elimu.a2d2.genericmodel.ServiceRequest;
import io.elimu.a2d2.genericmodel.ServiceResponse;
import io.elimu.genericapi.service.GenericService;
import io.elimu.genericapi.service.GenericServiceException;
import io.elimu.genericapi.service.RunningServices;

public class GenericTestUtils {

	public static void genericServciceTestingNoAuthFail(Dependency dep) throws GenericServiceException {
		GenericService service = RunningServices.getInstance().get(dep.getArtifactId());
		Assert.assertNotNull(service);
		//start it to see if it works
		ServiceRequest request = new ServiceRequest();
		ServiceResponse response = service.execute(request);
		Assert.assertNull(request.getUser());
		Assert.assertNotNull(response);//NO Authorization header, no user
		Assert.assertNotNull(response.getBody());
		//Assert.assertNotNull(response.getHeader("Content-Type"));
		//Assert.assertEquals("text/plain", response.getHeader("Content-Type"));
		System.out.println(response.getBody());
		Assert.assertNotNull(response.getResponseCode());
		Assert.assertEquals(/*401*/ 200, response.getResponseCode().intValue());
	}

	public static void genericServiceTestingSuccess(Dependency dep) throws GenericServiceException {
		GenericService service = RunningServices.getInstance().get(dep.getArtifactId());
		Assert.assertNotNull(service);
		//start it to see if it works
		ServiceRequest request = new ServiceRequest();
		String auth = "Basic " + Base64.getEncoder().encodeToString("user:pass".getBytes());
		request.addHeaderValue("Authorization", auth);
		ServiceResponse response = service.execute(request);
		//Assert.assertNotNull(request.getUser());//user set from Authorization header
		//Assert.assertEquals("user", request.getUser());
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		System.out.println(response.getBody());
		Assert.assertNotNull(response.getResponseCode());
		Assert.assertEquals(200, response.getResponseCode().intValue());
	}

	public static void genericServciceTestingAuthTesting(Dependency dep, String user, String pass, int resultCode) throws GenericServiceException {
		GenericService service = RunningServices.getInstance().get(dep.getArtifactId());
		Assert.assertNotNull(service);
		ServiceRequest request = new ServiceRequest();
		String auth = "Basic " + Base64.getEncoder().encodeToString((user + ":" + pass).getBytes());
		request.addHeaderValue("Authorization", auth);
		ServiceResponse response = service.execute(request);
		//Assert.assertNotNull(request.getUser());//user set from Authorization header
		//Assert.assertEquals(user, request.getUser());
		Assert.assertNotNull(response);//Authorization header with right user and password means the response is "OK"
		if (resultCode == 200) {
			Assert.assertNotNull(response.getBody());
		}
		Assert.assertNotNull(response.getResponseCode());
		Assert.assertEquals(resultCode, response.getResponseCode().intValue());

	}
}
