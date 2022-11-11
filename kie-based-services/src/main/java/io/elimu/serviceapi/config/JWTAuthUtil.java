package io.elimu.serviceapi.config;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.elimu.genericapi.service.GenericKieBasedService;
import io.elimu.genericapi.service.RunningServices;

public class JWTAuthUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(JWTAuthUtil.class);

	private static final Map<String, byte[]> CACHE_N = new HashMap<>();
	private static final Map<String, byte[]> CACHE_E = new HashMap<>();
	
	public static boolean isValidJwt(String jwtToken, String serviceId) {
		try {
			DecodedJWT jwt = JWT.decode(jwtToken.replace("Bearer", "").trim());
			GenericKieBasedService service = serviceId == null ? null : (GenericKieBasedService) RunningServices.getInstance().get(serviceId);
			Properties serviceProperties = service == null ? new Properties() : service.getConfig();
			//Ensure that the iss value exists in the CDS Service's allowlist of trusted CDS Clients.
			String iss = jwt.getIssuer();
			List<String> validIss = getConfigPropertyAsString(serviceProperties, "jwtValidIss");
			if (validIss.isEmpty() || !validIss.contains(iss)) { 
				return false; 
			}
			//Ensure that the aud value matches the CDS Service endpoint currently processing the request.
			List<String> aud = jwt.getAudience();
			List<String> validAud = getConfigPropertyAsString(serviceProperties, "jwtValidAud");
			if (aud == null || validAud.isEmpty() || !aud.stream().anyMatch(a -> validAud.contains(a))) {
				return false;
			}
			//Ensure that the exp value is not before the current date/time.
//			if (jwt.getExpiresAt().before(new Date())) {
//				return false;
//			}
			//Ensure the jku value, if present, matches the request and validates the signature
			String jku = jwt.getHeaderClaim("jku") == null ? null : jwt.getHeaderClaim("jku").asString();
			List<String> validJku = getConfigPropertyAsString(serviceProperties, "jwtValidJku");
			if (jku == null || validJku.isEmpty() || !isValidKey(jwt)) {
				return false;
			}
			//Ensure that the tenant value exists in the CDS Service's allowlist of trusted tenants (may not be applicable to all CDS Services).
			String tenant =jwt.getClaim("tenant") == null ? null : jwt.getClaim("tenant").asString();  
			List<String> validTenants = getConfigPropertyAsString(serviceProperties, "jwtValidTenant");
			if (tenant != null && !validTenants.isEmpty() && validTenants.contains(tenant)) {
				return false;
			}
			//Future: Ensure that the jti value doesn't exist in the short-term storage of JWTs previously processed by this CDS Service.
			return true;
		} catch (Exception e) {
			LOGGER.warn("Couldn't parse JWT token: " + e.getMessage());
			return false;
		}
	}
	
	private static boolean isValidKey(DecodedJWT jwt) {
		RSAPublicKey key = getPublicKey(jwt.getHeaderClaim("jku").asString(), jwt.getAlgorithm());
		JWTVerifier verifier = JWT.require(Algorithm.RSA256(new SimpleRSAKeyProvider(key))).build();
		try {
			verifier.verify(jwt.getToken());
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static RSAPublicKey getPublicKey(String jku, String algorithm) {
		String key = "jku=" + jku + ";alg=" + algorithm; 
		if (CACHE_N.get(key) == null) {
			try {
				HttpURLConnection conn = (HttpURLConnection) new URL(jku).openConnection();
				conn.setRequestMethod("GET");
				conn.setDoOutput(true);
				conn.connect();
				try (InputStream input = conn.getInputStream()) {
					byte[] data = input.readAllBytes();
					JSONObject obj = new JSONObject(new String(data, StandardCharsets.UTF_8));
					if (obj != null && obj.has("keys")) {
						for (Iterator<Object> iter = obj.getJSONArray("keys").iterator(); iter.hasNext(); ) {
							JSONObject item = (JSONObject) iter.next();
							if (item.has("n") && item.has("alg") && item.has("e")) {
								String newKey = "jku=" + jku + ";alg=" + item.getString("alg");
								CACHE_N.put(newKey, org.apache.commons.codec.binary.Base64.decodeBase64(item.getString("n")));
								CACHE_E.put(newKey, org.apache.commons.codec.binary.Base64.decodeBase64(item.getString("e")));
							}
						}
					}
				}
				
			} catch (Exception e) {
				LOGGER.warn("Couldn't read JKU located at " + jku, e);
			}
		}
		byte[] jkuKey = CACHE_N.get(key);
		byte[] jkuModulus = pad(CACHE_E.get(key));
		RSAPublicKeySpec spec = new RSAPublicKeySpec(
				new BigInteger(1, jkuKey), 
				new BigInteger(1, jkuModulus)
		);
		try {
			return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
		} catch (Exception e) {
			throw new RuntimeException("Cannot find key generator for RSA", e); 
		}

	}

	private static byte[] pad(byte[] bs) {
		if (bs.length == 8) {
			return bs;
		}
		byte[] retval = new byte[8];
		for (int index = 0; index < retval.length; index++) {
			retval[index] = 0;
		}
		int delta = retval.length - bs.length;
		for (int index = 0; index < bs.length; index++) {
			retval[index+delta] = bs[index];
		}
		return retval;
	}

	private static List<String> getConfigPropertyAsString(Properties serviceProperties, String key) {
		String config = serviceProperties.getProperty(key);
		if (config == null) {
			config = serviceProperties.getProperty("proc.var." + key + ".value");
		}
		if (config == null) {
			config = "";
		}
		List<String> valid = new ArrayList<>(Arrays.asList(config.split(",")));
		if (valid.contains("")) {
			valid.remove("");
		}
		return valid;
	}


}
