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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.elimu.a2d2.exception.WorkItemHandlerException;
import io.elimu.a2d2.genericmodel.ServiceResponse;

public class ServiceSoapAPIDelegate implements WorkItemHandler {

	private static final String SERVICE_RESPONSE = "serviceResponse";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {

		try {

			Map<String, Object> workItemResult = workItem.getResults();
			String url = (String) workItem.getParameter("url");

			if (Strings.isNullOrEmpty(url)) {
				logger.error("Url is null");
				throw new WorkItemHandlerException("Url is null");
			}

			logger.debug("url for soap::" + url);
			String body = (String) workItem.getParameter("body");

			if (Strings.isNullOrEmpty(body)) {
				logger.error("body is null");
				throw new WorkItemHandlerException("Body is null");
			}

			String soapaction = (String) workItem.getParameter("soapaction");
			if ("".equals(soapaction)) soapaction = null;
			if (soapaction == null) {
				logger.debug("soapaction is null");
			}
			String username = (String) workItem.getParameter("username");
			if ("".equals(username)) username = null;
			if (username == null) {
				logger.debug("username is null");
			}
			String password = (String) workItem.getParameter("password");
			if ("".equals(password)) password = null;
			if (password == null) {
				logger.debug("password is null");
			}
			if (username != null && password == null) {
				logger.error("username provided without password");
				throw new WorkItemHandlerException("username provided without password");
			}

			logger.debug("body for soap::" + body);
			InputStream is = new ByteArrayInputStream(body.getBytes());
			SOAPMessage message = MessageFactory.newInstance().createMessage(null, is);
			if (soapaction != null)
				message.getMimeHeaders().addHeader("SOAPAction", soapaction);
			if (username != null) {
				setAuthHeader(message, username, password);
			}
			SOAPConnectionFactory soapconnectionfactory = SOAPConnectionFactory.newInstance();
			SOAPConnection connection = soapconnectionfactory.createConnection();
			java.net.URL endpoint = new URL(url);
			SOAPMessage response = connection.call(message, endpoint);
			logger.debug("body for soap::" + response);
			connection.close();
			ServiceResponse serviceResponse = createServiceResponse(response);
			workItemResult.put(SERVICE_RESPONSE, serviceResponse);
			manager.completeWorkItem(workItem.getId(), workItemResult);

		} catch (SOAPException | IOException ex) {
			logger.error("Error is SOAP request : ", ex.getMessage());
			throw new WorkItemHandlerException("Exception in requesting SOAP :: "+ex.getMessage());
		}
	}

	private ServiceResponse createServiceResponse(SOAPMessage response) throws SOAPException, IOException {
		ServiceResponse serviceResponse = new ServiceResponse();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		response.writeTo(out);
		serviceResponse.setBody(new String(out.toByteArray()));
		serviceResponse.setResponseCode(200);
		serviceResponse.setHeaders(null);
		return serviceResponse;
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		manager.abortWorkItem(workItem.getId());
		logger.warn("Service SOAP API delegate Aborted");
	}

	private void setAuthHeader(SOAPMessage message, String username, String password) throws SOAPException {

            SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
            SOAPFactory factory = SOAPFactory.newInstance();
            String prefix = "wsse";
            String uri = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
            SOAPElement securityElem =
                    factory.createElement("Security", prefix, uri);
            SOAPElement tokenElem =
                    factory.createElement("UsernameToken", prefix, uri);
            tokenElem.addAttribute(QName.valueOf("wsu:Id"), "UsernameToken-2");
            tokenElem.addAttribute(QName.valueOf("xmlns:wsu"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
            SOAPElement userElem =
                    factory.createElement("Username", prefix, uri);
            userElem.addTextNode(username);
            SOAPElement pwdElem =
                    factory.createElement("Password", prefix, uri);
            pwdElem.addTextNode(password);
            pwdElem.addAttribute(QName.valueOf("Type"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
            tokenElem.addChildElement(userElem);
            tokenElem.addChildElement(pwdElem);
            securityElem.addChildElement(tokenElem);
            SOAPHeader header = envelope.addHeader();
            header.addChildElement(securityElem);
	}
}
