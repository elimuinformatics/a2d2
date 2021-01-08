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

package io.elimu.a2d2.ftltransformwih;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.utils.KieHelper;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.QuestionnaireResponse;
import io.elimu.a2d2.cdsresponse.entity.TemplateRepository;
import io.elimu.a2d2.ftltransformwih.ApplyTemplateDelegate;

public class ApplyTemplateDelegateTest {

	@Test
	public void testXmlFLTTemplate() {

		String questionnaireResponseString = "{\"resourceType\":\"QuestionnaireResponse\",\"status\":\"completed\","
				+ "\"subject\":{\"reference\":\"http://fhir.elimu.io/Patient/123456\"},\"group\":[{\"linkId\":\"centor-score\","
				+ "\"question\":[{\"linkId\":\"fever-present-boolean\",\"answer\":[{\"valueBoolean\":true}]},"
				+ "{\"linkId\":\"lack-of-cough-boolean\",\"answer\":[{\"valueBoolean\":true}]},{\"linkId\":"
				+ "\"tonsillar-exudate-boolean\",\"answer\":[{\"valueBoolean\":true}]},{\"linkId\":\"swollen-nodes-boolean\","
				+ "\"answer\":[{\"valueBoolean\":true}]}]}]}";

		// String jsonInString =
		// "{\"resourceType\":\"QuestionnaireResponse\",\"id\":\"5635\",\"meta\":{\"versionId\":\"1\",\"lastUpdated\":\"2018-07-13T13:10:13.000+00:00\"},\"status\":\"in-progress\",\"subject\":{\"reference\":\"Patient/962\"},\"author\":{\"reference\":\"Patient/962\"},\"authored\":\"2018-07-13T13:10:12+00:00\",\"group\":{\"linkId\":\"vancouver-risk-score\",\"title\":\"vancouver-risk-score\",\"question\":[{\"linkId\":\"age-value\",\"answer\":[{\"valueString\":\"Less_than_18\"}]},{\"linkId\":\"gender-value\",\"answer\":[{\"valueString\":\"male\"}]},{\"linkId\":\"number-of-encounter-value\",\"answer\":[{\"valueString\":\"true\"}]}]}}";

		FhirContext ctx = FhirContext.forDstu2();
		QuestionnaireResponse questionnaireResponseObject = (QuestionnaireResponse) ctx.newJsonParser()
				.parseResource(questionnaireResponseString);

		String questionnaireResponseJSONString = ctx.newJsonParser()
				.encodeResourceToString(questionnaireResponseObject);

		Map<String, String> fltMap = TemplateRepository.getInstance().getFltMap();
		String ftlContents = "<#ftl strip_whitespace=\"true\">\r\n<#compress>\r\n<#assign qr = ftl_param_qr_str?eval>"
				+ "\r\n<xf:instance id=\"sapphire-autofill\" xmlns:xf=\"http://www.w3.org/2002/xforms\">\r\n    "
				+ "<${qr.group.linkId}>\r\n       <#list qr.group.question as question>\r\n          "
				+ "<${question.linkId}><#t>\r\n              ${(question.answer[0].valueBoolean?c)!}<#t>\r\n              "
				+ "${(question.answer[0].valueString)!}<#t>\r\n              ${(question.answer[0].valueDecimal?c)!}<#t>\r\n"
				+ "              ${(question.answer[0].valueInteger)!}<#t>\r\n              ${(question.answer[0].valueDate)!}"
				+ "<#t>\r\n              ${(question.answer[0].valueDateTime)!}<#t>\r\n       <#t></${question.linkId}>\r\n"
				+ "        </#list>\r\n    </${qr.group.linkId}>\r\n</xf:instance>\r\n</#compress>";
		fltMap.put("create-resource_QuestionnaireResponseToXmlOrbeon.ftl", ftlContents);

		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("ftl_param_qr_str", questionnaireResponseJSONString);
		execution.setParameter("templateFileName", "create-resource_QuestionnaireResponseToXmlOrbeon.ftl");

		KieHelper kieHelper = new KieHelper();
		KieBase kieBase = kieHelper.build();
		KieSession ksession = kieBase.newKieSession();

		WorkItemHandler wih = (WorkItemHandler) new ApplyTemplateDelegate();
		WorkItemManager workItemManager = ksession.getWorkItemManager();

		workItemManager.registerWorkItemHandler("ApplyTemplateDelegate", wih);
		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNotNull(execution.getResult("transformedData"));

	}

	@Test
	public void testXMLInput() throws IOException {
		String templatePath = "src/test/resources/soap-response-to-json.ftl";
		Map<String, String> ftlMap = TemplateRepository.getInstance().getFltMap();
        String templateContent = new String(Files.readAllBytes(Paths.get(templatePath)));
        ftlMap.put("soap-response-to-json.ftl", templateContent);

        String xmlString = new String(Files.readAllBytes(Paths.get("src/test/resources/soap-response.xml")));
		WorkItemImpl execution = new WorkItemImpl();
		execution.setParameter("xml_param_encounter", xmlString);
		execution.setParameter("templateFileName", "soap-response-to-json.ftl");

		KieHelper kieHelper = new KieHelper();
		KieBase kieBase = kieHelper.build();
		KieSession ksession = kieBase.newKieSession();

		WorkItemHandler wih = (WorkItemHandler) new ApplyTemplateDelegate();
		WorkItemManager workItemManager = ksession.getWorkItemManager();

		workItemManager.registerWorkItemHandler("ApplyTemplateDelegate", wih);
		wih.executeWorkItem(execution, workItemManager);

		Assert.assertNotNull(execution.getResult("transformedData"));
	}
}
