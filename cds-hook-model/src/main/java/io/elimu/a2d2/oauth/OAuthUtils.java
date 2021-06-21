// Copyright 2018-2021 Elimu Informatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.elimu.a2d2.oauth;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.digest.Md5Crypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

/**
 * Utility class to do OAuth authentication. You will need to do pass all the params, but
 * this will do the authentication for you based on OAuth.<br>
 * 
 * You will need to have a series of parameters to use this utility internally:<br>
 * <ul>
 *   <li><strong>clientId</strong>: Usually the application name (a2d2-api, omnibus-api, fhir4-api, etc)</li>
 *   <li><strong>clientSecret</strong>: Usually just a blank string</li>
 *   <li><strong>tokenUrl</strong>: The URL to invoke to get a token, internally it is https://auth-internal.elimuinformatics.com/auth/realms/product/protocol/openid-connect/token</li>
 *   <li><strong>user</strong>: The service username provided for the application or service. For testing purposes it can be your auth-internal account user</li>
 *   <li><strong>password</strong>: The service password provided for the application or service. For testing purposes it can be your auth-internal account password</li>
 *   <li><strong>scope</strong>: The scope, which for now is "offline_access", but will later on be a parameterized component based on FHIR entities, and possibly even Patient IDs</li>
 * </ul><br>
 * Based on them, and assuming the variable names match something in your process or rule, the following script should be sufficient to get a Bearer token:<br>
 * <code>
 * String token = null;<br>
 * String body = new io.elimu.a2d2.oauth.BodyBuilder().<br>
 * 			addToBody("token_url", tokenUrl).<br>
 * 			addToBody("client_id", clientId).<br>
 * 			addToBody("client_secret", clientSecret).<br>
 * 			addToBody("grant_type", "password").<br>
 * 			addToBody("username", user).<br>
 * 			addToBody("password", pass).<br>
 * 			addToBody("scope", scope).build();<br>
 * java.util.Map results = io.elimu.a2d2.oauth.OAuthUtils.authenticate(body, tokenUrl, clientId, clientSecret);<br>
 * token = (String) results.get("access_token");<br>
 * if (token == null) {<br>
 * 		org.slf4j.LoggerFactory.getLogger("io.elimu.a2d2.oauthcheck").warn("Couldn't get access token: " + results.get("errorMessage"));<br>
 * } <br>
 * </code>
 * At the end of the previous code, if you have the variable "token" populated, you have successfully obtained an OAuth token. Otherwise, a warning message will be logged.<br>
 * <br>
 * Some other extension ways of using this have been added, but not yet tested, which would also work for OAuth against AppOrchard. Keeping it here to remove test code for it
 * This sample depends on three variables:<br>
 * <ul>
 *   <li><strong>pkcsFileName</strong>: Similarly to how FTL Transform task defines "templateFileName", this would be a path in the service wher to find a pkcs file, with the private key for AppOrchard</li>
 *   <li><strong>tokenUrl</strong>: The token URL for your AppOrchard application, something in the vecinity of https://your-service-apporchard.epic.com/your-service-oauth/oauth2/token</li>
 *   <li><strong>clientId</strong>: The Client ID provided by AppOrchard when you register the service and private key</li>
 * </ul>
 * Based on those variables, assuming the variable names match, the following script should be sufficient to bet a token from AppOrchard, although this is yet to be tested:<br>
 * <code>
 * String token = null;<br>
 * String pk = io.elimu.a2d2.cdsresponse.entity.TemplateRepository.getInstance().getFltMap().get(pkcsFileName);<br>
 * io.elimu.a2d2.oauth.OAuthUtils.registerPrivateKey(tokenUrl, pk);<br>
 * String jwtToken = io.elimu.a2d2.oauth.OAuthUtils.jwtToken(tokenUrl, clientId);<br>
 * String body = new io.elimu.a2d2.oauth.BodyBuilder().addToBody("grant_type", "client_credentials").<br>
 * 		addToBody("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer").<br>
 * 		addToBody("client_assertion", jwtToken).build();<br>
 * java.util.Map results = io.elimu.a2d2.oauth.OAuthUtils.authenticate(body, tokenUrl, clientId,"");<br>
 * token = (String) results.get("access_token");<br>
 * if (token == null) {<br>
 * 		org.slf4j.LoggerFactory.getLogger("io.elimu.a2d2.oauthcheck").warn("Couldn't get access token: " + results.get("errorMessage"));<br>
 * } <br>
 * </code>
 */
public class OAuthUtils {
    
    private static final Logger LOG = LoggerFactory.getLogger(OAuthUtils.class);
    private static final Map<String, Map<String, Object>> CACHE = new HashMap<>();
    private static final Map<String, String> KEYS = new HashMap<>();

    /**
     * Registers a pkcs file content, that can be used to generate a GWT token.
     * @param tokenUrl the token URL associated to the private key
     * @param privateKeyContent the private key content as a string
     */
    public static void registerPrivateKey(String tokenUrl, String privateKeyContent) {
    	if (!KEYS.containsKey(tokenUrl)) {
    		KEYS.put(tokenUrl, privateKeyContent);
    	}
    }

