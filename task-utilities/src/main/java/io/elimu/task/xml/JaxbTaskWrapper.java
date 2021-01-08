package io.elimu.task.xml;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.drools.core.xml.jaxb.util.JaxbMapAdapter;
import org.jbpm.services.task.impl.model.xml.JaxbTask;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.internal.task.api.model.InternalTaskData;

@XmlRootElement(name="task-wrapper")
@XmlAccessorType(XmlAccessType.NONE)
public class JaxbTaskWrapper {

	@XmlElement
    private JaxbTask task;
	
	@XmlJavaTypeAdapter(JaxbMapAdapter.class)
    @XmlElement
	private Map<String, Object> inputs;
	
	@XmlJavaTypeAdapter(JaxbMapAdapter.class)
    @XmlElement
	private Map<String, Object> outputs;
	
	public Map<String, Object> getInputs() {
		return inputs;
	}

	public void setInputs(Map<String, Object> inputs) {
		this.inputs = inputs;
	}

	public Map<String, Object> getOutputs() {
		return outputs;
	}

	public void setOutputs(Map<String, Object> outputs) {
		this.outputs = outputs;
	}
	
	public void internalSetTask(JaxbTask task) {
		this.task = task;
	}

	public Task getTaskWithInputsAndOutputs() {
		InternalTask internalTask = (InternalTask) this.task.getTask();
		if (inputs != null) {
			((InternalTaskData) internalTask.getTaskData()).setTaskInputVariables(inputs);
		}
		if (outputs != null) {
			((InternalTaskData) internalTask.getTaskData()).setTaskOutputVariables(outputs);
		}
		return internalTask;
	}
}
