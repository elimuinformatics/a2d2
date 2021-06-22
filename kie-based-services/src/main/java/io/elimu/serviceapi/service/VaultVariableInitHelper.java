package io.elimu.serviceapi.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.elimu.a2d2.cdsmodel.Dependency;

public class VaultVariableInitHelper extends ProcessVariableInitHelper {

	private static final Logger log = LoggerFactory.getLogger(VaultVariableInitHelper.class);
	public static final Long RESPONSE_EVENT_TIMEOUT = Long.valueOf(System.getProperty("vault.response.timeout", "300000"));
	private static Map<String, ResponseEvent<Map<String, Object>>> CACHE = new HashMap<>();
	
	private CloseableHttpClient client;
	public VaultVariableInitHelper() {
		this.client = HttpClientBuilder.create().build();
	}

	@Override
	public Map<String, Object> initVariables(Dependency dep) {
		Map<String, Object> serviceProperty = super.initVariables(dep);
		try {
			serviceProperty.putAll(getVaultData(dep.getArtifactId()));
		} catch (Exception e) {
			log.error("Error reading Vault properties for " + dep.getArtifactId(), e);
		}
		log.info("Vault Variable initialization done for service :: " + dep.getArtifactId());
		return serviceProperty;
	}
	
	private Map<String, Object> getVaultData(String serviceId) {
		String envName = AppContextUtils.getInstance().getProfileName();
		String vaultUrl = System.getProperty("vault.url");
		String vaultToken = System.getProperty("vault.token");
		String vaultApp = System.getProperty("vault.app");
		if (notEmpty(envName) && notEmpty(vaultUrl) && notEmpty(vaultToken)) {
			Map<String, Object> retval = new HashMap<>();
			retval.putAll(getVaultServerValues(vaultUrl + "/v1/kv/data/" + vaultApp + "%2F" + envName + "%2Fallservices", vaultToken));
			retval.putAll(getVaultServerValues(vaultUrl + "/v1/kv/data/" + vaultApp + "%2F" + envName + "%2F" + serviceId, vaultToken));
			return retval;
		}
		return new HashMap<>();
	}

	private Map<String, Object> getVaultServerValues(String url, String token) {
		ResponseEvent<Map<String, Object>> retval = CACHE.get(url);
		if (retval != null && retval.isNew()) {
			return retval.getValue();
		}
		try {
			HttpGet get = new HttpGet(url);
			get.setHeader("X-Vault-Token", token);
			get.setHeader("Accept", "application/json");
			HttpResponse response = client.execute(get);
			if (response.getStatusLine().getStatusCode() == 200) {
				ObjectMapper mapper = new ObjectMapper();
				retval = new ResponseEvent<Map<String,Object>>(readContent(mapper.readTree(response.getEntity().getContent())));
				CACHE.put(url, retval);
				return retval.getValue();
			} else {
				log.warn("Coulnd't read URL " + url + " because we got HTTP status " + response.getStatusLine() + ". Defautling this config level as empty Map");
				return new HashMap<>();
			}
		} catch (Exception e) {
			log.warn("Coulnd't read URL " + url, e);
			return new HashMap<>();
		}
	}
	
	private static Map<String, Object> readContent(JsonNode node) {
		Map<String, Object> retval = new HashMap<>();
		if (node.has("data")) {
			JsonNode dataNode = node.get("data");
			if (dataNode.has("data")) {
				dataNode = dataNode.get("data");
				for (Iterator<String> items = dataNode.fieldNames(); items.hasNext();) {
					String name = items.next();
					JsonNode item = dataNode.get(name);
					retval.put(name, item.asText());
				}
			}
		}
		return retval;
	}

	private boolean notEmpty(String value) {
		return value != null && !"".equals(value.trim());
	}

	public static void main(String[] args) {
		System.setProperty("vault.url", "https://vault-internal.elimuinformatics.com");
		//System.setProperty("vault.token", "s.gvxxhnUfLUOZ6hHg9mMMnyHZ");
		  System.setProperty("vault.token", "s.T6qAR63bDeNaQOnAIM6Cg6Pq");
		System.setProperty("vault.app", "omnibus-api");
		AppContextUtils.getInstance().testSetEnvironmentName("dev");
		long start = System.currentTimeMillis();
		Map<String, Object> values = new VaultVariableInitHelper().initVariables(new Dependency("io.elimu.a2d2:test:1.0"));
		long length = System.currentTimeMillis() - start;
		System.out.println("Took " + length + " millis to read these properties: " + values);
		
		start = System.currentTimeMillis();
		values = new VaultVariableInitHelper().initVariables(new Dependency("io.elimu.a2d2:test:1.0"));
		length = System.currentTimeMillis() - start;
		System.out.println("Took " + length + " millis to read these properties: " + values);
		
		
	}
}
