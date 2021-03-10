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
import java.util.Base64;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.drools.core.io.impl.BaseResource;
import org.drools.core.io.impl.ByteArrayResource;
import org.drools.core.io.impl.ClassPathResource;
import org.junit.Assert;
import org.kie.api.KieServices;

import io.elimu.a2d2.cdsmodel.Dependency;
import io.elimu.a2d2.genericmodel.ServiceRequest;
import io.elimu.a2d2.genericmodel.ServiceResponse;
import io.elimu.a2d2.process.ServiceUtils;
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
	
	public static void deployKjar(String serviceDslPath, String count) throws IOException {
		KieServices ks = KieServices.get();
		String releaseId = System.getProperty("test.service.release") + ".test" + count; //so we don't overwrite module from integration test
		Dependency dep = new Dependency(releaseId);
		ClassPathResource drl = (ClassPathResource) ks.getResources().newClassPathResource("generic-example-rules.drl");
		ClassPathResource dsl = (ClassPathResource) ks.getResources().newClassPathResource(serviceDslPath);
		ClassPathResource bpmn = (ClassPathResource) ks.getResources().newClassPathResource("generic-example-process.bpmn2");
		ByteArrayResource kmod = (ByteArrayResource) ks.getResources().newByteArrayResource(ks.newKieModuleModel().toXML().getBytes());
		ByteArrayResource pom = (ByteArrayResource) ks.getResources().newByteArrayResource(
				("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"        xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" + 
				"        <modelVersion>4.0.0</modelVersion>\n" + 
				"        <groupId>" + dep.getGroupId() + "</groupId>\n" + 
				"        <artifactId>" + dep.getArtifactId() + "</artifactId>\n" + 
				"        <version>" + dep.getVersion() + "</version>\n" + 
				"        <packaging>jar</packaging>\n" + 
				"</project>\n").getBytes());
		ByteArrayResource desc = (ByteArrayResource) ks.getResources().newByteArrayResource(
				("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
						"<deployment-descriptor xsi:schemaLocation=\"http://www.jboss.org/jbpm deployment-descriptor.xsd\"" +
						"                       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + 
						"    <persistence-unit></persistence-unit>\n" + 
						"    <audit-persistence-unit></audit-persistence-unit>\n" + 
						"    <audit-mode>NONE</audit-mode>\n" + 
						"    <persistence-mode>NONE</persistence-mode>\n" + 
						"    <runtime-strategy>PER_PROCESS_INSTANCE</runtime-strategy>\n" + 
						"    <marshalling-strategies/>\n    <event-listeners/>\n    <task-event-listeners/>\n" + 
						"    <globals/>\n    <work-item-handlers/>\n    <environment-entries/>\n" + 
						"    <configurations/>\n    <required-roles/>\n    <remoteable-classes/>\n" + 
						"    <limit-serialization-classes>true</limit-serialization-classes>\n" + 
						"</deployment-descriptor>\n").getBytes());
		URL path = ServiceUtils.toJarPath(dep, false);
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		File f = new File(path.getFile());
		f.getParentFile().mkdirs();
		JarOutputStream target = new JarOutputStream(new FileOutputStream(f), manifest);
		add("META-INF/kie-deployment-descriptor.xml", desc, target);
		add("META-INF/maven/" + dep.getGroupId() + "/" + dep.getArtifactId() + "/pom.xml", pom, target);
		add("io/elimu/generic/examplerules.drl", drl, target);
		add("io/elimu/generic/example.bpmn2", bpmn, target);
		add("service.dsl", dsl, target);
		add("META-INF/kmodule.xml", kmod, target);
		target.close();
	}
	
	private static void add(String path, BaseResource resource, JarOutputStream target) throws IOException {
		  BufferedInputStream in = null;
		  try {
			  byte[] content = resource.getBytes();
			  JarEntry entry = new JarEntry(path);
			  entry.setTime(System.currentTimeMillis() - 100_000);
			  target.putNextEntry(entry);
			  in = new BufferedInputStream(new ByteArrayInputStream(content));
			  byte[] buffer = new byte[1024];
			  while (true) {
				  int count = in.read(buffer);
				  if (count == -1) {
					  break;
				  }
				  target.write(buffer, 0, count);
			  }
			  target.closeEntry();
		  	} finally {
		  		if (in != null) {
		  			in.close();
		  		}
		  	}
		}
}
