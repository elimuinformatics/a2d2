package io.elimu.a2d2.cql;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import io.elimu.a2d2.exception.WorkItemHandlerException;

public class CqlClientWorkItemHandler implements WorkItemHandler {

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// do nothing
	}

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String fhirServerUrl = (String) workItem.getParameter("fhirServerUrl");
		String fhirServerAuth = (String) workItem.getParameter("fhirServerAuth");
		String measureId = (String) workItem.getParameter("measureId");
		String periodStart = (String) workItem.getParameter("periodStart");
		Date periodStartDate = (Date) workItem.getParameter("periodStartDate");
		String periodEnd = (String) workItem.getParameter("periodEnd");
		Date periodEndDate = (Date) workItem.getParameter("periodEndDate");
		String reportType = (String) workItem.getParameter("reportType");
		String subject = (String) workItem.getParameter("subject");
		String practitioner = (String) workItem.getParameter("practitioner");
		String lastReceivedOn = (String) workItem.getParameter("lastReceivedOn");
		String productLine = (String) workItem.getParameter("productLine");
		Object additionalData = workItem.getParameter("additionalData"); //Bundle
		String additionalDataJson = (String) workItem.getParameter("additionalDataJson"); //Bundle//JSON String

		List<String> missingVariables = new ArrayList<>();
		if (fhirServerUrl == null || "".equals(fhirServerUrl.trim())) {
			missingVariables.add("fhirServerUrl");
		}
		if (measureId == null || "".equals(measureId.trim())) {
			missingVariables.add("measureId");
		}
		if (!missingVariables.isEmpty()) {
			throw new WorkItemHandlerException("Cannot process without required variables: " + missingVariables);
		}
		
		Map<String, Object> results = workItem.getResults();
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Object ctx = cl.loadClass("ca.uhn.fhir.context.FhirContext").getMethod("forR4").invoke(null);
			Object parser = ctx.getClass().getMethod("newJsonParser").invoke(ctx);
			StringBuilder query = new StringBuilder(fhirServerUrl).append("/Measure/").append(measureId).append("/$evaluate-measure?");
			boolean hasParams = false;
			if (periodStart != null && !"".equals(periodStart)) {
				query.append("periodStart=").append(periodStart);
				hasParams = true;
			} else if (periodStartDate != null) {
				query.append("periodStart=").append(new SimpleDateFormat("yyyy-MM-dd").format(periodStartDate));
				hasParams = true;
			}
			if (periodEnd != null && !"".equals(periodEnd)) {
				if (hasParams) {
					query.append('&');
				}
				query.append("periodEnd=").append(periodEnd);
				hasParams = true;
			} else if (periodEndDate != null) {
				if (hasParams) {
					query.append('&');
				}
				query.append("periodEnd=").append(new SimpleDateFormat("yyyy-MM-dd").format(periodEndDate));
				hasParams = true;
			}
			if (reportType != null && !"".equals(reportType)) {
				if (hasParams) {
					query.append('&');
				}
				query.append("reportType=").append(reportType);
				hasParams = true;
			}
			if (subject != null && !"".equals(subject)) {
				if (hasParams) {
					query.append('&');
				}
				query.append("subject=").append(subject);
				hasParams = true;
			}
			if (practitioner != null && !"".equals(practitioner)) {
				if (hasParams) {
					query.append('&');
				}
				query.append("practitioner=").append(practitioner);
				hasParams = true;
			}
			if (lastReceivedOn != null && !"".equals(lastReceivedOn)) {
				if (hasParams) {
					query.append('&');
				}
				query.append("lastReceivedOn=").append(lastReceivedOn);
				hasParams = true;
			}
			if (productLine != null && !"".equals(productLine)) {
				if (hasParams) {
					query.append('&');
				}
				query.append("productLine=").append(productLine);
				hasParams = true;
			}
			HttpUriRequest request = additionalData == null && additionalDataJson == null ? new HttpGet(query.toString()) : new HttpPost(query.toString());
			request.addHeader("Content-Type", "application/json");
			if (additionalData != null) {
				String json = (String) parser.getClass().getMethod("encodeResourceToString", cl.loadClass("org.hl7.fhir.instance.model.api.IBaseResource")).invoke(parser, additionalData);
				((HttpPost) request).setEntity(new StringEntity(json));
			} else if (additionalDataJson != null) {
				((HttpPost) request).setEntity(new StringEntity(additionalDataJson));
			}
			if (fhirServerAuth != null && !"".equals(fhirServerAuth)) {
				request.addHeader("Authorization", fhirServerAuth);
			}
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == 200) {
				Class<?> mrClass = cl.loadClass("org.hl7.fhir.r4.model.MeasureReport");
				Object report = parser.getClass().getMethod("parseResource", Class.class, InputStream.class).invoke(parser, mrClass, response.getEntity().getContent());
				List<?> group = (List<?>) mrClass.getMethod("getGroup").invoke(report);
				for (int index = 0; index < group.size(); index++) {
					Object component = group.get(index);
					List<?> populations = (List<?>) component.getClass().getMethod("getPopulation").invoke(component);
					for (int j = 0; j < populations.size(); j++) {
						Object population = populations.get(j);
						Object count = population.getClass().getMethod("getCount").invoke(population);
						Object cc = population.getClass().getMethod("getCode").invoke(population);
						Object coding = cc.getClass().getMethod("getCodingFirstRep").invoke(cc);
						String code = (String) coding.getClass().getMethod("getCode").invoke(coding);
						String system = (String) coding.getClass().getMethod("getSystem").invoke(coding);
						String key1 = "group_" + index + "_population_" + code;
						String key2 = "group_" + index + "_population_" + system + "|" + code;
						results.put(key1, count);
						results.put(key2, count);
						String id = (String) population.getClass().getMethod("getId").invoke(population);
						if (id != null) {
							results.put(id, count);
						}
					}
					Object score = component.getClass().getMethod("getMeasureScore").invoke(component);
					Object value = score.getClass().getMethod("getValue").invoke(score);
					results.put("group_" + index + "_measureScore", value);
				}
				results.put("report", report);
				manager.completeWorkItem(workItem.getId(), results);
			} else {
				throw new WorkItemHandlerException("Response from CQL run in server is status "  + response.getStatusLine().getStatusCode());
			}
		} catch (IOException e) {
			throw new WorkItemHandlerException("Cannot send request to CQL run in server", e);
		} catch (ReflectiveOperationException e) {
			throw new WorkItemHandlerException("Problem with reflection sending request to CQL run in server", e);
		}
	}
}
