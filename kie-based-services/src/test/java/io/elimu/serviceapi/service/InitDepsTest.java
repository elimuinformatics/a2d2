package io.elimu.serviceapi.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import io.elimu.genericapi.service.GenericKieBasedService;
import io.elimu.genericapi.service.RunningServices;

public class InitDepsTest {
	
		@Before
		public void setUp() throws Exception {
			System.setProperty("kie.wb.location", "https://kie-wb/");
			System.setProperty("kie.wb.user", "kie");
			System.setProperty("kie.wb.pass", "kie");
		}
	
		@Ignore("Broken, fix later")
		@Test
		public void testStartMME() throws Exception {
			GenericKieBasedService service = new GenericKieBasedService("io.elimu.a2d2:MedicationRequest:1.0.8", "");
			RunningServices.getInstance().register(service, false);
			long start = System.currentTimeMillis();
			String timestamp = service.getJarTimestamp();
			String depsTimestamps = service.getDependenciesTimestamp();
			long delay1 = System.currentTimeMillis() - start;
			Assert.assertNotNull(timestamp);
			Assert.assertNotNull(depsTimestamps);
			System.out.println("JAR timestamp = " + timestamp);
			System.out.println("Deps JAR timestamp = " + depsTimestamps);
			System.out.println("first delay = " + delay1);
			start = System.currentTimeMillis();
			service.getJarTimestamp();
			service.getDependenciesTimestamp();
			long delay2 = System.currentTimeMillis() - start;
			System.out.println("second delay = " + delay2);
		}
}
