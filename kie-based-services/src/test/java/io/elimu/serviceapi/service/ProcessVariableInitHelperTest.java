package io.elimu.serviceapi.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.drools.core.io.impl.BaseResource;
import org.drools.core.io.impl.ByteArrayResource;
import org.drools.core.io.impl.ClassPathResource;
import org.junit.Before;
import org.kie.api.KieServices;
import org.mockito.InjectMocks;

import io.elimu.a2d2.cdsmodel.Dependency;
import io.elimu.a2d2.process.ServiceUtils;

public class ProcessVariableInitHelperTest {
	

	@InjectMocks
	ProcessVariableInitHelper pvh=new ProcessVariableInitHelper();
	
	
		@Before
		public void ssetUp() throws Exception {
			Properties prop = new Properties();
			prop.load(ProcessVariableInitHelperTest.class.getResourceAsStream("/generic-service-test.properties"));
			for (Object key : prop.keySet()) {
				System.setProperty(String.valueOf(key), prop.getProperty(String.valueOf(key)));
			}
			deployManualKjar();
			deployManualDbKjar();
		
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
		
		private void deployManualKjar() throws IOException {
			deployKjar("generic-example-service.dsl", "4");
		}
		
		private void deployManualDbKjar() throws Exception {
			deployKjar("generic-example-db-service.dsl", "4");
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


}
