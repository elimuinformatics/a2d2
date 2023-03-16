package io.elimu.serviceapi.config;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import io.elimu.a2d2.cdsmodel.Dependency;
import io.elimu.a2d2.genericmodel.ServiceRequest;
import io.elimu.serviceapi.service.AppContextUtils;
import io.elimu.serviceapi.service.ProcessVariableInitHelper;

public class ConfigAPIProcessVariableInitHelper extends ProcessVariableInitHelper {

	public Map<String, Object> initVariables(ServiceRequest request, String client, Dependency dep, Properties config) throws TimeoutException{
		Map<String, Object> serviceProperty = super.initVariables(dep);
		String env = System.getProperty("configApiEnv");
		String appName = config.getProperty("kie.project.appname");
		if (appName == null || "".equals(appName.trim())) {
			appName = dep.getArtifactId();
		}
		try {
			serviceProperty.putAll(new ConfigAPIUtil().getConfig(request, env, client, appName));
		} catch (TimeoutException e) {
			throw new TimeoutException("Timed out");
		}
		serviceProperty.put("configApiAppName", appName);
		serviceProperty.put("configApiClient", client);
		return serviceProperty;
	}
}
