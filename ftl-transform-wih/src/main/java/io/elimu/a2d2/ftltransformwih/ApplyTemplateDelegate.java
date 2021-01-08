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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import freemarker.ext.dom.NodeModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import io.elimu.a2d2.cdsresponse.entity.TemplateRepository;
import io.elimu.a2d2.exception.WorkItemHandlerException;

public class ApplyTemplateDelegate implements WorkItemHandler {

	private static final String FTL_PARAM = "ftl_param_";
	private static final String XML_PARAM = "xml_param_";
	private static final Logger log = LoggerFactory.getLogger(ApplyTemplateDelegate.class);

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {

		try {
			Map<String, Object> workItemResult = workItem.getResults();
			Map<String, Object> templateData = new HashMap<String, Object>();
			String templateFileName = (String) workItem.getParameter("templateFileName");
			String templateContent = null;
			Map<String, String> fltMap = TemplateRepository.getInstance().getFltMap();

			for (Entry<String, Object> entry : workItem.getParameters().entrySet()) {
				if (entry.getKey().startsWith(FTL_PARAM)) {
					templateData.put(entry.getKey(), entry.getValue());
				}
				else if (entry.getKey().startsWith(XML_PARAM)) {
					NodeModel xmlNode = NodeModel.parse(new InputSource(new StringReader((String)entry.getValue())));
					templateData.put(entry.getKey(), xmlNode);
				}

			}

			for (Entry<String, String> entry : fltMap.entrySet()) {
				if (entry.getKey().equalsIgnoreCase(templateFileName)) {
					templateContent = entry.getValue();
					log.trace("ftl content found " + templateContent + " for::  " + templateFileName);
					break;
				}
			}

			String ftlFile = saveFile(templateContent);

			if (ftlFile != null) {

				Configuration cfg = new Configuration(Configuration.VERSION_2_3_30);
				cfg.setClassForTemplateLoading(ApplyTemplateDelegate.class, "/");
				cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
				cfg.setDefaultEncoding("UTF-8");
				cfg.setDirectoryForTemplateLoading(new File(System.getProperty("java.io.tmpdir")));

				Template template = cfg.getTemplate(ftlFile);

				StringWriter processedOutput = new StringWriter();
				template.process(templateData, processedOutput);
				String transformedData = processedOutput.getBuffer().toString();

				workItemResult.put("transformedData", transformedData);
				manager.completeWorkItem(workItem.getId(), workItemResult);

				processedOutput.flush();
			} else {
				log.error("Error in ApplyTemplateDelegate in saving file");
			}

			log.trace("Apply template delegate completed");

		} catch (Exception e) {
			log.error("Error in ApplyTemplateDelegate . " + e.getMessage() + ". [" + e.getClass().getName() + "]");
			log.error("Stack trace", e);
			throw new WorkItemHandlerException("Error in ApplyTemplateDelegate . " + e.getMessage() + ". [" + e.getClass().getName() + "]",e);
		}

	}

	public String saveFile(String ftlContent) {

		String filePath = null;
		String fileName = "test" + new Random().nextInt(10000) + ".ftl";
		filePath = System.getProperty("java.io.tmpdir") + "/" + fileName;

		log.debug("ftl file path is ::: " + filePath);

		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "utf-8"))) {
			writer.write(ftlContent);
		} catch (Exception e) {
			log.error("Error in saveFile . " + e.getMessage() + ". [" + e.getClass().getName() + "]");
			log.error("Stack trace", e);
			return null;
		}

		return fileName;
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		//we should never invoke manager->abortWorkItem on a service task that is synchronic, even if an error occurs
		//manager.abortWorkItem(workItem.getId());
	}

}