    /**
     * Creates a JWT token following AppOrchard's structure.
     * @param tokenUrl the token URL to use in the token and to pick a previously registered private key (see @{@link #registerPrivateKey(String, String)}
     * @param clientId the client ID provided by the auth server.
     * @return the generated JWT token as a string
     */
    public static String jwtToken(String tokenUrl, String clientId) {
    	try {
    		String header = "{\"alg\":\"RS384\",\"typ\":\"JWT\"}";
    		String payload = "{  \"iss\": \"" + clientId + "\",  \"sub\": \"" + clientId + "\",  \"aud\": \"" 
    				+ tokenUrl + "\",  \"jti\": \" "+ UUID.randomUUID().toString() + "\",  \"exp\": " 
    				+ Double.valueOf((System.currentTimeMillis() + 300_000)/1000).longValue() +"}";
    		String signatureUnsigned = Base64.getUrlEncoder().encodeToString(header.getBytes()) + "." + 
    				Base64.getUrlEncoder().encodeToString(payload.getBytes());
    		
    		String key = KEYS.get(tokenUrl);
    		if (key == null) {
    			throw new IllegalArgumentException("No registered key");
    		}
    		
    		String privateKeyPEM = key
    			      .replace("-----BEGIN PRIVATE KEY-----", "")
    			      .replaceAll(System.lineSeparator(), "")
    			      .replace("-----END PRIVATE KEY-----", "");
    		byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
    		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
    		PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    		Signature instance = Signature.getInstance("SHA1withRSA");
    		instance.initSign(privateKey);
    		instance.update(signatureUnsigned.getBytes());
    		byte[] signature = instance.sign();

    		String jwtToken = Base64.getUrlEncoder().encodeToString(header.getBytes()) + '.' + 
    				Base64.getUrlEncoder().encodeToString(payload.getBytes()) + '.' + 
    				Base64.getUrlEncoder().encodeToString(signature);
    		return jwtToken;
    	} catch (Exception e) {
    		LOG.warn("Couldn't create JWT token", e);
    	}
    	return null;
    }
    
    /**
     * Authenticates using OAuth endpoints. It will store the access token generated, as well as the expires information, so that 
     * future calls that use the same body, tokenUrl, clientId and clientSecret will be able to use the same token again, or the 
     * refresh token to request a new token if necessary.
     * 
     * @param body the body with all the different parameters needed by OAuth, depending on grant_type provided in body as well.
     * @param tokenUrl The token URL to where we want to send the OAuth request.
     * @param clientId a client ID 
     * @param clientSecret a client secret (usually blank)
     * @return A map with the JSON results, including if successfull an "access_token" key with the Bearer token to use.
     */
    public static Map<String, Object> authenticate(String body, String tokenUrl, String clientId, String clientSecret) {
        String keyUnhashed = "url=" + tokenUrl + "&" + body + "&client_id=" + clientId + "&client_secret=" + clientSecret;
        String key = Md5Crypt.md5Crypt(keyUnhashed.getBytes());
        Map<String, Object> retval = CACHE.get(key);
        if (retval != null) {
            Long expirationTimeMillis = (Long) retval.get("_timeutil_expiration_time_millis");
            if (expirationTimeMillis != null && expirationTimeMillis.longValue() < System.currentTimeMillis()) {
                return retval;
            }
        }
        retval = _authenticate(body, tokenUrl, clientId, clientSecret);
        if (retval.containsKey("expires_in")) {
            Long expirationTime = ((Long) retval.get("expires_in")).longValue() * 1000 + System.currentTimeMillis();
            retval.put("_timeutil_expiration_time_millis", expirationTime);
            CACHE.put(key, retval);
        }
        return retval;
    }

    private static Map<String, Object> _authenticate(String body, String tokenUrl, String clientId, String clientSecret) {
        HttpsURLConnection connection = null;
        Map<String, Object> results = new HashMap<>();
        try {
            URL url = new URL(tokenUrl);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Authorization", 
                    "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + (clientSecret == null ? "" : clientSecret)).getBytes()));
            PrintStream os = new PrintStream(connection.getOutputStream());
            os.print(body);
            os.close();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))){
                String line = null;
                StringWriter out = new StringWriter(connection.getContentLength() > 0 ? connection.getContentLength() : 2048);
                while ((line = reader.readLine()) != null) {
                    out.append(line);
                }
                String response = out.toString();
                results = new Gson().fromJson(response, new TypeToken<Map<String, Object>>() {
                    private static final long serialVersionUID = 1L; 
                }.getType());
                longify(results, "expires_in");
                longify(results, "refresh_expires_in");
            } catch (Exception e2) {
                throw e2;
            }
        } catch (Exception e) {
            results.put("error", e);
            results.put("errorMessage", e.getMessage());
        } finally {
            if (connection != null) {
            	connection.disconnect();
            }
        }
        return results;
    }

    private static void longify(Map<String, Object> results, String key) {
        if (results.containsKey(key)) {
            try {
                Object val = results.get(key);
                if (val instanceof Number) {
                    results.put(key, ((Number)val).longValue());
                } else {
                    String value = String.valueOf(val);
                    results.put(key, Long.parseLong(value));
                }
            } catch (Exception e) {
                LOG.warn("Couldn't parse value '" + results.get(key) + "' from key '" + key + "' as long");
            }
        }
    }
}
