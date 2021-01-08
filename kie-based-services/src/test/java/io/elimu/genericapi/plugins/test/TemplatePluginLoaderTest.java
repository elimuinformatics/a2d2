package io.elimu.genericapi.plugins.test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.commons.io.FileUtils;
import org.drools.core.io.impl.BaseResource;
import org.drools.core.io.impl.ByteArrayResource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.KieServices;

import io.elimu.a2d2.cdsmodel.Dependency;
import io.elimu.a2d2.cdsresponse.entity.TemplateRepository;
import io.elimu.a2d2.process.ServiceUtils;
import io.elimu.genericapi.plugins.FtlLoaderPluginLoader;

public class TemplatePluginLoaderTest {

	private Dependency dep;
	
	private Dependency subDep;

	@Before
	public void setUp() throws Exception {
		Properties prop = new Properties();
		prop.load(TemplatePluginLoaderTest.class.getResourceAsStream("/template-plugin-test.properties"));
		for (Object key : prop.keySet()) {
			System.setProperty(String.valueOf(key), prop.getProperty(String.valueOf(key)));
		}
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

	private void createDepJarWithFTL() throws FileNotFoundException, IOException {
		KieServices ks = KieServices.get();
		String releaseId = System.getProperty("test.dep.release"); 
		subDep = new Dependency(releaseId);
		URL path = ServiceUtils.toJarPath(subDep, false);
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		File f = new File(path.getFile());
		f.getParentFile().mkdirs();
		ByteArrayResource ftlFile = (ByteArrayResource) ks.getResources()
				.newByteArrayResource(("<html>\n" + "<head>\n" + "  <title>Hello World :: ${title}\n" + "</head>\n"
						+ "<body>\n" + "  <h1>${title}</h1></body></html>").getBytes());

		ByteArrayResource pom = (ByteArrayResource) ks.getResources().newByteArrayResource(
				("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
						+ "        xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
						+ "        <modelVersion>4.0.0</modelVersion>\n" + "        <groupId>" + subDep.getGroupId()
						+ "</groupId>\n" + "        <artifactId>" + subDep.getArtifactId() + "</artifactId>\n"
						+ "        <version>" + subDep.getVersion() + "</version>\n"
						+ "        <packaging>jar</packaging>\n" + "</project>\n").getBytes());

		JarOutputStream target = new JarOutputStream(new FileOutputStream(f), manifest);
		add("io/elimu/generic/test.ftl", ftlFile, target);
		add("META-INF/maven/" + subDep.getGroupId() + "/" + subDep.getArtifactId() + "/pom.xml", pom, target);
		target.close();
	}

	private void createServiceJarWithDep() throws IOException {
		KieServices ks = KieServices.get();
		String releaseId = System.getProperty("test.service.release") + ".test"; // so we don't overwrite module from
																					// integration test
		dep = new Dependency(releaseId);
		ByteArrayResource kmod = (ByteArrayResource) ks.getResources()
				.newByteArrayResource(ks.newKieModuleModel().toXML().getBytes());

		// Add dep
		ByteArrayResource pom = (ByteArrayResource) ks.getResources().newByteArrayResource(
				("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
						+ "        xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
						+ "        <modelVersion>4.0.0</modelVersion>\n" + "        <groupId>" + dep.getGroupId()
						+ "</groupId>\n" + "        <artifactId>" + dep.getArtifactId() + "</artifactId>\n"
						+ "        <version>" + dep.getVersion() + "</version>\n"
						+ "        <packaging>jar</packaging>\n" + "<dependencies>\n" + "<dependency>\n" + "<groupId>"
						+ subDep.getGroupId() + "</groupId>\n" + "<artifactId>" + subDep.getArtifactId()
						+ "</artifactId>\n" + "<version>" + subDep.getVersion() + "</version>\n" + "</dependency>\n"
						+ "</dependencies>\n" + "</project>\n").getBytes());
		URL path = ServiceUtils.toJarPath(dep, false);
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		File f = new File(path.getFile());
		f.getParentFile().mkdirs();
		JarOutputStream target = new JarOutputStream(new FileOutputStream(f), manifest);
		add("META-INF/maven/" + dep.getGroupId() + "/" + dep.getArtifactId() + "/pom.xml", pom, target);
		add("META-INF/kmodule.xml", kmod, target);
		target.close();
	}
	
	@After
	public void cleanUp() throws Exception {
		URL depURL = ServiceUtils.toJarPath(dep, false);
		URL subDepURL = ServiceUtils.toJarPath(subDep, false);
		FileUtils.forceDelete(new File(depURL.getPath()));
		FileUtils.forceDelete(new File(subDepURL.getPath()));
	}

	@Ignore("Broken, fix later")
	@Test
	public void testDependenciesTemplateLoad() throws IOException {

		createDepJarWithFTL();
		createServiceJarWithDep();

		FtlLoaderPluginLoader ftp = new FtlLoaderPluginLoader();
		ftp.process(dep);

		String ftlkey = subDep.getArtifactId() + "_test.ftl";
		HashMap<?,?> ftlmap = (HashMap<?,?>) TemplateRepository.getInstance().getFltMap();
		Assert.assertTrue(ftlmap.size() >= 1);
		Assert.assertNotNull(ftlmap.get(ftlkey));
	}
}
