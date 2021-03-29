package io.elimu.a2d2.corewih;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang3.StringUtils;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.elimu.a2d2.exception.WorkItemHandlerException;

public class OAuth2WorkItemHandler implements WorkItemHandler {

	private static final Logger LOG = LoggerFactory.getLogger(OAuth2WorkItemHandler.class);
	
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String clientId = (String) workItem.getParameter("clientId");
		String scope = (String) workItem.getParameter("scope");
		String username = (String) workItem.getParameter("username");
		String password = (String) workItem.getParameter("password");
		String grantType = (String) workItem.getParameter("grantType");
		String tokenUrl = (String) workItem.getParameter("tokenUrl");
		String refreshToken = (String) workItem.getParameter("refreshToken");
		List<String> missingValues = new ArrayList<>();
		if (StringUtils.isEmpty(clientId)) {
			missingValues.add("clientId");
		}
		if (StringUtils.isEmpty(refreshToken)) {
			if (StringUtils.isEmpty(username)) {
				missingValues.add("username");
			}
			if (StringUtils.isEmpty(password)) {
				missingValues.add("password");
			}
		}
		if (StringUtils.isEmpty(scope)) {
			scope = "offline_access";
		}
		if (StringUtils.isEmpty(grantType)) {
			grantType = StringUtils.isEmpty(refreshToken) ? "password" : "refresh_token";
		} else if (!StringUtils.isEmpty(refreshToken)) {
			grantType = "refresh_token";
		}
		if (StringUtils.isEmpty(tokenUrl)) {
			missingValues.add("tokenUrl");
		}
		if (!missingValues.isEmpty()) {
			throw new WorkItemHandlerException("Missing mandatory parameters: " + missingValues);
		}
		String content = "grant_type=" + grantType + "&client_id=" + clientId 
				+ "&scope=" + scope;
		if (StringUtils.isEmpty(refreshToken)) {
			content += "&username=" + username + "&password=" + password;
		} else {
			content += "&refresh_token=" + refreshToken;
		}
	    BufferedReader reader = null; 
	    HttpsURLConnection connection = null;
	    Map<String, Object> results = workItem.getResults();
	    try {
	        URL url = new URL(tokenUrl);
	        connection = (HttpsURLConnection) url.openConnection();
	        connection.setRequestMethod("POST");
	        connection.setDoOutput(true);
	        connection.setRequestProperty("Accept", "*/*");
	        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        connection.setRequestProperty("Authorization", 
	        		"Basic " + Base64.getEncoder().encodeToString((clientId + ":").getBytes()));
	        PrintStream os = new PrintStream(connection.getOutputStream());
	        os.print(content);
	        os.close();
	        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        String line = null;
	        StringWriter out = new StringWriter(connection.getContentLength() > 0 ? connection.getContentLength() : 2048);
	        while ((line = reader.readLine()) != null) {
	            out.append(line);
	        }
	        String response = out.toString();
	        Gson gson = new Gson();
	        Map<?, ?> output = (Map<?, ?>) gson.fromJson(response, Map.class);
	        results.put("accessToken", output.get("access_token"));
	        results.put("refreshToken", output.get("refresh_token"));
	        try {
	        	results.put("expiresIn", ((Number) output.get("expires_in")).longValue());
	        } catch (Exception e) {
	        	LOG.warn("Cannot parse expires_in value '" + output.get("expires_in") + "' to a Long");
	        }
	        try {
	        	results.put("refreshExpiresIn", ((Number) output.get("refresh_expires_in")).longValue());
	        } catch (Exception e) {
	        	LOG.warn("Cannot parse refresh_expires_in value '" + output.get("refresh_expires_in") + "' to a Long");
	        }
	    } catch (Exception e) {
	    	results.put("error", e);
	    	results.put("errorMessage", e.getMessage());
	    } finally {
	        if (reader != null) {
	            try {
	                reader.close();
	            } catch (IOException e) {
	            }
	        }
	        connection.disconnect();
	        manager.completeWorkItem(workItem.getId(), results);
	    }
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// do nothing
	}
}
