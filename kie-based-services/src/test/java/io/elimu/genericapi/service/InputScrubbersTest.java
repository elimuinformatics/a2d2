package io.elimu.genericapi.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
import io.elimu.a2d2.genericmodel.ServiceRequest;
import io.elimu.a2d2.process.ServiceUtils;
import io.elimu.service.models.ServiceInfo;
import io.jsonwebtoken.lang.Assert;

@RunWith(MockitoJUnitRunner.class)
public class InputScrubbersTest {

	ServiceInfo serviceInfo = new ServiceInfo("input-show-service", 5L, "io.elimu.generic:input-show-service:1.0.1",
			"generic", "testing", "cds-services", "done", new ArrayList<>());

	@BeforeClass
	public static void setUp() throws Exception {
		Properties properties = new Properties();
		properties.load(RunningServicesTest.class.getClassLoader().getResourceAsStream("config.properties"));
		System.setProperty("inputshow.service.release", properties.getProperty("inputshow.service.release"));
		System.setProperty("kie.wb.location", properties.getProperty("kie.wb.location"));
		System.setProperty("kie.wb.user", properties.getProperty("kie.wb.user"));
		System.setProperty("kie.wb.pwd", properties.getProperty("kie.wb.pwd"));
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
		String releaseId = System.getProperty("inputshow.service.release") + "." + version;
		Dependency dep = new Dependency(releaseId);
		ClassPathResource dsl = (ClassPathResource) ks.getResources()
				.newClassPathResource("inputshow-service.dsl");

		ClassPathResource discoveryDsl = (ClassPathResource) ks.getResources()
				.newClassPathResource("inputshow-discovery.dsl");
		ClassPathResource bpmn = (ClassPathResource) ks.getResources()
				.newClassPathResource("inputshow.bpmn2");
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
		add("io/elimu/generic/inputshow.bpmn2", bpmn, target);
		add("service.dsl", dsl, target);
		add("discovery.dsl", discoveryDsl, target);
		add("META-INF/kmodule.xml", kmod, target);
		target.close();

	}

	@Ignore("Broken, fix later")
	@Test
	public void testHtmlScrubbing() throws Exception {
		String releaseId = System.getProperty("inputshow.service.release") + ".1";
		Dependency dep = new Dependency(releaseId);
		RunningServices.getInstance().register(new GenericKieBasedService(dep));
		GenericService service1 = RunningServices.getInstance().get(dep.getArtifactId());
		Assert.notNull(service1);
		
		ServiceRequest request = new ServiceRequest();
		request.addHeaderValue("Content-Type", "text/html");
		String bodyBefore = "<p><a href='http://example.com/' onclick='stealCookies()'>Link</a></p>";;
		String bodyAfterExpected = "<p><a href=\"http://example.com/\" rel=\"nofollow\">Link</a></p>";
		request.setBody(bodyBefore);
		Logger.getInstance().log(null);
		Assert.isNull(Logger.getInstance().getBody());
		service1.execute(request);
		
		String bodyAfterActual = Logger.getInstance().getBody();
		Assert.notNull(bodyAfterActual);
		Assert.isTrue(bodyAfterActual.equals(bodyAfterExpected));
	}
	
	@Ignore("Broken, fix later")
	@Test
	public void testJsonScrubbing() throws Exception {
		String releaseId = System.getProperty("inputshow.service.release") + ".1";
		System.err.println(releaseId);
		Dependency dep = new Dependency(releaseId);
		RunningServices.getInstance().register(new GenericKieBasedService(dep));
		GenericService service1 = RunningServices.getInstance().get(dep.getArtifactId());
		Assert.notNull(service1);
		
		
		ServiceRequest request = new ServiceRequest();
		request.addHeaderValue("Content-Type", "application/json");
		String bodyBefore = "{\"foo\",\"bar\", }";
		String bodyAfterExpected = "{\"foo\":\"bar\" }";
		request.setBody(bodyBefore);
		Logger.getInstance().log(null);
		Assert.isNull(Logger.getInstance().getBody());
		service1.execute(request);
		
		String bodyAfterActual = Logger.getInstance().getBody();
		Assert.notNull(bodyAfterActual);
		Assert.isTrue(bodyAfterActual.equals(bodyAfterExpected));
	}

	public static class Logger {
		
		private static final Logger INSTANCE = new Logger();
		
		private Logger() {
		}
		
		public static Logger getInstance() {
			return INSTANCE;
		}
		
		private String body;
		
		public void log(String body) {
			this.body = body;
		}
		
		public String getBody() {
			return body;
		}
	}

}
