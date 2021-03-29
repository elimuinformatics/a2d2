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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.elimu.a2d2.exception.WorkItemHandlerException;

public class OAuth2WorkItemHandler implements WorkItemHandler {

	private static final Logger LOG = LoggerFactory.getLogger(OAuth2WorkItemHandler.class);
	
	private final Pattern patToken = Pattern.compile(".*\"access_token\"\\s*:\\s*\"([^\"]+)\".*");
	private final Pattern patExpires = Pattern.compile(".*\"expires_in\"\\s*:\\s*([^,]+).*");
	private final Pattern patRefreshToken = Pattern.compile(".*\"refresh_token\"\\s*:\\s*\"([^\"]+)\".*");
	private final Pattern patRefreshExpires = Pattern.compile(".*\"refresh_expires_in\"\\s*:\\s*([^,]+).*");

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String clientId = (String) workItem.getParameter("clientId");
		String scope = (String) workItem.getParameter("scope");
		String username = (String) workItem.getParameter("username");
		String password = (String) workItem.getParameter("password");
		String grantType = (String) workItem.getParameter("grantType");
		String tokenUrl = (String) workItem.getParameter("tokenUrl");
		List<String> missingValues = new ArrayList<>();
		if (clientId == null || "".equals(clientId.trim())) {
			missingValues.add("clientId");
		}
		if (username == null || "".equals(username.trim())) {
			missingValues.add("username");
		}
		if (password == null || "".equals(password.trim())) {
			missingValues.add("password");
		}
		if (scope == null || "".equals(scope.trim())) {
			scope = "offline_access";
		}
		if (grantType == null || "".equals(grantType.trim())) {
			grantType = "password";
		}
		if (tokenUrl == null || "".equals(tokenUrl.trim())) {
			missingValues.add("tokenUrl");
		}
		if (!missingValues.isEmpty()) {
			throw new WorkItemHandlerException("Missing mandatory parameters: " + missingValues);
		}
		String content = "grant_type=" + grantType + "&client_id=" + clientId 
				+ "&scope=" + scope + "&username=" + username 
				+ "&password=" + password;
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
	        Matcher matcher = patToken.matcher(response);
	        if (matcher.matches() && matcher.groupCount() > 0) {
	            String token = matcher.group(1);
	            results.put("token", token);
	        }
	        matcher = patExpires.matcher(response);
	        if (matcher.matches() && matcher.groupCount() > 0) {
	        	String expires = matcher.group(1);
	        	try {
	        		results.put("expiresIn", Long.parseLong(expires));
	        	} catch (Exception e) { 
	        		LOG.warn("Cannot parse expires text '" + expires + "' from OAuth2 response");
	        	}
	        }
		matcher = patRefreshToken.matcher(response);
		if (matcher.matches() && matcher.groupCount() > 0) {
			String refreshToken = matcher.group(1);
			results.put("refreshToken", refreshToken);
		}
		matcher = patRefreshExpires.matcher(response);
		if (matcher.matches() && matcher.groupCount() > 0) {
			String refreshExpires = matcher.group(1);
			try {
				results.put("refreshExpiresIn", Long.parseLong(refreshExpires));
			} catch (Exception e) {
				LOG.warn("Cannot parse refresh_expires text '" + refreshExpires + "' from OAuth2 response");
			}
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
