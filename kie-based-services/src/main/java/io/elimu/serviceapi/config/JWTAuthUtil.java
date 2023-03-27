package io.elimu.serviceapi.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
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

	private static final Map<String, JSONObject> CACHE = new HashMap<>();
	
	public static boolean isValidJwt(String jwtToken, String serviceId) {
		try {
			if (jwtToken == null || "NO_CREDS".equalsIgnoreCase(jwtToken)) {
				return false;
			}
			LOGGER.debug("Parsing token " + jwtToken);
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
			//Ensure the jku value, if present, matches the request and validates the signature
			String jku = jwt.getHeaderClaim("jku") == null ? null : jwt.getHeaderClaim("jku").asString();
			List<String> validJku = getConfigPropertyAsString(serviceProperties, "jwtValidJku");
			List<String> presetKeys = getConfigPropertyAsString(serviceProperties, "jwtValidPresetKeys");
			if (presetKeys != null && !presetKeys.isEmpty()) {
				if(!isValidKeyWithJKU(jwt, presetKeys.get(validIss.indexOf(iss)))) {
					return false;
				}
			} else if (jku == null || validJku.isEmpty() || !isValidKeyWithJKU(jwt, presetKeys.get(validIss.indexOf(iss)))) {
				return false;
			}
			//Ensure the key value. If presetn, matches the request and validates the signature
			//Ensure that the tenant value exists in the CDS Service's allowlist of trusted tenants (may not be applicable to all CDS Services).
			String tenant =jwt.getClaim("tenant") == null ? null : jwt.getClaim("tenant").asString();  
			List<String> validTenants = getConfigPropertyAsString(serviceProperties, "jwtValidTenant");
			if (tenant != null && !validTenants.isEmpty() && validTenants.contains(tenant)) {
				return false;
			}
			//Future: Ensure that the jti value doesn't exist in the short-term storage of JWTs previously processed by this CDS Service.
			return true;
		} catch (Exception e) {
			LOGGER.warn("Couldn't parse JWT token: " + e.getMessage(), e);
			return false;
		}
	}
	
	private static boolean isValidKeyWithJKU(DecodedJWT jwt, String presetKey) {
		PublicKey key = null;
		if (presetKey == null || "".equals(presetKey) || "invalid".equalsIgnoreCase(presetKey)) {
			key = getPublicKey(jwt.getHeaderClaim("jku").asString(), jwt.getAlgorithm());
		} else {
			key = buildKey(presetKey);
		}
		JWTVerifier verifier = null;
		if (jwt.getAlgorithm().startsWith("RS")) {
			verifier = JWT.require(Algorithm.RSA256(new SimpleRSAKeyProvider((RSAPublicKey) key))).build();
		} else if (jwt.getAlgorithm().startsWith("ES")) {
			verifier = JWT.require(Algorithm.ECDSA384(new SimpleECDSAKeyProvider((ECPublicKey) key))).build();
		}
		if (verifier == null) {
			LOGGER.warn("Cannot validate algorithm of type " + jwt.getAlgorithm());
			return false;
		}
		try {
			verifier.verify(jwt.getToken());
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private static PublicKey buildKey(String presetKey) {
		try {
			byte[] key = org.apache.commons.codec.binary.Base64.decodeBase64(presetKey);
			ByteArrayInputStream fileInputStream = new ByteArrayInputStream(key);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate)cf.generateCertificate(fileInputStream);
			return cert.getPublicKey();
		} catch (Exception e) {
			LOGGER.warn("Couldnt read preset key", e);
			return null;
		}
	}

	private static PublicKey getPublicKey(String jku, String algorithm) {
		String key = "jku=" + jku + ";alg=" + algorithm; 
		if (CACHE.get(key) == null) {
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
							if (item.has("alg")) {
								String newKey = "jku=" + jku + ";alg=" + item.getString("alg");
								CACHE.put(newKey, item);
							}
						}
					}
				}
				
			} catch (Exception e) {
				LOGGER.warn("Couldn't read JKU located at " + jku, e);
			}
		}
		JSONObject item = CACHE.get(key);
		if (algorithm.startsWith("RS")) {
			byte[] jkuKey = org.apache.commons.codec.binary.Base64.decodeBase64(item.getString("n"));
			byte[] jkuModulus = pad(org.apache.commons.codec.binary.Base64.decodeBase64(item.getString("e")));
			RSAPublicKeySpec spec = new RSAPublicKeySpec(
					new BigInteger(1, jkuKey), 
					new BigInteger(1, jkuModulus)
			);
			try {
				return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
			} catch (Exception e) {
				throw new RuntimeException("Cannot find key generator for RSA", e); 
			}
		} else if (algorithm.startsWith("ES")) {
			byte[] jkuKey = org.apache.commons.codec.binary.Base64.decodeBase64(item.getString("x"));
			byte[] jkuModulus = org.apache.commons.codec.binary.Base64.decodeBase64(item.getString("y"));
			
			try {
				AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC", "SunEC");
				parameters.init(new ECGenParameterSpec("secp384r1"));
				ECParameterSpec ecParameters = parameters.getParameterSpec(ECParameterSpec.class);
				ECPoint ecPoint = new ECPoint(new BigInteger(1, jkuKey), new BigInteger(1, jkuModulus));
				ECPublicKeySpec spec = new ECPublicKeySpec(ecPoint, ecParameters);
				return (ECPublicKey) KeyFactory.getInstance("EC").generatePublic(spec);
			} catch (Exception e) {
				throw new RuntimeException("Cannot find key generator for EC", e); 
			}
		}
		return null;
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
		if (delta > 0) {
			for (int index = 0; index < bs.length; index++) {
				retval[index+delta] = bs[index];
			}
		} else {
			retval = bs;
		}
		return retval;
	}

	private static String getConfigProperty(Properties serviceProperties, String key) {
		String config = serviceProperties.getProperty(key);
		if (config == null) {
			config = serviceProperties.getProperty("proc.var." + key + ".value");
		}
		if (config == null) {
			config = "";
		}
		return config;
	}
	
	private static List<String> getConfigPropertyAsString(Properties serviceProperties, String key) {
		String config = getConfigProperty(serviceProperties, key);
		List<String> valid = new ArrayList<>(Arrays.asList(config.split(",")));
		if (valid.contains("")) {
			valid.remove("");
		}
		return valid;
	}


}
