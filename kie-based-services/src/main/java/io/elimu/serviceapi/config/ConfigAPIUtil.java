package io.elimu.serviceapi.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.elimu.a2d2.genericmodel.ServiceRequest;
import io.elimu.a2d2.oauth.BodyBuilder;
import io.elimu.a2d2.oauth.OAuthUtils;

public class ConfigAPIUtil {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigAPIUtil.class);

	private static final String AUTH_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";
	private static final int CONFIG_GET_TIMEOUT = Integer.valueOf(System.getProperty("configApiConnectTimeout", "10000"));
	
	private final static Map<String, CachedResult> CACHE = new HashMap<>();
	
	public Map<String, Object> getConfig(ServiceRequest request, String environment, String client, String appName) throws TimeoutException {
		try {
			LOG.info("Started fetching configuration from Config-api ");
			Map<String, Object> retval = new HashMap<>();
			String configApiUrl = System.getProperty("configApiUrl");
			if (configApiUrl == null) {
				return retval;
			}
			String token = getOrGenerateToken(request);
			if (token == null) {
				return retval;
			}
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(configApiUrl).pathSegment(client, environment,appName);
			URI uri = null;
			try {
				uri = new URI(builder.toUriString());
				LOG.info("Config-api URL builded successfully");
			} catch (URISyntaxException e1) {
			LOG.error ("Error while fetching configuration from Config-api ", e1);
			}
			String url = uri.toString();
			CachedResult result = CACHE.get(url);
			if (result != null && !result.isOld()) {
				LOG.info("Fetching configuration of config-api from CACHE");
				retval.putAll(result.getVariables());
			} else {
				RequestConfig config = RequestConfig.custom()
						.setConnectTimeout(CONFIG_GET_TIMEOUT)
						.setConnectionRequestTimeout(CONFIG_GET_TIMEOUT)
						.setSocketTimeout(CONFIG_GET_TIMEOUT).build();
				try (CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build()) {
					HttpGet get = new HttpGet(url);
					get.addHeader(AUTH_HEADER, BEARER_PREFIX + token);
					LOG.info("Fetching configuration of config-api from SERVER");
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
						else {
							// to do: Check what kind exception might be happens,
							throw new TimeoutException("Timeout occurred when fetching configuration parameters");
						}
						CACHE.put(url, new CachedResult(retval));
					} catch (TimeoutException e) { 
						throw e;
					} catch (Exception e) {
						LOG.warn("Error invoking config-api", e);
					}
				}
			LOG.info("ConfigAPIUtil fetched {} variable values", CACHE.get(url).getVariables().size());
			return retval;
		} catch (TimeoutException e) {
			throw new TimeoutException("Timeout occurred when fetching configuration parameters");
		 }
		catch (Exception e) {
			throw e;
		}
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
