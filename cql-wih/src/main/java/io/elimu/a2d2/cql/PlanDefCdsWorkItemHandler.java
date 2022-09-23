package io.elimu.a2d2.cql;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.ToNumberPolicy;

import io.elimu.a2d2.cdsresponse.entity.CDSHookRequest;
import io.elimu.a2d2.cdsresponse.entity.CDSResponse;
import io.elimu.a2d2.cdsresponse.entity.FHIRAuthorization;
import io.elimu.a2d2.exception.WorkItemHandlerException;

public class PlanDefCdsWorkItemHandler implements WorkItemHandler {

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {

		Map<String, Object> results = workItem.getResults();
		String fhirServerUrl = (String) workItem.getParameter("fhirServerUrl");
		String planDefId = (String) workItem.getParameter("planDefinitionId");
		//TODO validate required fhirServerUrl
		//TODO validate required planDefinitionId
		String cdsServerUrl = fhirServerUrl.replace("/fhir", "/cds-services");
		String fhirServerAuth = (String) workItem.getParameter("fhirServerAuth");
		Map<String, Object> prefetchData = new HashMap<>();
		Map<String, String> contextData = new HashMap<>();
		for (String key : workItem.getParameters().keySet()) {
			if (key.startsWith("context_")) {
				contextData.put(key.replace("context_", ""), String.valueOf(workItem.getParameter(key)));
			}
			if (key.startsWith("prefetch_")) {
				prefetchData.put(key.replace("prefetch_", ""), workItem.getParameter(key));
			}
		}
		
		//first we get the cds-services URL via discovery
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpGet get = new HttpGet(cdsServerUrl);
			get.setHeader("Content-Type", "application/json");
			if (fhirServerAuth != null) {
				get.setHeader("Authorization", fhirServerAuth);
			}
			HttpResponse response = client.execute(get);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode services = mapper.readTree(response.getEntity().getContent());
			String hook = null;
			String requestPath = null;
			Map<String, String> prefetches = new HashMap<>();
			for (JsonNode service : services.get("services")) {
				if (service.has("id") && planDefId.equals(service.get("id").asText())) {
					hook = service.get("hook").asText();
					requestPath = service.get("id").asText();
					for (Iterator<String> iter = ((ObjectNode) service.get("prefetch")).fieldNames(); iter.hasNext(); ) {
						String key = iter.next();
						prefetches.put(key, service.get("prefetch").get(key).asText());
					}
				}
			}
			
			//prefetch everything requested
			for (String key : prefetches.keySet()) {
				if (!prefetchData.containsKey(key)) {
					String item = prefetches.get(key);
					String query = fhirServerUrl + "/" + extractQuery(item, contextData);
					Object fhirPrefetch = extractFhirData(client, query, fhirServerAuth);
					prefetchData.put(key, fhirPrefetch);
				}
			}

			//invoke the cds-service
			CDSHookRequest cdsRequest = new CDSHookRequest();
			cdsRequest.setHookInstance(UUID.randomUUID().toString());//TODO maybe provided?
			cdsRequest.setHook(hook);//Obtain it from querying the fhirServerUrl/../cds-services
			cdsRequest.setFhirServer(fhirServerUrl);
			if (fhirServerAuth != null && !"".equals(fhirServerAuth)) {
				cdsRequest.setFhirAuthorization(new FHIRAuthorization());
				cdsRequest.getFhirAuthorization().setAccessToken(fhirServerAuth);//TODO might need more items
			}
			cdsRequest.setContext(new HashMap<String, Object>(contextData));
			cdsRequest.setPrefetch(prefetchData);
			String url = cdsServerUrl + "/" + requestPath;
			HttpPost post = new HttpPost(url);
			post.setHeader("Content-Type", "application/json");
			post.setHeader("Accept", "application/json");
			
			if (fhirServerAuth != null) {
				post.setHeader("Authorization", fhirServerAuth);
			}
			String cdsJson = new GsonBuilder().setPrettyPrinting().create().toJson(cdsRequest);
			post.setEntity(new StringEntity(cdsJson));
			HttpResponse response2 = client.execute(post);
			if (response2.getStatusLine().getStatusCode() != 200) {
				throw new WorkItemHandlerException("Cds Service " + url + " returned status " + response2.getStatusLine().getStatusCode());
			}
			InputStream json = response2.getEntity().getContent();
			CDSResponse cdsResponse = new Gson().fromJson(new InputStreamReader(json), CDSResponse.class);
			//return the cards
			results.put("cards", cdsResponse.getCards());
			manager.completeWorkItem(workItem.getId(), results);
		} catch (IOException e) {
			throw new WorkItemHandlerException("Problem invoking CDS service " + planDefId, e);
		}
	}

	private Object extractFhirData(CloseableHttpClient client, String fhirQuery, String fhirServerAuth) throws IOException {
		HttpGet get = new HttpGet(fhirQuery);
		get.setHeader("Content-Type", "application/json");
		if (fhirServerAuth != null && !"".equals(fhirServerAuth.trim())) {
			get.setHeader("Authorization", fhirServerAuth);
		}
		HttpResponse response = client.execute(get);
		InputStream json = response.getEntity().getContent();
		return new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).
				setLongSerializationPolicy(LongSerializationPolicy.STRING).
				setNumberToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create().
				fromJson(new InputStreamReader(json), HashMap.class);
	}

	protected String extractQuery(String item, Map<String, String> context) {
        if (item.matches(".{0,}\\{\\{context\\..*\\}\\}.*")) {
        	while (true) {
	        	Pattern pattern = Pattern.compile(".{0,}\\{\\{context\\.(.*?)\\}\\}.*");
	        	Matcher matcher = pattern.matcher(item);
	        	if (matcher.find()) {
		        	String ctxKey = matcher.group(1);
		        	item = item.replace("{{context." + ctxKey + "}}", context.get(ctxKey));
	        	} else {
	        		break;
	        	}
        	}
        }
        return item;
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// do nothing
	}
}
