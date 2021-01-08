package io.elimu.genericapi.service;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import io.elimu.serviceapi.service.AppContextUtils;

public class MavenJarResolverTest {
	
	@BeforeClass
	public static void setUp() throws Exception {

		System.setProperty("kie.wb.location", "https://kie-wb");
		System.setProperty("kie.wb.user", "kie");
		System.setProperty("kie.wb.pwd", "kie");
		AppContextUtils.getInstance().testSetEnvironmentName("dev");
	}

	@Ignore("Broken, fix later")
	@Test
	public void testResolveDependencies() throws Exception {
		MavenJarResolver.resolveDependencies("io.elimu.a2d2", "MedicationRequest", "1.1.3");
	}
	
}
