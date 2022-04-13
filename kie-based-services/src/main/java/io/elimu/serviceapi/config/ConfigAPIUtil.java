package io.elimu.serviceapi.config;

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

import io.elimu.a2d2.genericmodel.ServiceRequest;
import io.elimu.a2d2.oauth.BodyBuilder;
import io.elimu.a2d2.oauth.OAuthUtils;

public class ConfigAPIUtil {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigAPIUtil.class);

	private static final String AUTH_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";
	
	private final static Map<String, CachedResult> CACHE = new HashMap<>();
	
	public Map<String, Object> getConfig(ServiceRequest request, String environment, String client, String appName) {
		Map<String, Object> retval = new HashMap<>();
		String configApiUrl = System.getProperty("configApiUrl");
		if (configApiUrl == null) {
			return retval;
		}
		String token = getOrGenerateToken(request);
		if (token == null) {
			return retval;
		}
		String url = configApiUrl + "/" + client + "/" + environment + "/" + appName;
		CachedResult result = CACHE.get(url);
		if (result != null && !result.isOld()) {
			retval.putAll(result.getVariables());
		} else {
			try (CloseableHttpClient httpclient = HttpClientBuilder.create().build()) {
				HttpGet get = new HttpGet(url);
				get.addHeader(AUTH_HEADER, BEARER_PREFIX + token);
				HttpResponse response = httpclient.execute(get);
				if (response.getStatusLine().getStatusCode() == 200) {
					JsonNode data = new ObjectMapper().readTree(response.getEntity().getContent());
					for(Iterator<String> iter = data.fieldNames(); iter.hasNext(); ) {
						String key = iter.next();
						JsonNode node = data.get(key);
						if (node.isBoolean()) {
							retval.put(key, node.asBoolean());
						} else if (node.isInt()) {
							retval.put(key, node.asInt());
						} else if (node.isNumber()) {
							retval.put(key, node.asDouble());
						} else if (node.isTextual()) {
							retval.put(key, node.asText());
						} else if (node.isNull()) {
							retval.put(key, null);
						} else {
							LOG.warn("Invalid value type for JSON property: " + node.getNodeType().name());
						}
					}
				}
				CACHE.put(url, new CachedResult(retval));
			} catch (Exception e) {
				LOG.warn("Error invoking config-api", e);
			}
		}
		LOG.info("ConfigAPIUtil fetched {} variable values", CACHE.get(url).getVariables().size());
		return retval;
	}

	private String getOrGenerateToken(ServiceRequest request) {
		if (request != null && request.getHeader(AUTH_HEADER) != null && request.getHeader(AUTH_HEADER).startsWith(BEARER_PREFIX)) {
			return request.getHeader(AUTH_HEADER).replace(BEARER_PREFIX, "");
		}
		String prefix = System.getProperty("proc.envvar.prefix");
		String clientId = System.getProperty("configApiClientId");
		if (clientId == null) {
			return null;
		}
		String clientSecret = System.getProperty("configApiClientSecret");
		if (clientSecret == null) {
			return null;
		}
		String tokenUrl = System.getProperty(prefix + ".allservices.fhirTokenUrl");
		if (tokenUrl == null) {
			return null;
		}
		String grantType = System.getProperty(prefix + ".allservices.fhirGrantType");
		if (grantType == null) {
			return null;
		}
		String username = System.getProperty(prefix + ".allservices.fhirUsername");
		if (username == null) {
			return null;
		}
		String password = System.getProperty(prefix + ".allservices.fhirPassword");
		if (password == null) {
			return null;
		}
		String scope = System.getProperty(prefix + ".allservices.fhirScope");
		if (scope == null) {
			return null;
		}
		String body = new BodyBuilder().addToBody("token_url", tokenUrl).
				addToBody("client_id", clientId).addToBody("client_secret", clientSecret).
				addToBody("grant_type", grantType).addToBody("scope", scope).
				addToBody("username", username).addToBody("password", password).
				build();
		Map<String, Object> tokenResults = OAuthUtils.authenticate(body, tokenUrl, clientId, clientSecret);
		return (String) tokenResults.get("access_token");
	}
}
