package io.elimu.genericapi.service;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.process.ProcessInstance;

import io.elimu.serviceapi.service.AppContextUtils;

public class MavenJarResolverTest {
	
	@BeforeClass
	public static void setUp() throws Exception {

		System.setProperty("kie.wb.location", "https://wb.elimuinformatics.com/kie-wb");
		System.setProperty("kie.wb.user", "mariano");
		System.setProperty("kie.wb.pwd", "Winter44");
		AppContextUtils.getInstance().testSetEnvironmentName("dev");
	}

	@Test
	public void testResolveDependencies() throws Exception {
		MavenJarResolver.resolveDependencies("io.elimu.a2d2", "MedicationRequest", "1.1.10");
	}
	
}
