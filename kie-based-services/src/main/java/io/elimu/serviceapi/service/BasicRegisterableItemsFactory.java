package io.elimu.serviceapi.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.core.event.DebugAgendaEventListener;
import org.drools.core.event.DebugProcessEventListener;
import org.drools.core.event.DebugRuleRuntimeEventListener;
import org.jbpm.runtime.manager.impl.DefaultRegisterableItemsFactory;
import org.jbpm.runtime.manager.impl.RuntimeEngineImpl;
import org.jbpm.services.task.wih.LocalHTWorkItemHandler;
import org.kie.api.KieBase;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.task.TaskLifeCycleEventListener;
import org.kie.internal.runtime.Cacheable;
import org.kie.internal.runtime.Closeable;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.conf.NamedObjectModel;
import org.kie.internal.runtime.conf.ObjectModel;

public class BasicRegisterableItemsFactory extends DefaultRegisterableItemsFactory {

	private DeploymentDescriptor descriptor;
	protected boolean logExecution;
	private static final String HUMAN_TASK = "Human Task";

	public BasicRegisterableItemsFactory(DeploymentDescriptor descriptor, boolean logExecution) {
		this.descriptor = descriptor;
		this.logExecution = logExecution;
	}
	
	@Override
	public Map<String, WorkItemHandler> getWorkItemHandlers(RuntimeEngine runtime) {
		Map<String, WorkItemHandler> defaultHandlers = new HashMap<>();
        if (descriptor != null) {
        	Map<String, Object> params = getParametersMap(runtime);
        	for (NamedObjectModel model : descriptor.getWorkItemHandlers()) {
        		Object hInstance = getInstanceFromModel(model, getRuntimeManager().getEnvironment().getClassLoader(), params);
        		if (hInstance != null) {
        			if (hInstance instanceof RuntimeEngineAware) {
        				RuntimeEngineAware reaware = (RuntimeEngineAware) hInstance;
	        			reaware.setRuntimeManager(((RuntimeEngineImpl) runtime).getManager());
        			}
        			defaultHandlers.put(model.getName(), (WorkItemHandler) hInstance);
        		}
        	}
        }
        if (!defaultHandlers.containsKey(HUMAN_TASK)) {
        	LocalHTWorkItemHandler humanTaskHandler = new LocalHTWorkItemHandler();
        	humanTaskHandler.setRuntimeManager(((RuntimeEngineImpl) runtime).getManager());
        	defaultHandlers.put(HUMAN_TASK, humanTaskHandler);
        }
        return defaultHandlers;
	}

	@Override
    protected Map<String, Object> getParametersMap(RuntimeEngine runtime) {
        RuntimeManager manager = ((RuntimeEngineImpl)runtime).getManager();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ksession", runtime.getKieSession());
        try {
            parameters.put("taskService", runtime.getTaskService());
        } catch (UnsupportedOperationException e) {
            // in case task service was not configured
        }
        parameters.put("runtimeManager", manager);
        parameters.put("classLoader", getRuntimeManager().getEnvironment().getClassLoader());
        return parameters;
    }

	
	@Override
	public List<ProcessEventListener> getProcessEventListeners(RuntimeEngine runtime) {
		List<ProcessEventListener> listeners = getEventListenerFromDescriptor(runtime, ProcessEventListener.class);
		if (logExecution) {
			listeners.add(new DebugProcessEventListener());
		}
		return listeners;
	}
	
	@Override @SuppressWarnings("unchecked")
	protected <T> List<T> getEventListenerFromDescriptor(RuntimeEngine runtime, Class<T> type) {
		List<T> listeners = new ArrayList<>();
        if (descriptor != null) {
        	Map<String, Object> params = getParametersMap(runtime);
        	for (ObjectModel model : descriptor.getEventListeners()) {
        		Object listenerInstance = getInstanceFromModel(model, getRuntimeManager().getEnvironment().getClassLoader(), params);
        		if (listenerInstance != null && type.isAssignableFrom(listenerInstance.getClass())) {
        			listeners.add((T) listenerInstance);
        		} else {
        		    // close/cleanup instance as it is not going to be used at the moment, except these that are cacheable
        		    if (listenerInstance instanceof Closeable && !(listenerInstance instanceof Cacheable)) {
        		        ((Closeable) listenerInstance).close();
        		    }
        		}
        		setManager(listenerInstance, runtime);
        	}
        }
        return listeners;
	}
	
	private void setManager(Object listenerInstance, RuntimeEngine runtime) {
		if (listenerInstance instanceof RuntimeEngineAware) {
			((RuntimeEngineAware) listenerInstance).setRuntimeManager(((RuntimeEngineImpl) runtime).getManager());
		}
	}
	

	@Override
	public List<AgendaEventListener> getAgendaEventListeners(RuntimeEngine runtime) {
		List<AgendaEventListener> listeners = getEventListenerFromDescriptor(runtime, AgendaEventListener.class);
		if (logExecution) {
			listeners.add(new DebugAgendaEventListener());
		}
		return listeners;
	}

	@Override
	public List<RuleRuntimeEventListener> getRuleRuntimeEventListeners(RuntimeEngine runtime) {
		List<RuleRuntimeEventListener> listeners = getEventListenerFromDescriptor(runtime, RuleRuntimeEventListener.class);
		if (logExecution) {
			listeners.add(new DebugRuleRuntimeEventListener());
		}
		return listeners;
	}

	@Override
	public Map<String, Object> getGlobals(RuntimeEngine runtime) {
    	Map<String, Object> globals = new HashMap<>();
        if (descriptor != null) {
        	Map<String, Object> params = getParametersMap(runtime);
        	for (NamedObjectModel model : descriptor.getGlobals()) {
        		Object gInstance = getInstanceFromModel(model, getRuntimeManager().getEnvironment().getClassLoader(), params);
        		if (gInstance != null) {
            		if (gInstance instanceof RuntimeEngineAware) {
            			((RuntimeEngineAware) gInstance).setRuntimeManager(((RuntimeEngineImpl) runtime).getManager());
            		}
        			globals.put(model.getName(), gInstance);
        		}
        	}
        }
        if (hasGlobal("logger", runtime.getKieSession().getKieBase())) {
        	globals.put("logger", new CDSLoggerImpl(runtimeManager.getIdentifier()));
        }
        return globals;
	}

	private boolean hasGlobal(String varName, KieBase kieBase) {
		return kieBase.getKiePackages().stream().anyMatch(
				kpkg -> kpkg.getGlobalVariables().stream().anyMatch(
						g -> varName.equals(g.getName())
				)
		);
	}

	@Override
	public List<TaskLifeCycleEventListener> getTaskListeners() {
    	List<TaskLifeCycleEventListener> defaultListeners = new ArrayList<>();
        if (descriptor != null) {
        	Map<String, Object> params = new HashMap<>();
        	params.put("runtimeManager", getRuntimeManager());
        	params.put("classLoader", getRuntimeManager().getEnvironment().getClassLoader());
        	params.put("kieContainer", getRuntimeManager().getKieContainer());
        	for (ObjectModel model : descriptor.getTaskEventListeners()) {
        		Object taskListener = getInstanceFromModel(model, getRuntimeManager().getEnvironment().getClassLoader(), params);
        		if (taskListener != null) {
        			defaultListeners.add((TaskLifeCycleEventListener) taskListener);
        		}
        	}
        }
        return defaultListeners;
	}
	
	protected DeploymentDescriptor getDescriptor() {
		return descriptor;
	}
}
