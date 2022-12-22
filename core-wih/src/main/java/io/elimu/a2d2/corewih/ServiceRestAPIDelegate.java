// Copyright 2018-2020 Elimu Informatics
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

package io.elimu.a2d2.corewih;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import io.elimu.a2d2.exception.WorkItemHandlerException;
import io.elimu.a2d2.genericmodel.ClientRequest;
import io.elimu.a2d2.genericmodel.ServiceResponse;

public class ServiceRestAPIDelegate implements WorkItemHandler {

	private static final String CLIENT_REQUEST = "clientRequest";
	private static final String HEADER_VALUE_PARAM = "header_value_";
	private static final String BODY_VALUE_PARAM = "body_value_";
	private static final String PARAM_VALUE_PARAM = "param_value_";
	private static final String RETRIES_PARAM = "retries";
	private static final String SERVICE_RESPONSE = "serviceResponse";
	private static final String STRICT_REDIRECT_PARAM = "strictRedirect";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {

		try {
			Object strictRedirect = workItem.getParameter(STRICT_REDIRECT_PARAM);
			boolean lax = strictRedirect == null || !"true".equalsIgnoreCase(strictRedirect.toString());
			Map<String, Object> workItemResult = workItem.getResults();
			ClientHttpRequestFactory  requestFactory  =
				new HttpComponentsClientHttpRequestFactory(HttpClients.custom()
						.setRedirectStrategy(lax ? new LaxRedirectStrategy() : new DefaultRedirectStrategy())
				        .setDefaultRequestConfig(RequestConfig.custom()
				                .setCookieSpec(CookieSpecs.STANDARD).build())
				        .build());

		    RestTemplate restTemplate = new RestTemplate(requestFactory);
			restTemplate.setErrorHandler(new RestManageErrorHandler());
			String url = null;
			String userName = null;
			String password  = null;
			String token = null;
			HttpHeaders headers = null;
			Object body = null;

			ClientRequest clientRequest = (ClientRequest) workItem.getParameter(CLIENT_REQUEST);
			HttpMethod method = getMethod(workItem);

			int retries = Integer.valueOf(System.getProperty("http.invoke.retries", "3"));
			int delay = Integer.valueOf(System.getProperty("http.invoke.retrydelay", "5000"));
			String sRetries = String.valueOf(workItem.getParameter(RETRIES_PARAM));
			if (sRetries != null && !"null".equals(sRetries)) {
				retries = Integer.valueOf(sRetries);
			}
			
			if(method == null) {
				logger.debug("method is null");
				throw new WorkItemHandlerException("Service Request method is null");
			}

			if(clientRequest == null) {
				url = (String)workItem.getParameter("url");
				userName  = (String) workItem.getParameter("auth_basic_username");
				password  = (String) workItem.getParameter("auth_basic_password");
				token = (String) workItem.getParameter("auth_bearertoken");
				headers = buildHeader(workItem);
				body = buildBody(workItem, headers);
			}
			else {
				url = clientRequest.getUrl();
				userName = clientRequest.getBasicUserName();
				password = clientRequest.getBasicPassword();
				token = clientRequest.getAuthToken();
				body = clientRequest.getBody();
				headers = buildHeaderFromClientRequest(clientRequest.getHeaders());
			}

			String parameter = buildParams(workItem);

			if(userName != null && !userName.isEmpty()) {
				if(password != null  && !password.isEmpty()) {
					String authToken = userName + ":" + password;
					String encodedAuth = "Basic " + Base64.getEncoder().encodeToString(authToken.getBytes());
					headers.add("Authorization", encodedAuth);
					logger.debug("Basic authentication  for user " + userName);
				}
				else {
					logger.error("Password is missing for the request  " + url + " for userName" + userName);
					throw new WorkItemHandlerException("Basic Authentication password is missing");
				}
			}
			else if (token != null){
				headers.add("Authorization", "Bearer " + token);
				logger.debug("Token based authentication");

			}
			else {
				logger.warn("No authentication for the rest api request " + url);
			}


			HttpEntity<?> entity = new HttpEntity<>(body, headers);
			logger.debug("Prepared http entity");
			ResponseEntity<?> response = null;
			boolean success = false;
			int count = 0;
			while (count < retries && !success) {
				try {
					response = restTemplate.exchange(url + parameter,
						method,
						entity,
						String.class);
					logger.debug("Received response");
					ServiceResponse serviceResponse = createServiceResponse(response);
					logger.trace("Body received is " + response.getBody());
					logger.trace("Code reason received is " + response.getStatusCode().getReasonPhrase());
					workItemResult.put(SERVICE_RESPONSE, serviceResponse);
					if (response.getStatusCodeValue() < 500) {
						success = true;
					} else {
						count++;
						logger.warn("We had a return status code" + response.getStatusCodeValue() + "so we will retry (this was attempt " + count + ")");
					}
				} catch (RestManageException rme) {
					count++;
					logger.warn("We had an error (code: " + rme.getStatusCode()+ ") when invoking the url " + url + ".", rme.getMessage());
					ServiceResponse resp = new ServiceResponse();
					resp.setResponseCode(rme.getStatusCode());
					resp.setBody(rme.getMessage());			
					resp.addHeaderValue("ErrorMessage", rme.getStatusText());
					workItemResult.put(SERVICE_RESPONSE, resp);
					Thread.sleep(delay * count);
				} catch (Exception ex2) {
					count++;
					logger.warn("We had an error when invoking the url " + url, ex2);
					ServiceResponse resp = new ServiceResponse();
					resp.setBody(ex2.getMessage());
					resp.setResponseCode(500);
					resp.addHeaderValue("ErrorMessage", ex2.getMessage());
					workItemResult.put(SERVICE_RESPONSE, resp);
					Thread.sleep(delay * count);
				}
			}

			manager.completeWorkItem(workItem.getId(), workItemResult);

			logger.trace("Service Rest API completed for   " + url );

		}
		catch (Exception ex) {
			logger.error("Error in ServiceRestApiDelegate: " + ex.getMessage(), ex);
			throw new WorkItemHandlerException("Error in ServiceRestApiDelegate:",ex);
		}

	}


