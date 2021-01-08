package io.elimu.cdshookapi.service.test;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import io.elimu.a2d2.cdsmodel.Dependency;
import io.elimu.genericapi.service.GenericKieBasedService;
import io.elimu.genericapi.service.GenericService;
import io.elimu.genericapi.service.RunningServices;

@Ignore("SAP-925 ignored until kJAR is updated")
public class CDSServiceTest {

	@BeforeClass
	public static void before() throws Exception {
		System.setProperty("kie.wb.location", "http://kie-wb");
		System.setProperty("kie.wb.user", "kie");
		System.setProperty("kie.wb.pwd", "kie");
		System.setProperty("maven.repo.location", "https://mavenrepo/");
		System.setProperty("maven.repo.user", "mavenuser");
		System.setProperty("maven.repo.pwd", "mavenpass");
	}
	
	@Test
	public void testRecreateDependency() throws Exception {
		Dependency dep = new Dependency("io.elimu.a2d2:pro-recommend:0.0.1-SNAPSHOT");
		RunningServices.getInstance().recreateDependency(dep);
		GenericService service = RunningServices.getInstance().get("pro-recommend");
		Assert.assertNotNull(service);
		Assert.assertTrue(service instanceof GenericKieBasedService);
	}
}
