package io.elimu.genericapi.task;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jbpm.services.task.commands.TaskContext;
import org.jbpm.services.task.events.DefaultTaskEventListener;
import org.kie.api.task.TaskEvent;
import org.kie.api.task.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import io.elimu.a2d2.oauth.BodyBuilder;
import io.elimu.a2d2.oauth.OAuthUtils;

public class StoreQRTaskEventListener extends DefaultTaskEventListener {

	private static final Logger LOG = LoggerFactory.getLogger(StoreQRTaskEventListener.class);
	
	private final String fhirServerInputName;
	private final String fhirAuthInputName;
	private final boolean useDefaultAuth;

	public StoreQRTaskEventListener(String fhirServerInputName, String fhirAuthInputName, boolean useDefaultAuth) {
		this.fhirServerInputName = fhirServerInputName;
		this.fhirAuthInputName = fhirAuthInputName;
		this.useDefaultAuth = useDefaultAuth;
	}

	@Override
	public void afterTaskCompletedEvent(TaskEvent event) {
		
		Map<String, Object> inputs = event.getTask().getTaskData().getTaskInputVariables();
		Map<String, Object> outputs = event.getTask().getTaskData().getTaskOutputVariables();
		if (inputs == null || outputs == null) {
			((TaskContext) event.getTaskContext()).getTaskContentService().loadTaskVariables(event.getTask());
			inputs = event.getTask().getTaskData().getTaskInputVariables();
			outputs = event.getTask().getTaskData().getTaskOutputVariables();
		}
		String fhirServerUrl = (String) inputs.get(fhirServerInputName);
		String auth = null;
		if (useDefaultAuth) {
			String tokenUrl = (String) inputs.get("fhirTokenUrl");
			String clientId = (String) inputs.get("fhirClientId");
			String clientSecret = (String) inputs.get("fhirClientSecret");
			String body = new BodyBuilder().addToBody("token_url", tokenUrl).
					addToBody("client_id", clientId).addToBody("client_secret", clientSecret).
					addToBody("grant_type", (String) inputs.get("fhirGrantType")).
					addToBody("scope", (String) inputs.get("fhirScope")).
					addToBody("username", (String) inputs.get("fhirUsername")).
					addToBody("password", (String) inputs.get("fhirPassword")).
					build();
			Map<String, Object> result = OAuthUtils.authenticate(body, tokenUrl, clientId, clientSecret);
			auth = "Bearer " + result.get("access_token");
		} else if (fhirAuthInputName != null) {
			auth = (String) inputs.get(fhirAuthInputName);
		}
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpPost post = new HttpPost(fhirServerUrl + "/QuestionnaireResponse");
			post.addHeader("Content-Type", "application/json+fhir");
			if (auth != null) {
				post.addHeader("Authorization", auth);
			}
			post.setEntity(new StringEntity(asQR(event.getTask())));
			HttpResponse response = client.execute(post);
			String outputBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
				throw new IOException(String.format("Response status %i response body %s", response.getStatusLine().getStatusCode(), outputBody));
			}
		} catch (Exception e) {
			LOG.error(String.format("Couldn't store QuestionnaireResponse for task %i of service %s", event.getTask().getId(), event.getTask().getTaskData().getDeploymentId()), e);
		}
	}
	
	public String getInputType(Object obj) {
		return obj == null ? "null" : obj.getClass().getSimpleName().toLowerCase();
	}

	protected String asQR(Task task) throws Exception {
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_30);
		cfg.setClassForTemplateLoading(StoreQRTaskEventListener.class, "/");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setDefaultEncoding("UTF-8");
		File ftlFile = null;
		cfg.setDirectoryForTemplateLoading(new File(System.getProperty("java.io.tmpdir")));
		byte[] data = IOUtils.toByteArray(getClass().getResourceAsStream("/task-qr-template.ftl"));
		ftlFile = File.createTempFile(System.getProperty("java.io.tmpdir"), ".ftl");
		Files.write(data, ftlFile);
		Template template = cfg.getTemplate(ftlFile.getName());
		StringWriter processedOutput = new StringWriter();
		Map<String, Object> templateData = new HashMap<>();
		templateData.put("task", task);
		templateData.put("helper", this);
		template.process(templateData, processedOutput);
		return processedOutput.getBuffer().toString();
	}
}