	private ServiceResponse createServiceResponse(ResponseEntity<?> response) {
		ServiceResponse serviceResponse = new ServiceResponse();
		serviceResponse.setBody(response.getBody() != null ? response.getBody().toString() : "");
		serviceResponse.setResponseCode(response.getStatusCodeValue());
		serviceResponse.setHeaders(response.getHeaders());
		return serviceResponse;
	}


	private HttpHeaders buildHeaderFromClientRequest(Map<String, List<String>> requestHeaders) {
		HttpHeaders headers = new HttpHeaders();
		for (Entry<String, List<String>> entry : requestHeaders.entrySet()) {
			headers.add(entry.getKey(), entry.getValue().get(0));
		}

		return headers;
	}


	private HttpMethod getMethod(WorkItem workItem) {

		String requestType = (String)workItem.getParameter("method");
		if("GET".equalsIgnoreCase(requestType)) {
			return HttpMethod.GET;
		}
		else if("POST".equalsIgnoreCase(requestType)) {
			return HttpMethod.POST;
		}
		else if("PUT".equalsIgnoreCase(requestType)) {
			return HttpMethod.PUT;
		}
		else if("DELETE".equalsIgnoreCase(requestType)) {
			return HttpMethod.DELETE;
		}
		else
		{
			return null;
		}

	}

	private HttpHeaders buildHeader(WorkItem workItem) {

		HttpHeaders headers = new HttpHeaders();

		for (Entry<String, Object> entry : workItem.getParameters().entrySet()) {
			if (entry.getKey().startsWith(HEADER_VALUE_PARAM)) {
				String key = entry.getKey().replace(HEADER_VALUE_PARAM, "");
				if (key.contains("__")) {
					key = key.replace("__", "-");
				}
				headers.add(key,  (String) entry.getValue());
			}
		}

		return headers;
	}


	private String  buildParams(WorkItem workItem) {

		String parameters = "?";

		for (Entry<String, Object> entry : workItem.getParameters().entrySet()) {
			if (entry.getKey().startsWith(PARAM_VALUE_PARAM)) {
				String key = entry.getKey().replace(PARAM_VALUE_PARAM, "");
				String value = (String) entry.getValue();
				parameters = parameters + key + "=" + value + "&";
			}
		}

		parameters = parameters.substring(0, parameters.length() - 1);


		return parameters;
	}


	private Object buildBody(WorkItem workItem, HttpHeaders headers) {

		String bodyEncoding = (String)workItem.getParameter("bodyEncoding");
		Object body = null;

		if("raw".equalsIgnoreCase(bodyEncoding)) {
			body = workItem.getParameter("body");
		}
		else if ("x-www-form-urlencoded".equalsIgnoreCase(bodyEncoding)) {
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
			for (Entry<String, Object> entry : workItem.getParameters().entrySet()) {
				if (entry.getKey().startsWith(BODY_VALUE_PARAM)) {
					String key = entry.getKey().replace(BODY_VALUE_PARAM, "");
					map.add(key,(String) entry.getValue());
					logger.trace("Added body param " + key + "=" + (String)entry.getValue());
				}
			}
			body = map;

		}

		return body;
	}

	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		manager.abortWorkItem(workItem.getId());
		logger.warn("Service Rest API delegate Aborted");
	}


}
