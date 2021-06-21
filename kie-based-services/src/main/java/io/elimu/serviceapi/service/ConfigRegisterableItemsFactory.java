package io.elimu.serviceapi.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.drools.core.event.DebugAgendaEventListener;
import org.drools.core.event.DebugProcessEventListener;
import org.drools.core.event.DebugRuleRuntimeEventListener;
import org.drools.mvel.SafeMVELEvaluator;
import org.jbpm.runtime.manager.impl.RuntimeEngineImpl;
import org.jbpm.services.task.wih.LocalHTWorkItemHandler;
import org.kie.api.KieBase;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.task.TaskLifeCycleEventListener;
import org.kie.internal.runtime.Cacheable;
import org.kie.internal.runtime.Closeable;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.conf.NamedObjectModel;
import org.kie.internal.runtime.conf.ObjectModel;
import org.kie.internal.security.KiePolicyHelper;
import org.kie.soup.project.datamodel.commons.util.MVELEvaluator;
import org.kie.soup.project.datamodel.commons.util.RawMVELEvaluator;
import org.mvel2.MVEL;
import org.mvel2.ParserConfiguration;
import org.mvel2.ParserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigRegisterableItemsFactory extends BasicRegisterableItemsFactory {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigRegisterableItemsFactory.class);
	
	private final Properties config;
	private final String serviceId;
	private final KieContainer kcontainer;
	
	public ConfigRegisterableItemsFactory(KieContainer kcontainer, String serviceId, DeploymentDescriptor descriptor, boolean logExecution, Properties props) {
		super(descriptor, logExecution);
		this.kcontainer = kcontainer;
		this.serviceId = serviceId;
		this.config = props;
	}

	
	@Override
	public Map<String, WorkItemHandler> getWorkItemHandlers(RuntimeEngine runtime) {
		Map<String, WorkItemHandler> defaultHandlers = new HashMap<>();
        if (getDescriptor() != null) {
        	Map<String, Object> params = getParametersMap(runtime);
        	for (NamedObjectModel model : getDescriptor().getWorkItemHandlers()) {
        		String value = getConfigProperty(serviceId, "workItemHandlers." + model.getName());
        		Object hInstance = null;
        		if (value != null) {
        			hInstance = getCloudConfigModel(value, params);
        		} else { 
        			hInstance = getInstanceFromModel(model, getRuntimeManager().getEnvironment().getClassLoader(), params);
        		}
        		if (hInstance instanceof RuntimeEngineAware) {
        			RuntimeEngineAware reaware = (RuntimeEngineAware) hInstance;
                		reaware.setRuntimeManager(((RuntimeEngineImpl) runtime).getManager());
        		}
    			if (hInstance != null) {
    				defaultHandlers.put(model.getName(), (WorkItemHandler) hInstance);
    			}

        	}
        }
        if (!defaultHandlers.containsKey("Human Task")) {
        	LocalHTWorkItemHandler humanTaskHandler = new LocalHTWorkItemHandler();
        	humanTaskHandler.setRuntimeManager(((RuntimeEngineImpl) runtime).getManager());
        	defaultHandlers.put("Human Task", humanTaskHandler);
        }
        return defaultHandlers;
	}

	private Object getCloudConfigModel(String value, Map<String, Object> params) {
		ParserConfiguration configuration = new ParserConfiguration();
		configuration.setClassLoader(getRuntimeManager().getEnvironment().getClassLoader());
		ParserContext ctx = new ParserContext(configuration);
		if (params != null) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				ctx.addVariable(entry.getKey(), entry.getValue().getClass());
			}
		}
		Object compiledExpression = MVEL.compileExpression(value, ctx);
		 
		return MVELEvaluatorHolder.evaluator.executeExpression( compiledExpression, params );
	}
	
	private static class MVELEvaluatorHolder {
		static MVELEvaluator evaluator = KiePolicyHelper.isPolicyEnabled() ? 
				new SafeMVELEvaluator() :
	            new RawMVELEvaluator(); 
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
		List<T> listeners = new ArrayList<T>();
        if (getDescriptor() != null) {
        	Map<String, Object> params = getParametersMap(runtime);
    		String value = getConfigProperty(serviceId, "eventListeners");
    		if (value == null) {
    			for (ObjectModel model : getDescriptor().getEventListeners()) {
    				Object listenerInstance = getInstanceFromModel(model, getRuntimeManager().getEnvironment().getClassLoader(), params);
    				configListenerInstance(listenerInstance, runtime, listeners, type);
    			}
    		} else {
    			String[] listenersString = value.split(";");
    			for (String listener : listenersString) {
    				Object listenerInstance = getCloudConfigModel(listener, params);
    				if (listenerInstance != null && type.isAssignableFrom(listenerInstance.getClass())) {
    					listeners.add((T) listenerInstance);
    					setManager(listenerInstance, runtime);
    				}
    			}
    		}
        }
        return listeners;
	}
	
	@SuppressWarnings("unchecked")
	private <T> void configListenerInstance(Object listenerInstance, RuntimeEngine runtime, List<T> listeners, Class<T> type) {
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
	
	private void setManager(Object listenerInstance, RuntimeEngine runtime) {
		if (listenerInstance instanceof RuntimeEngineAware) {
			((RuntimeEngineAware) listenerInstance).setRuntimeManager(((RuntimeEngineImpl) runtime).getManager());
		}
	}

	private String getConfigProperty(String serviceId, String suffix) {
		String value = null;
		String customKey = "services." + serviceId + "." + suffix;
		String defaultKey = "services.default-structure-allservices." + suffix;
		try {
			if (config != null && config.containsKey(customKey)) {
				value = config.getProperty(customKey);
			} else if (config != null && config.containsKey(defaultKey)) {
				value = config.getProperty(defaultKey);
			}
		} catch (Exception e) { 
			LOG.warn("Couldn't locate property " + customKey + " or " + defaultKey + " on remote config. Carrying forward with pre-existing values");
		}
		return value;
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
        if (getDescriptor() != null) {
        	Map<String, Object> params = getParametersMap(runtime);
        	for (NamedObjectModel model : getDescriptor().getGlobals()) {
        		String value = getConfigProperty(serviceId, "globals." + model.getName());
        		Object gInstance = null;
        		if (value == null) {
        			gInstance = getInstanceFromModel(model, getRuntimeManager().getEnvironment().getClassLoader(), params);
        		} else {
        			gInstance = getCloudConfigModel(value, params);
        		}
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
		if (getDescriptor() != null) {
			ClassLoader cl = this.kcontainer.getClassLoader();
			if (cl == null) {
				cl = getRuntimeManager().getEnvironment().getClassLoader();
			}
			KieContainer kc = getRuntimeManager().getKieContainer();
			if (kc == null) {
				kc = this.kcontainer;
			}
			Map<String, Object> params = new HashMap<>();
			params.put("runtimeManager", getRuntimeManager());
			params.put("classLoader", cl);
			params.put("kieContainer", kc);
			Optional<Object> taskListener = createTaskListener(cl, params);
			
			if (taskListener.isPresent()) {
				defaultListeners.add((TaskLifeCycleEventListener) taskListener.get());
			}
		}
		return defaultListeners;
	}
	
	private Optional<Object> createTaskListener(ClassLoader cl, Map<String, Object> params) {
		String value = getConfigProperty(serviceId, "taskEventListeners");
		Object taskListener = null;
		if (value == null) {
			for (ObjectModel model : getDescriptor().getTaskEventListeners()) {
				taskListener = getInstanceFromModel(model, cl, params);
			}
		} else {
			String[] listenersString = value.split(";");
			for (String listener : listenersString) {
				taskListener = getCloudConfigModel(listener, params);
			}
		}
		return Optional.ofNullable(taskListener);
	}

}
