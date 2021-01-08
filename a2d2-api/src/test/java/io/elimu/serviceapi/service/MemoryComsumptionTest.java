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

import io.elimu.a2d2.cdsmodel.Dependency;
import io.elimu.a2d2.genericmodel.ServiceRequest;
import io.elimu.a2d2.process.ServiceUtils;
import io.elimu.a2d2.processtest.GenericServiceTest;
import io.elimu.genericapi.service.GenericKieBasedService;

public class MemoryComsumptionTest {

	@BeforeClass
	public static void before() throws Exception {
		System.setProperty("kie.wb.location", "https:/kie-workbench-url/");
		System.setProperty("kie.wb.user", "kie");
		System.setProperty("kie.wb.pwd", "kie");
	}
	
	
	private void deployKjar(String serviceDslPath, String count) throws IOException {
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

	private void add(String path, BaseResource resource, JarOutputStream target) throws IOException {
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
	
	@Ignore("Broken, fix later")
	@Test
	public void testMemoryConsumption() throws Exception {
		Properties prop = new Properties();
		prop.load(GenericServiceTest.class.getResourceAsStream("/generic-service-test.properties"));
		for (Object key : prop.keySet()) {
			System.setProperty(String.valueOf(key), prop.getProperty(String.valueOf(key)));
		}
		deployKjar("generic-example-service.dsl", "1");
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
