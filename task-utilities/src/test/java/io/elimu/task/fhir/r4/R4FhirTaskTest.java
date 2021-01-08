package io.elimu.task.fhir.r4;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.Task;
import org.jbpm.runtime.manager.impl.RuntimeEngineImpl;
import org.jbpm.services.task.impl.model.xml.JaxbAttachment;
import org.jbpm.services.task.impl.model.xml.JaxbComment;
import org.jbpm.services.task.impl.model.xml.JaxbContent;
import org.jbpm.services.task.impl.model.xml.JaxbContentData;
import org.jbpm.services.task.impl.model.xml.JaxbDeadlines;
import org.jbpm.services.task.impl.model.xml.JaxbFaultData;
import org.jbpm.services.task.impl.model.xml.JaxbI18NText;
import org.jbpm.services.task.impl.model.xml.JaxbOrganizationalEntity;
import org.jbpm.services.task.impl.model.xml.JaxbPeopleAssignments;
import org.jbpm.services.task.impl.model.xml.JaxbTask;
import org.jbpm.services.task.impl.model.xml.JaxbTaskData;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.TaskEvent;
import org.kie.internal.task.api.TaskModelFactory;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.internal.task.api.model.InternalTaskData;

import io.elimu.task.xml.JaxbTaskWrapper;

public class R4FhirTaskTest {

	//@Test
	public void testXmlTask() throws Exception {
		final TaskModelFactory tmf = TaskModelProvider.getFactory();
		InternalTask task = (InternalTask) tmf.newTask();
		task.setTaskData(tmf.newTaskData());
		task.setPeopleAssignments(tmf.newPeopleAssignments());
		JaxbTask xmlTask = new JaxbTask(task);
		JaxbTaskWrapper xmlWrapper = new JaxbTaskWrapper();
		Map<String, Object> inputs = new HashMap<>();
		inputs.put("input1", "Hello");
		inputs.put("input2", "There!");
		Map<String, Object> outputs = new HashMap<>();
		outputs.put("output1", "General");
		outputs.put("output2", "Kenobi!");
		xmlWrapper.internalSetTask(xmlTask);
		xmlWrapper.setInputs(inputs);
		xmlWrapper.setOutputs(outputs);
		StringWriter writer = new StringWriter();
		JAXBContext.newInstance(JaxbAttachment.class, JaxbComment.class, JaxbContent.class,
	        		JaxbContentData.class, JaxbDeadlines.class, JaxbFaultData.class,
	        		JaxbI18NText.class, JaxbOrganizationalEntity.class, JaxbPeopleAssignments.class,
	        		JaxbTaskData.class, JaxbTask.class, JaxbTaskWrapper.class).
			createMarshaller().marshal(xmlWrapper, writer);
	}
	
	//@Test
	public void testR4TaskTransformEmptyTask() throws Exception {
		RuntimeManager manager = createRuntimeManager();
		R4FhirTaskEventListener listener = new R4FhirTaskEventListener(manager, "", "", "");
		final TaskModelFactory tmf = TaskModelProvider.getFactory();
		InternalTask task = (InternalTask) tmf.newTask();
		task.setTaskData(tmf.newTaskData());
		task.setPeopleAssignments(tmf.newPeopleAssignments());
		Map<String, Object> inputs = new HashMap<>();
		inputs.put("input1", "Hello");
		inputs.put("input2", "There!");
		Map<String, Object> outputs = new HashMap<>();
		outputs.put("output1", "General");
		outputs.put("output2", "Kenobi!");
		TaskEvent event = new TestTaskEvent(task, inputs, outputs);
		event.getTaskContext().loadTaskVariables(task);
		Task r4task = (Task) listener.toFhirTask(event);
		Assert.assertNotNull(r4task);
		InternalTask task2 = (InternalTask) listener.toJbpmTask(r4task, task);
		Assert.assertNotNull(task2);
		Assert.assertNotNull(task2.getTaskData().getTaskInputVariables());
		Assert.assertNotNull(task2.getTaskData().getTaskOutputVariables());
		Assert.assertEquals(task.getTaskData().getTaskOutputVariables(), task2.getTaskData().getTaskOutputVariables());
		Assert.assertTrue(task2.getTaskData().getTaskInputVariables().keySet().containsAll(task.getTaskData().getTaskInputVariables().keySet()));
	}

	@Test
	public void testJbpmTaskParsing() throws Exception {
		RuntimeManager manager = createRuntimeManager();
		R4FhirTaskEventListener listener = new R4FhirTaskEventListener(manager, "", "", "");
		String json = IOUtils.toString(getClass().getResourceAsStream("/r4task-output-example.json"));
		Task r4task = FhirContext.forR4().newJsonParser().parseResource(Task.class, json);
		final TaskModelFactory tmf = TaskModelProvider.getFactory();
		InternalTask origTask = (InternalTask) tmf.newTask();
		origTask.setTaskData(tmf.newTaskData());
		((InternalTaskData) origTask.getTaskData()).setTaskInputVariables(new HashMap<>());
		((InternalTaskData) origTask.getTaskData()).setTaskOutputVariables(new HashMap<>());
		origTask.setPeopleAssignments(tmf.newPeopleAssignments());
		listener.toJbpmTask(r4task, origTask);
	}

	private RuntimeManager createRuntimeManager() {
		KieFileSystem kfs = KieServices.get().newKieFileSystem();
		KieBuilder kbuilder = KieServices.get().newKieBuilder(kfs);
		kbuilder.buildAll();
		KieContainer kc = KieServices.get().newKieContainer(kbuilder.getKieModule().getReleaseId());
		final KieSession ksession = kc.newKieSession();
		RuntimeManager manager = new RuntimeManager() {
			@Override
			public RuntimeEngine getRuntimeEngine(Context<?> context) {
				return new RuntimeEngineImpl(ksession, null);
			}
			@Override
			public String getIdentifier() {
				return "test";
			}
			@Override
			public void disposeRuntimeEngine(RuntimeEngine engine) {}
			@Override
			public void close() {}
			@Override
			public void signalEvent(String event, Object signal) {}
		};
		return manager;
	}
}
