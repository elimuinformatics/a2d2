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

import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import io.elimu.a2d2.cdsmodel.Dependency;
import io.elimu.genericapi.service.RunningServices;

@Ignore("This was to test Auth service which has phased out")
public class GenericServiceIntegrationTest {

	@Before
	public void setUp() throws Exception {
		Properties prop = new Properties();
		prop.load(GenericServiceIntegrationTest.class.getResourceAsStream("/generic-service-test.properties"));
		for (Object key : prop.keySet()) {
			System.setProperty(String.valueOf(key), prop.getProperty(String.valueOf(key)));
		}
	}
	
	@Test
	public void testKieServiceDef() throws Exception {
		String releaseId = System.getProperty("test.service.release");
		Assert.assertNotNull(releaseId);
		Dependency dep = new Dependency(releaseId);
		RunningServices.getInstance().downloadDependency(dep);
		RunningServices.getInstance().restartService(dep);
		GenericTestUtils.genericServiceTestingSuccess(dep);
	}

	@Test
	public void testKieServiceDefNoAuthHeader() throws Exception {
		String releaseId = System.getProperty("test.service.release");
		Assert.assertNotNull(releaseId);
		Dependency dep = new Dependency(releaseId);
		RunningServices.getInstance().downloadDependency(dep);
		RunningServices.getInstance().restartService(dep);
		GenericTestUtils.genericServciceTestingNoAuthFail(dep);
	}

}
