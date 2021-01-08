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

import java.util.Map.Entry;
import org.kie.api.runtime.process.WorkItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.elimu.a2d2.genericmodel.ClientRequest;
import io.elimu.a2d2.genericmodel.ServiceRequest;

public class ServiceDelegateHelper {

	private static final String SERVICE_REQUEST = "serviceRequest";
	private static final String CLIENT_REQUEST = "clientRequest";
	private static final String REQUEST_BODY = "body";
	private static final String REQUEST_PATH = "path";
	private static final String REQUEST_USER = "user";
	private static final String REQUEST_METHOD = "method";
	private static final String HEADER_VALUE_PARAM = "header_value_";
	private static final String PARAM_VALUE_PARAM = "param_value_";
	private static final String AUTH_TOKEN = "authToken";
	private static final String URL = "url";
	private static final String BASIC_USER_NAME = "basicUserName";
	private static final String BASIC_PASSWORD = "basicPassword";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public ServiceRequest createServiceRequest(WorkItem workItem) {

		String body = (String) workItem.getParameter(REQUEST_BODY);
		String path = (String) workItem.getParameter(REQUEST_PATH);
		String user = (String) workItem.getParameter(REQUEST_USER);
		String method = (String) workItem.getParameter(REQUEST_METHOD);

		ServiceRequest serviceRequest = (ServiceRequest) workItem.getParameter(SERVICE_REQUEST);

		if (serviceRequest == null) {
			serviceRequest = new ServiceRequest();
		}

		if (body != null) {
			serviceRequest.setBody(body);
		} else {
			logger.warn("Service request body is null");
		}

		if (path != null) {
			serviceRequest.setPath(path);
		} else {
			logger.warn("Service request path is null");
		}

		if (user != null) {
			serviceRequest.setUser(user);
		} else {
			logger.warn("Service request user is null");
		}

		if (method != null) {
			serviceRequest.setMethod(method);
		} else {
			logger.warn("Service request method  is null");
		}
		setServiceReqParams(serviceRequest, workItem);
		logger.trace("Service request is created");
		return serviceRequest;
	}
	
	private void setServiceReqParams(ServiceRequest serviceRequest, WorkItem workItem) {
		for (Entry<String, Object> entry : workItem.getParameters().entrySet()) {
			if (entry.getKey().startsWith(PARAM_VALUE_PARAM)) {
				String key = entry.getKey().replace(PARAM_VALUE_PARAM, "");
				serviceRequest.addParamValue(key, (String) entry.getValue());
			} else if(entry.getKey().startsWith(HEADER_VALUE_PARAM)) {
				String key = entry.getKey().replace(HEADER_VALUE_PARAM, "");
				if (key.contains("__")) {
					key = key.replace("__", "-");
				}
				serviceRequest.addHeaderValue(key, (String) entry.getValue());
			}
		}
	}

	public ClientRequest createClientRequest(WorkItem workItem) {

		ClientRequest clientRequest = null;

		String authToken = (String) workItem.getParameter(AUTH_TOKEN);
		String url = (String) workItem.getParameter(URL);
		String basicUserName = (String) workItem.getParameter(BASIC_USER_NAME);
		String basicPassword = (String) workItem.getParameter(BASIC_PASSWORD);

		ServiceRequest serviceRequest = createServiceRequest(workItem);

		clientRequest = (ClientRequest) workItem.getParameter(CLIENT_REQUEST);

		if (clientRequest == null) {
			clientRequest = new ClientRequest();
		}

		if(serviceRequest.getBody() !=null) {
			clientRequest.setBody(serviceRequest.getBody());
		}

		if(serviceRequest.getHeaders()!=null) {
			clientRequest.setHeaders(serviceRequest.getHeaders());
		}

		if(serviceRequest.getMethod() != null) {
			clientRequest.setMethod(serviceRequest.getMethod());
		}

		if(serviceRequest.getParams() !=null) {
			clientRequest.setParams(serviceRequest.getParams());
		}

		if(serviceRequest.getPath() != null) {
			clientRequest.setPath(serviceRequest.getPath());
		}

		if(serviceRequest.getUser() != null) {
			clientRequest.setUser(serviceRequest.getUser());
		}

		if (authToken != null) {
			clientRequest.setAuthToken(authToken);
		}
		else {
			logger.warn("Client Request Auth token is null");
		}

		if (url != null) {
			clientRequest.setUrl(url);
		}
		else {
			logger.warn("Client Request URL is null");
		}

		if (basicUserName != null) {
			clientRequest.setBasicUserName(basicUserName);
		}
		else {
			logger.warn("Client Request Basic UserName is null");
		}

		if (basicPassword != null) {
			clientRequest.setBasicPassword(basicPassword);
		}
		else {
			logger.warn("Client Request Basic Password is null");
		}

		return clientRequest;
	}


}
