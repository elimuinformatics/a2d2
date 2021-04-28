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

import io.elimu.a2d2.cdsresponse.entity.TemplateRepository;

public class OAuthUtils {
    
    private static final Logger LOG = LoggerFactory.getLogger(OAuthUtils.class);
    private static final Map<String, Map<String, Object>> CACHE = new HashMap<>();
    private static final Map<String, String> KEYS = new HashMap<>();

    public static void registerPrivateKey(String tokenUrl, String privateKeyContent) {
    	if (!KEYS.containsKey(tokenUrl)) {
    		KEYS.put(tokenUrl, privateKeyContent);
    	}
    }
    
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
    		//TODO handle errors and test that this works once you register something in KEYS
    	}
    	return null;
    }
    
    //TODO invoke this from QueryingServerHelper
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
    
    public static void main(String[] args) {
		String pkcsFileName = "covid-tracker-program_privatekey.pkcs";
		String tokenUrl = "https://apporchard.epic.com/interconnect-aocurprd-oauth/oauth2/token";
		String clientId = "12345-1234123-1234566";
		
		String pk = TemplateRepository.getInstance().getFltMap().get(pkcsFileName);
		OAuthUtils.registerPrivateKey(tokenUrl, pk);
		String jwtToken = OAuthUtils.jwtToken(tokenUrl, clientId);
		
		String body = new BodyBuilder().addToBody("grant_type", "client_credentials").
				addToBody("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer").
				addToBody("client_assertion", jwtToken).build();
		Map<String, Object> retval = OAuthUtils.authenticate(body, tokenUrl, clientId,"");

		System.out.println("accessToken = " + retval.get("access_token"));
	}
}
