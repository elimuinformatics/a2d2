package io.elimu.serviceapi.config;

import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

public class ConfigAPIUtilTest {

	@Test
	public void testConfigApiCall() throws Exception {
		ConfigAPIUtil util = new ConfigAPIUtil();
		Map<String, Object> values = util.getConfig("dev", "default", "pphtn-submit");
		Assert.assertNotNull(values);
		Assert.assertTrue(values.isEmpty());

		Properties jvmprops = System.getProperties();
		if (!jvmprops.containsKey("testConfigApiUrl") || 
				!jvmprops.containsKey("testConfigApiClientId") ||
				!jvmprops.containsKey("testConfigApiClientSecret") ||
				!jvmprops.containsKey("tokenUrl") ||
				!jvmprops.containsKey("grantType") ||
				!jvmprops.containsKey("username") ||
				!jvmprops.containsKey("password") ||
				!jvmprops.containsKey("scope")) {
			return;
		}
		System.setProperty("proc.envvar.prefix", "testenv");
		System.setProperty("configApiUrl", System.getProperty("testConfigApiUrl"));
		System.setProperty("configApiClientId", System.getProperty("testConfigApiClientId"));
		System.setProperty("configApiClientSecret", System.getProperty("testConfigApiClientSecret"));
		System.setProperty("testenv.allservices.fhirTokenUrl", System.getProperty("tokenUrl"));
		System.setProperty("testenv.allservices.fhirGrantType", System.getProperty("grantType"));
		System.setProperty("testenv.allservices.fhirUsername", System.getProperty("username"));
		System.setProperty("testenv.allservices.fhirPassword", System.getProperty("password"));
		System.setProperty("testenv.allservices.fhirScope", System.getProperty("scope"));
		
		values = util.getConfig("dev", "default", "pphtn-submit");
		Assert.assertNotNull(values);
		Assert.assertFalse(values.isEmpty());
		Assert.assertTrue(values.containsKey("fhirBaseUrl"));
		Assert.assertNotNull(values.get("fhirBaseUrl"));
	}
}
