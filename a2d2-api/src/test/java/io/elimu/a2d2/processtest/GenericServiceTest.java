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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.springframework.mock.env.MockEnvironment;

import io.elimu.a2d2.cdsmodel.Dependency;
import io.elimu.a2d2.genericmodel.ServiceRequest;
import io.elimu.a2d2.genericmodel.ServiceResponse;
import io.elimu.a2d2.process.ServiceUtils;
import io.elimu.genericapi.service.GenericKieBasedService;
import io.elimu.genericapi.service.RunningServices;
import io.elimu.serviceapi.service.AppContextUtils;

//@Ignore("This test was for auth services, which were phased out")
public class GenericServiceTest {

	@BeforeClass
	public static void setUpStatic() {
		System.setProperty("kie.maven.offline.force", "true");
		MockEnvironment environment = new MockEnvironment();
		environment.setActiveProfiles("local", "test", "default");
		AppContextUtils.getInstance().setEnvironment(environment);
	}
	
	@Before
	public void setUp() throws Exception {
		Properties prop = new Properties();
		prop.load(GenericServiceTest.class.getResourceAsStream("/generic-service-test.properties"));
		for (Object key : prop.keySet()) {
			System.setProperty(String.valueOf(key), prop.getProperty(String.valueOf(key)));
		}
		deployManualKjar();
		deployManualDbKjar();
	}
	

	
	private void deployManualKjar() throws IOException {
		GenericTestUtils.deployKjar("generic-example-service.dsl", "1");
	}
	
	private void deployManualDbKjar() throws Exception {
		GenericTestUtils.deployKjar("generic-example-db-service.dsl", "2");
	}

	
	@Test
	public void testKieServiceDef() throws Exception {
		String releaseId = System.getProperty("test.service.release") + ".test1";
		Dependency dep = new Dependency(releaseId);
		RunningServices.getInstance().register(new GenericKieBasedService(dep));
		GenericTestUtils.genericServiceTestingSuccess(dep);
	}

	@Test
	public void testKieServiceDefNoAuthHeader() throws Exception {
		String releaseId = System.getProperty("test.service.release") + ".test1";
		Dependency dep = new Dependency(releaseId);
		RunningServices.getInstance().register(new GenericKieBasedService(dep));
		GenericTestUtils.genericServciceTestingNoAuthFail(dep);
	}

	@Test
	public void testKieServiceNoAuthButOverridenConfig() throws Exception {
		String releaseId = System.getProperty("test.service.release") + ".test1";
		Assert.assertNotNull(releaseId);
		Dependency dep = new Dependency(releaseId);
		File f = new File(System.getProperty("java.io.tmpdir") + File.separator + dep.getArtifactId() + ".properties");
		try {
			System.setProperty("kie.apps.config.folder", f.getParentFile().getAbsolutePath());
			String props = "kie.project.validationtype=LIST\n" +
					"kie.project.authtype=BASIC\n" +
					"kie.project.logexec=true\n" +
					"kie.project.auth.list.mary=marypass\n" +
					"kie.project.auth.list.john=J0hnYYY!!\n";
			Files.write(Paths.get(f.toURI()), props.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			RunningServices.getInstance().register(new GenericKieBasedService(dep));
			GenericTestUtils.genericServciceTestingNoAuthFail(dep);
			GenericTestUtils.genericServciceTestingAuthTesting(dep, "john", "J0hnYYY!!", 200);
			GenericTestUtils.genericServciceTestingAuthTesting(dep, "mary", "wrongpass", /*401*/ 200);
		} finally {
			f.delete();
		}
	}
	
	@Test
	public void testKieServiceAuthWithDatabase() throws Exception {
		String releaseId = System.getProperty("test.service.release") + ".test2";
		Assert.assertNotNull(releaseId);
		Dependency dep = new Dependency(releaseId);
		RunningServices.getInstance().register(new GenericKieBasedService(dep));
		GenericKieBasedService service = (GenericKieBasedService) RunningServices.getInstance().get(dep.getArtifactId());
		Assert.assertNotNull(service);
		initDb(service.getConfig(), "user", "pass");
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
		
		String auth2 = "Basic " + Base64.getEncoder().encodeToString("user:wrongpass".getBytes());
		ServiceRequest request2 = new ServiceRequest();
		request2.addHeaderValue("Authorization", auth2);
		ServiceResponse response2 = service.execute(request2);
		Assert.assertEquals(/*401*/ 200, response2.getResponseCode().intValue());

	}

	private void initDb(Properties props, String username, String password) {
		String user = props.getProperty("kie.project.auth.dbuser");
		String pass = props.getProperty("kie.project.auth.dbpass");
		String url = props.getProperty("kie.project.auth.dburl");
		try (Connection conn = DriverManager.getConnection(url, user, pass)) {
			try (Statement stmt = conn.createStatement()) {
				stmt.execute("create table my_users (usr varchar(500) not null primary key,pass varchar(500) not null)");
			} catch (Exception e) { }
			try (Statement stmt = conn.createStatement()) {
				conn.createStatement().execute("insert into my_users (usr, pass) values ('" + username + "', MD5('" + password + "'))");
			} catch (Exception e) { }
		} catch (Exception e) { }
	}
}
