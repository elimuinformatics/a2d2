package io.elimu.genericapi.service;

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
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieServices;
import org.mockito.junit.MockitoJUnitRunner;

import io.elimu.a2d2.cdsmodel.Dependency;
import io.elimu.a2d2.process.ServiceUtils;
import io.elimu.service.models.ServiceInfo;
import io.elimu.serviceapi.service.AppContextUtils;
import io.jsonwebtoken.lang.Assert;

@RunWith(MockitoJUnitRunner.class)
public class RunningServicesTest {
	
	ServiceInfo serviceInfo = new ServiceInfo("example-service", 5L, "io.elimu.generic:example-service:1.0.1",
			"generic", "testing", "cds-services", "done");

	@BeforeClass
	public static void setUp() throws Exception {

		Properties properties = new Properties();
		properties.load(RunningServicesTest.class.getClassLoader().getResourceAsStream("config.properties"));
		System.setProperty("test.service.release", properties.getProperty("test.service.release"));
		System.setProperty("kie.wb.location", properties.getProperty("kie.wb.location"));
		System.setProperty("kie.wb.user", properties.getProperty("kie.wb.user"));
		System.setProperty("kie.wb.pwd", properties.getProperty("kie.wb.pwd"));
		AppContextUtils.getInstance().testSetEnvironmentName("dev");
		createProject(1);
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

	public static void createProject(int version) throws IOException {
		KieServices ks = KieServices.get();
		String releaseId = System.getProperty("test.service.release") + "." + version;
		Dependency dep = new Dependency(releaseId);
		ClassPathResource drl = (ClassPathResource) ks.getResources().newClassPathResource("test-rules.drl");
		ClassPathResource dsl = (ClassPathResource) ks.getResources()
				.newClassPathResource("test-service.dsl");
		ClassPathResource dsl2 = (ClassPathResource) ks.getResources()
				.newClassPathResource("test-service-dev.dsl");
		ClassPathResource discoveryDsl = (ClassPathResource) ks.getResources()
				.newClassPathResource("discovery.dsl");
		ClassPathResource bpmn = (ClassPathResource) ks.getResources()
				.newClassPathResource("test-process.bpmn2");
		ByteArrayResource kmod = (ByteArrayResource) ks.getResources()
				.newByteArrayResource(ks.newKieModuleModel().toXML().getBytes());
		ByteArrayResource pom = (ByteArrayResource) ks.getResources().newByteArrayResource(
				("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
						+ "        xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
						+ "        <modelVersion>4.0.0</modelVersion>\n" + "        <groupId>" + dep.getGroupId()
						+ "</groupId>\n" + "        <artifactId>" + dep.getArtifactId() + "</artifactId>\n"
						+ "        <version>" + dep.getVersion() + "</version>\n"
						+ "        <packaging>jar</packaging>\n" + "</project>\n").getBytes());
		ByteArrayResource desc = (ByteArrayResource) ks.getResources()
				.newByteArrayResource(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
						+ "<deployment-descriptor xsi:schemaLocation=\"http://www.jboss.org/jbpm deployment-descriptor.xsd\""
						+ "                       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
						+ "    <persistence-unit></persistence-unit>\n"
						+ "    <audit-persistence-unit></audit-persistence-unit>\n"
						+ "    <audit-mode>NONE</audit-mode>\n" + "    <persistence-mode>NONE</persistence-mode>\n"
						+ "    <runtime-strategy>PER_PROCESS_INSTANCE</runtime-strategy>\n"
						+ "    <marshalling-strategies/>\n    <event-listeners/>\n    <task-event-listeners/>\n"
						+ "    <globals/>\n    <work-item-handlers/>\n    <environment-entries/>\n"
						+ "    <configurations/>\n    <required-roles/>\n    <remoteable-classes/>\n"
						+ "    <limit-serialization-classes>true</limit-serialization-classes>\n"
						+ "</deployment-descriptor>\n").getBytes());

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
		add("service-dev.dsl", dsl2, target);
		add("discovery.dsl", discoveryDsl, target);
		add("META-INF/kmodule.xml", kmod, target);
		target.close();

	}

	@Ignore("Broken, fix later")
	@Test
	public void testUnregisterWithoutDeletion() throws Exception {
		String releaseId = System.getProperty("test.service.release") + ".1";
		Dependency dep = new Dependency(releaseId);
		RunningServices.getInstance().register(new GenericKieBasedService(dep));
		GenericService service1 = RunningServices.getInstance().unregister(dep.getArtifactId(), true);
		Assert.notNull(service1);
	}
	
	@Ignore("Broken, fix later")
	@Test
	public void testUnregisterWithZeroDeletion() throws Exception {
		String releaseId = System.getProperty("test.service.release") + ".1";
		Dependency dep = new Dependency(releaseId);
		RunningServices.getInstance().register(new GenericKieBasedService(dep));
		GenericService service1 = RunningServices.getInstance().unregister(dep.getArtifactId(), false);
		Assert.isNull(service1);
		Properties props = ((GenericKieBasedService) service1).getConfig();
		Assert.isTrue("testingDev".equals(props.get("packages")), "packages property should have been testingDev but was " + props.get("packages"));
	}

}
