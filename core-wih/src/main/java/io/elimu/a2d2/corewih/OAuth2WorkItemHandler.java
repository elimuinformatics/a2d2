package io.elimu.a2d2.corewih;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.elimu.a2d2.exception.WorkItemHandlerException;
import io.elimu.a2d2.oauth.BodyBuilder;
import io.elimu.a2d2.oauth.OAuthUtils;

public class OAuth2WorkItemHandler implements WorkItemHandler {

	private static final Logger LOG = LoggerFactory.getLogger(OAuth2WorkItemHandler.class);
	
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String clientId = (String) workItem.getParameter("clientId");		
		String clientSecret = (String) workItem.getParameter("clientSecret");
		String scope = (String) workItem.getParameter("scope");
		String username = (String) workItem.getParameter("username");
		String password = (String) workItem.getParameter("password");
		String grantType = (String) workItem.getParameter("grantType");
		String tokenUrl = (String) workItem.getParameter("tokenUrl");
		String refreshToken = (String) workItem.getParameter("refreshToken");
		String redirectUri = (String) workItem.getParameter("redirectUri");
		String code = (String) workItem.getParameter("code");
		List<String> missingValues = new ArrayList<>();
		if (StringUtils.isEmpty(tokenUrl)) {
			missingValues.add("tokenUrl");
		}
		if (StringUtils.isEmpty(scope)) {
			scope = "offline_access";
		}
		if (StringUtils.isEmpty(clientId)) {
			missingValues.add("clientId");
		}
		if (StringUtils.isEmpty(grantType)) {
			missingValues.add("grantType");
		} else {
			switch (grantType) {
				case "password":
					if (StringUtils.isEmpty(password)) {
						missingValues.add("password");
					}
					if (StringUtils.isEmpty(username)) {
						missingValues.add("username");
					}
					break;
				case "refresh_token":
					if (StringUtils.isEmpty(refreshToken)) {
						missingValues.add("refreshToken");
					}
					break;
				case "client_credentials":
					break;
				case "authorization_code":
					if (StringUtils.isEmpty(code)) {
						missingValues.add("code");
					}
					break;
			}
		}
		if (!missingValues.isEmpty()) {
			throw new WorkItemHandlerException("Missing mandatory parameters: " + missingValues);
		}

		String content = new BodyBuilder().
			addToBody("grant_type", grantType).
			addToBody("client_id", clientId).
			addToBody("client_secret", clientSecret).
			addToBody("scope", scope).
			addToBody("username", username).
			addToBody("password", password).
			addToBody("redirect_uri", redirectUri).
			addToBody("code", code).
			addToBody("refresh_token", refreshToken).
			build();
		
		//Extracted logic so we can use it also in QueryingServerHelper
		Map<String, Object> results = workItem.getResults();
		Map<String, Object> output = OAuthUtils.authenticate(content, tokenUrl, clientId, clientSecret);
        	results.put("accessToken", output.get("access_token"));
	        results.put("refreshToken", output.get("refresh_token"));
        	results.put("expiresIn", output.get("expires_in"));
	    	results.put("refreshExpiresIn", output.get("refresh_expires_in"));
    		results.put("error", output.get("error"));
	    	results.put("errorMessage", output.get("errorMessage"));
		results.put("scope", output.get("scope"));
		results.put("tokenType", output.get("token_type"));
		results.put("sessionState", output.get("session_type"));
        	manager.completeWorkItem(workItem.getId(), results);
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// do nothing
	}
}
