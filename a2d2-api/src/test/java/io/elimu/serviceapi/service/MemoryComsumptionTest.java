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

package io.elimu.serviceapi.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.drools.core.io.impl.BaseResource;
import org.drools.core.io.impl.ByteArrayResource;
import org.drools.core.io.impl.ClassPathResource;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.KieServices;
import org.springframework.mock.env.MockEnvironment;

import io.elimu.a2d2.cdsmodel.Dependency;
import io.elimu.a2d2.genericmodel.ServiceRequest;
import io.elimu.a2d2.process.ServiceUtils;
import io.elimu.a2d2.processtest.GenericServiceTest;
import io.elimu.a2d2.processtest.GenericTestUtils;
import io.elimu.genericapi.service.GenericKieBasedService;

public class MemoryComsumptionTest {

	@BeforeClass
	public static void before() throws Exception {
		System.setProperty("kie.maven.offline.force", "true");
		MockEnvironment environment = new MockEnvironment();
		environment.setActiveProfiles("local", "test", "default");
		AppContextUtils.getInstance().setEnvironment(environment);
	}

	@Test
	public void testMemoryConsumption() throws Exception {
		Properties prop = new Properties();
		prop.load(GenericServiceTest.class.getResourceAsStream("/generic-service-test.properties"));
		for (Object key : prop.keySet()) {
			System.setProperty(String.valueOf(key), prop.getProperty(String.valueOf(key)));
		}
		GenericTestUtils.deployKjar("generic-example-service.dsl", "1");
		String releaseId = System.getProperty("test.service.release") + ".test1";
		Dependency dep = new Dependency(releaseId);
		GenericKieBasedService service = new GenericKieBasedService(dep);
		ServiceRequest request = new ServiceRequest();
		String auth = "Basic " + Base64.getEncoder().encodeToString("user:pass".getBytes());
		request.addHeaderValue("Authorization", auth);
		System.gc(); //lets cleanup before counting
		long usedMem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		for (int i = 0; i < 100; i++) {
			service.execute(request);
			if (i % 10 == 0) {
				System.out.println("Executed " + i + " cases");
			}
		}
		System.gc();
		long usedMem2 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		
		System.out.println("Used memory before: " + (usedMem1/1024) + " KB");
		System.out.println("Used memory after: " + (usedMem2/1024) + "KB");
		long diffMem = (usedMem2 - usedMem1)/1024;
		System.out.println("Difference: " + diffMem + " KB"); //393305752
		Assert.assertTrue("Expected under 3MB of extended consumption but found " + diffMem, diffMem < 3_000); //less than 1 MB of increased use of memory between begin and end of executions
		
		/*without cleanup for 100 executions:    difference -9076 KB
		  without cleanup for 1000 executions:   difference 20106 KB
		  without cleanup for 10000 executions:  difference 184920 KB
		  without cleanup for 25000 executions:  difference 984081 KB
		  without cleanup for 100000 executions: Failure at 80k executions due to OOM
		
		  with cleanup for 100 executions:     difference -981 KB
		  with cleanup for 1000 executions:    difference 634 KB
		  with cleanup for 10000 executions:   difference 683 KB
          with cleanup for 50000 executions:   difference: 4379 KB
		  with cleanup for 100000 executions:  difference: -1619 KB
		*/
	}
}
