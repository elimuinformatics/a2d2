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

package io.elimu.a2d2.core_wih;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.utils.KieHelper;
import io.elimu.a2d2.corewih.ServiceSoapAPIDelegate;
import io.elimu.a2d2.exception.WorkItemHandlerException;
import io.elimu.a2d2.genericmodel.ServiceResponse;

public class ServiceSoapAPIDelegateTest {

	private static WorkItemManager workItemManager;
	private static WorkItemHandler wih;
	private static final String SERVICE_RESPONSE = "serviceResponse";

	@BeforeClass
	public static void initResourcesForTest() {

		KieHelper kieHelper = new KieHelper();
		KieBase kieBase = kieHelper.build();
		KieSession ksession = kieBase.newKieSession();
		wih = (WorkItemHandler) new ServiceSoapAPIDelegate();
		workItemManager = ksession.getWorkItemManager();
		workItemManager.registerWorkItemHandler("ServiceSoapAPIDelegate", wih);
	}

	@Test @Ignore("Address throwing a 302")
	public void testSOAPAPIWithParameters() {

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("url", "http://www.dataaccess.com/webservicesserver/numberconversion.wso?WSDL");
		execution.setParameter("body",
				"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://www.dataaccess.com/webservicesserver/\">\r\n   <soapenv:Header/>\r\n   <soapenv:Body>\r\n      <web:NumberToWords>\r\n         <web:ubiNum>11</web:ubiNum>\r\n      </web:NumberToWords>\r\n   </soapenv:Body>\r\n</soapenv:Envelope>");
		wih.executeWorkItem(execution, workItemManager);
		ServiceResponse serviceResponse = (ServiceResponse) execution.getResult(SERVICE_RESPONSE);
		Assert.assertNotNull(serviceResponse);
		Assert.assertNotNull(serviceResponse.getBody());
	}

	@Ignore("Broken, fix later")
	@Test
	public void testPartnersAPIWithParameters() {

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("url",
				"https://dev-webapi.partners.org/Other/Epic/Clinical/MedicationAdministrationRecord/DEV?wsdl");
		execution.setParameter("body",
				"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:Custom-com:PHS.2014.Services.MedAdmin\" "+
						"xmlns:urn1=\"urn:Custom-com:PHS.2014.Services.CommonObjects\">"+
						"<soapenv:Body><urn:getMedicationAdministrationHistory><!--Optional:--><urn:AdministrationStartDate>01-01-2016</urn:AdministrationStartDate><!--Optional:--><urn:AdministrationEndDate>12-31-2019</urn:AdministrationEndDate><!--Optional:--><!--urn:AllAdminActions></urn:AllAdminActions--><!--Optional:--><urn:PHSRequest><!--Optional:--><urn1:PHSConsumer/><!--Optional:--><urn1:PatientID>20400475974</urn1:PatientID><!--Optional:--><urn1:PatientIDType>PMRN</urn1:PatientIDType><!--Optional:--><urn1:Reserved1/><!--Optional:--><urn1:Reserved2/><!--Optional:--><urn1:Reserved3/><!--Optional:--></urn:PHSRequest></urn:getMedicationAdministrationHistory></soapenv:Body></soapenv:Envelope>");

		// put real values to test
		execution.setParameter("username", "NOTREAL");
		execution.setParameter("password", "NOTREAL");
		execution.setParameter("soapaction",
				"urn:Custom-com:PHS.2014.Services.MedAdmin.getMedicationAdministrationHistory");

		wih.executeWorkItem(execution, workItemManager);
		ServiceResponse serviceResponse = (ServiceResponse) execution.getResult(SERVICE_RESPONSE);
		Assert.assertNotNull(serviceResponse);
		Assert.assertNotNull(serviceResponse.getBody());
	}

	@Test(expected = WorkItemHandlerException.class)
	public void testSOAPAPIWithoutUrlParameters() {

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("url", "");
		execution.setParameter("body",
				"<soapenv:Envelope xmlns:soapenv=\\\"http://schemas.xmlsoap.org/soap/envelope/\\\" xmlns:web=\\\"http://www.dataaccess.com/webservicesserver/\\\">\\r\\n   <soapenv:Header/>\\r\\n   <soapenv:Body>\\r\\n      <web:NumberToWords>\\r\\n         <web:ubiNum>11</web:ubiNum>\\r\\n      </web:NumberToWords>\\r\\n   </soapenv:Body>\\r\\n</soapenv:Envelope>");
		wih.executeWorkItem(execution, workItemManager);
		ServiceResponse serviceResponse = (ServiceResponse) execution.getResult(SERVICE_RESPONSE);
		Assert.assertNotNull(serviceResponse);
		Assert.assertNotNull(serviceResponse.getBody());
	}

	@Test(expected = WorkItemHandlerException.class)
	public void testSOAPAPIWithoutBodyParameters() {

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("url", "http://www.dataaccess.com/webservicesserver/numberconversion.wso?WSDL");
		execution.setParameter("body", "");
		wih.executeWorkItem(execution, workItemManager);
		ServiceResponse serviceResponse = (ServiceResponse) execution.getResult(SERVICE_RESPONSE);
		Assert.assertNotNull(serviceResponse);
		Assert.assertNotNull(serviceResponse.getBody());
	}
}
