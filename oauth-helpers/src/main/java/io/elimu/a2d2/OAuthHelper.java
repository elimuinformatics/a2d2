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

package io.elimu.a2d2;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.kie.api.runtime.rule.RuleContext;

import io.elimu.a2d2.cds.fhir.helper.QueryingServerHelperBase;
import io.elimu.a2d2.genericmodel.NamedDataObject;
import io.elimu.a2d2.oauth.BodyBuilder;
import io.elimu.a2d2.oauth.OAuthUtils;

public class OAuthHelper {

	public static String getOAuthToken(RuleContext context) throws IllegalAccessException {
		return getOAuthToken(context, "fhirScope", "fhirTokenUrl", "fhirClientId", "fhirClientSecret", "fhirPassword", "fhirUsername", "fhirGrantType");
	}
	
	public static String getOAuthToken(RuleContext context, 
			String scopeKey, String tokenUrlKey, String clientIdKey, String clientSecretKey, 
			String passwordKey, String usernameKey, String grantTypeKey) throws IllegalAccessException {
		BodyBuilder builder = new BodyBuilder();
		String scope = extractParameter(context, scopeKey);
		if (scope != null) {
			builder.addToBody("scope", scope);
		}
		
		String tokenUrl = extractParameter(context, tokenUrlKey);
		if (tokenUrl != null) {
			builder.addToBody("token_url", tokenUrl);
		}
		
		String clientId = extractParameter(context, clientIdKey);
		if (clientId != null) {
			builder.addToBody("client_id", clientId);
		}

		String clientSecret = extractParameter(context, clientSecretKey);
		if (clientSecret != null) {
			builder.addToBody("client_secret", clientSecret);
		}

		String password = extractParameter(context, passwordKey + "Decrypt");
		if (password != null) {
			builder.addToBody("password", password);
		} else {
			password = extractParameter(context, passwordKey);
			if (password != null) {
				builder.addToBody("password", password);
			}
		}
		
		String username = extractParameter(context, usernameKey);
		if (username != null) {
			builder.addToBody("username", username);
		}
		
		String grantType = extractParameter(context, grantTypeKey);
		if (grantType != null) {
			builder.addToBody("grant_type", grantType);
		}
		
		String token = null;
		String body = builder.build();
		Map<String, Object> results = OAuthUtils.authenticate(body, tokenUrl, clientId, clientSecret);
		token = (String) results.get("access_token");
		if (token == null) {
			throw new IllegalAccessException((String) results.get("errorMessage"));
		}
		return token;
	}
	
	public static <T> T addOAuthToken(RuleContext context, QueryingServerHelperBase<T, ?> qsh) throws IllegalAccessException {
		return addOAuthToken(context, qsh, "fhirScope", "fhirTokenUrl", "fhirClientId", "fhirClientSecret", "fhirPassword", "fhirUsername", "fhirGrantType");
	}
	
	public static <T> T addOAuthToken(RuleContext context, QueryingServerHelperBase<T, ?> qsh,
			String scopeKey, String tokenUrlKey, String clientIdKey, String clientSecretKey, 
			String passwordKey, String usernameKey, String grantTypeKey) throws IllegalAccessException {
		List<String> params = new LinkedList<>();
		String scope = extractParameter(context, scopeKey);
		if (scope != null) {
			params.add("scope");
			params.add(scope);
		}
		
		String tokenUrl = extractParameter(context, tokenUrlKey);
		if (tokenUrl != null) {
			params.add("token_url");
			params.add(tokenUrl);
		}
		
		String clientId = extractParameter(context, clientIdKey);
		if (clientId != null) {
			params.add("client_id");
			params.add(clientId);
		}

		String clientSecret = extractParameter(context, clientSecretKey);
		if (clientSecret != null) {
			params.add("client_secret");
			params.add(clientSecret);
		}

		String password = extractParameter(context, passwordKey + "Decrypt");
		if (password != null) {
			params.add("password");
			params.add(password);
		} else {
			password = extractParameter(context, passwordKey);
			if (password != null) {
				params.add("password");
				params.add(password);
			}
		}
		
		String username = extractParameter(context, usernameKey);
		if (username != null) {
			params.add("username");
			params.add(username);
		}
		
		String grantType = extractParameter(context, grantTypeKey);
		if (grantType != null) {
			params.add("grant_type");
			params.add(grantType);
		}
		
		return qsh.addAuthentication(QueryingServerHelperBase.OAUTH, params.toArray(new String[params.size()]));
	}

	private static String extractParameter(RuleContext context, String paramName) throws IllegalAccessException {
		Collection<?> objects = context.getKieRuntime().getObjects(new NDOFilter(paramName));
		if (objects.isEmpty()) {
			return null;
		}
		NamedDataObject retval = (NamedDataObject) objects.iterator().next();
		return (String) retval.getValue();
	}
}
