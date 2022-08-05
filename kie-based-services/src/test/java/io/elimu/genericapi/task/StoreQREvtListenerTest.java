package io.elimu.genericapi.task;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.services.task.events.TaskEventImpl;
import org.jbpm.services.task.impl.model.TaskDataImpl;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.junit.Test;
import org.kie.api.task.TaskEvent;

public class StoreQREvtListenerTest {

	@Test
	public void testStoreQR() throws Exception {
		if (System.getProperty("fhirTokenUrl") == null || System.getProperty("patientId") == null ) {
			return;
		}
		TaskImpl task = new TaskImpl();
		TaskDataImpl taskData = new TaskDataImpl();
		task.setId(Long.valueOf(2));
		Map<String, Object> inputs = new HashMap<>();
		Map<String, Object> outputs = new HashMap<>();
		outputs.put("output1", "Value");
		outputs.put("output2", 3L);
		outputs.put("output3", new URL("https://google.com"));
		
		inputs.put("patient", System.getProperty("patientId"));
		inputs.put("fhirServerVar", System.getProperty("fhirServerUrl"));
		inputs.put("fhirTokenUrl", System.getProperty("fhirTokenUrl"));
		inputs.put("fhirClientId", System.getProperty("fhirClientId"));
		inputs.put("fhirClientSecret", System.getProperty("fhirClientSecret"));
		inputs.put("fhirGrantType", System.getProperty("fhirGrantType"));
		inputs.put("fhirScope", System.getProperty("fhirScope"));
		inputs.put("fhirUsername", System.getProperty("fhirUsername"));
		inputs.put("fhirPassword", System.getProperty("fhirPassword"));
		taskData.setTaskInputVariables(inputs);
		taskData.setTaskOutputVariables(outputs);
		task.setTaskData(taskData);
		StoreQRTaskEventListener listener = new StoreQRTaskEventListener("fhirServerVar", null, true);
		TaskEvent event = new TaskEventImpl(task, null);
		listener.afterTaskCompletedEvent(event);
	}
	
}
