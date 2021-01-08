package io.elimu.serviceapi.service;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import io.elimu.a2d2.cdsmodel.Dependency;
import io.elimu.a2d2.genericmodel.ServiceRequest;
import io.elimu.a2d2.genericmodel.ServiceResponse;
import io.elimu.a2d2.helpers.PasswordDecryptUtil;
import io.elimu.genericapi.service.GenericKieBasedService;
import io.elimu.genericapi.service.RunningServices;

public class EnvironmentVarsTest {

	private String findValue;
	
	@Before
	public void setup() throws Exception {
		System.setProperty("kie.wb.location", "https://kie-wb");
		System.setProperty("kie.wb.user", "kie");
		System.setProperty("kie.wb.pwd", "kie");
		System.setProperty("proc.envvar.prefix", "var.a2d2");
		findValue = PasswordDecryptUtil.encrypt("defaultSearchString");
		updateEnv("var.a2d2.preloaded-vars-process.findValue", findValue);
		Assert.assertEquals(findValue, System.getenv("var.a2d2.preloaded-vars-process.findValue"));
	}
	
	@SuppressWarnings({ "unchecked" })
	private void updateEnv(String name, String val) throws ReflectiveOperationException {
		//this dirty hack here is to set the environment variable just for the test
		//without actually depending on the OS or actually modifying an environment variable
		//in the test environment
		Map<String, String> env = System.getenv();
		Field field = env.getClass().getDeclaredField("m");
		field.setAccessible(true);
		((Map<String, String>) field.get(env)).put(name, val);
	}
	
	@Ignore("Broken, fix later")
	@Test
	public void testInitVariables() throws Exception {
		RunningServices.getInstance().downloadDependency(new Dependency("io.elimu.a2d2.examples:preloaded-vars-process:1.0.0"));
		GenericKieBasedService service = new GenericKieBasedService("io.elimu.a2d2.examples:preloaded-vars-process:1.0.0", "elimu");
		Assert.assertNotNull(service);
		ServiceRequest request1 = new ServiceRequest();
		request1.setMethod("GET");
		request1.addParamValue("stringToSearch", "Something without findValue");
		ServiceResponse response1 = service.execute(request1);
		Assert.assertNotNull(response1);
		Assert.assertEquals(200, response1.getResponseCode().intValue());
		Assert.assertEquals("The find value was NOT present on the search string 'Something without findValue'", response1.getBody());
		
		ServiceRequest request2 = new ServiceRequest();
		request2.setMethod("GET");
		request2.addParamValue("stringToSearch", "Something with defaultSearchString findValue");
		ServiceResponse response2 = service.execute(request2);
		Assert.assertNotNull(response2);
		Assert.assertEquals(200, response2.getResponseCode().intValue());
		Assert.assertEquals("The find value was present on the search string 'Something with defaultSearchString findValue'", response2.getBody());
	}
}

