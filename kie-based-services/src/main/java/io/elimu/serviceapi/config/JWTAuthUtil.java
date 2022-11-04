package io.elimu.serviceapi.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.elimu.genericapi.service.GenericKieBasedService;
import io.elimu.genericapi.service.RunningServices;

public class JWTAuthUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(JWTAuthUtil.class);
	
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
			if (jwt.getExpiresAt().before(new Date())) {
				return false;
			}
			//Ensure that the tenant value exists in the CDS Service's allowlist of trusted tenants (may not be applicable to all CDS Services).
			String tenant =jwt.getClaim("tenant") == null ? null : jwt.getClaim("tenant").asString();  
			List<String> validTenants = getConfigPropertyAsString(serviceProperties, "jwtValidTenant");
			if (tenant != null && !validTenants.isEmpty() && validTenants.contains(tenant)) {
				return false;
			}
			//Future: Ensure that the JWT signature matches the public key on record with the CDS Service. See additional notes below.
			//Future: Ensure that the jti value doesn't exist in the short-term storage of JWTs previously processed by this CDS Service.
			return true;
		} catch (Exception e) {
			LOGGER.warn("Couldn't parse JWT token: " + e.getMessage());
			return false;
		}
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
