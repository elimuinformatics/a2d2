package io.elimu.genericapi.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.drools.core.ClassObjectFilter;
import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.drools.persistence.PersistableRunner;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.elimu.a2d2.cdsmodel.Dependency;
import io.elimu.a2d2.genericmodel.ServiceRequest;
import io.elimu.a2d2.genericmodel.ServiceResponse;
import io.elimu.a2d2.process.ServiceUtils;
import io.elimu.genericapi.plugins.ModulePluginLoader;
import io.elimu.genericapi.scrubbers.ScrubberLocator;
import io.elimu.serviceapi.config.ConfigAPIProcessVariableInitHelper;
import io.elimu.serviceapi.service.AbstractKieService;
import io.elimu.serviceapi.service.AppContextUtils;

public class GenericKieBasedService extends AbstractKieService implements GenericService {

	private static final String NOT_ALLOWED_METHOD_KEY = "____NOT_ALLOWED";
	private static final Logger LOG = LoggerFactory.getLogger(GenericKieBasedService.class);
	private static final String SERVICECATEGORY = "serviceCategory";
	private static final String KIE_PROJECT_PROCESSID = "kie.project.processid.";
	private static final List<String> EXISTING_METHODS = Arrays.asList("GET", "HEAD", "POST", "PUT", "DELETE", "CONNECT", "OPTIONS", "TRACE");

	private String processId;
	private boolean logExecution = false;
	private Properties config;
	private JSONObject discoveryDsl;
	private List<String> otherCustomers;;
	
	public JSONObject getDiscoveryDsl() {
		return discoveryDsl;
	}

	public void setDiscoveryDsl(JSONObject discoveryDsl) {
		this.discoveryDsl = discoveryDsl;
	}

	public GenericKieBasedService() {
	}
	
	public GenericKieBasedService(String releaseId, String defaultCustomer) {
		super(releaseId, defaultCustomer);
	}
	
	public GenericKieBasedService(Dependency dep) {
		super(dep);
	}

	public void stop() {
		super.stop();
		this.processId = null;
	}
	
	@Override
	protected void init() {
		super.init();
		if (getDefaultCustomer() == null) {
			setDefaultCustomer(RunningServices.getInstance().getSpace(getDependency()));
		}
		String appConfigFile = System.getProperty("kie.apps.config.folder", 
				ServiceUtils.getDefaultConfigFolder()) + "/" + getId() + ".properties";
		try {
			this.config = ServiceUtils.getConfig(getDependency(), AppContextUtils.getInstance().getProfileName());
			if (this.config.containsKey(SERVICECATEGORY)) {
				setServiceCategory(this.config.getProperty(SERVICECATEGORY).trim());
			}
		} catch (IOException e) {
			throw new GenericServiceConfigException("File " + appConfigFile + " found, but couldn't be read. Service " + getId() + " not loaded.", e);
		}
		URL jarPath = ServiceUtils.toJarPath(getDependency());
		//priority 1: localized service properties file
		//configure a system property pointing to the server's config folder
		InputStream discoveryEntry = ServiceUtils.readEntry(jarPath, "discovery.dsl");
		if (discoveryEntry != null) {
			try {
				this.discoveryDsl = (JSONObject) new JSONParser().parse(new InputStreamReader(discoveryEntry, "UTF-8"));
			} catch (IOException | ParseException e) {
				LOG.error("Exception in parsing discoveryEntry", e);
			}
		}
		this.processId = extractProcessId(jarPath, config, "kie.project.processId");
		this.logExecution = Boolean.valueOf(config.getProperty("kie.project.logexec", "false").toLowerCase());
		String pluginEntries = config.getProperty("kie.project.module.plugins", "FtlLoader");
		if (pluginEntries != null) {
			for (String plugin : pluginEntries.split(",")) {
				ModulePluginLoader.get(getClassLoader(), plugin).process(getDependency());
			}
		}
		//init other customers
		if (config.getProperty("kie.project.space") != null) {
			setOtherCustomers(Arrays.asList(config.getProperty("kie.project.space").split(",")));
		}
	}


	@Override
	public ServiceResponse execute(ServiceRequest request) throws GenericServiceException {
		if (!ignoreScrubbing()) {
			ScrubberLocator.getInstance().getScrubber(request).scrub(request);
		}
		RuntimeEngine runtime = null;
		ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(getKieContainer().getClassLoader());
			runtime = getManager().getRuntimeEngine(ProcessInstanceIdContext.get());
			KieSession ksession = runtime.getKieSession();
			GenericDebugListener listener = enableDebugging(ksession, request);
			enableMixpanel(ksession);
			String procId = getProcessId(request.getMethod());
			if (procId != null && !NOT_ALLOWED_METHOD_KEY.equals(procId)) {
				LOG.info("Executing process " + procId + " for service " + getId());
				Map<String, Object> params = new HashMap<>();
				try {
					params.put("serviceRequest", request);
					params.put("serviceResponse", defaultResponse());
					params.put("defaultCustomer", getDefaultCustomer());
	                                String providedClient = getClient(request);
	                                params.put("configApiClient", providedClient);
	                                params.putAll(new ConfigAPIProcessVariableInitHelper().initVariables(request, providedClient, getDependency(), getConfig()));
				}catch(TimeoutException e) {
					throw e;
				}
				WorkflowProcessInstance instance = (WorkflowProcessInstance) ksession.startProcess(procId, params);
				ServiceResponse response = (ServiceResponse) instance.getVariable("serviceResponse");
				if (response == null) {
					return new ServiceResponse(appendListenerOutput(request, listener, "There were 0 ServiceResponse objects obtained from execution."), 400);
				} else {
					response.setBody(appendListenerOutput(request, listener, response.getBody()));
				}
				return response;
			} else if (!NOT_ALLOWED_METHOD_KEY.equals(procId)) {
				LOG.info("Executing rules directly for service " + getId());
				ksession.insert(request);
				ksession.fireAllRules();
				Collection<?> retvals = ksession.getObjects(new ClassObjectFilter(ServiceResponse.class));
				if (retvals.size() == 1) {
					ServiceResponse retval = (ServiceResponse) retvals.iterator().next();
					retval.setBody(appendListenerOutput(request, listener, retval.getBody()));
					return retval;
				} else {
					return new ServiceResponse(appendListenerOutput(request, listener, "There were " + retvals.size() + " ServiceResponse objects obtained from execution. Only 1 is allowed"), 400);
				}
			} else {
				return new ServiceResponse(appendListenerOutput(request, listener, "Method " + request.getMethod() + " not allowed"), 405);
			}
		} catch (Exception e) {
			LOG.error("Problem executing service", e);
			throw new GenericServiceException(
					"The execution of the service encountered an error of type " 
							+ e.getClass().getSimpleName() + " and stopped. Further"
							+ " information can be found in the server logs. Contact "
							+ "your system administrator.", e);
		} finally {
			cleanup(runtime, oldCL);
		}
	}
	
	private String getClient(ServiceRequest request) {
		String auth = request.getHeader("authorization");
		if (auth == null) {
			if ("*".equals(getDefaultCustomer())) {
				return "default";
			}
			return getDefaultCustomer();
		}
		try {
			String token = auth.replace("Bearer ", "");
			String tokenClaim = token.split("\\.")[1];
			tokenClaim = new String(Base64.getDecoder().decode(tokenClaim.getBytes()));
			JsonNode node = new ObjectMapper().readTree(tokenClaim.getBytes());
			return node.has("tenant") ? node.get("tenant").asText() : getDefaultCustomer();
		} catch (IOException | NullPointerException e) {
			LOG.warn("Couldn't extract tenant from bearer token: {}", e.getMessage());
			return getDefaultCustomer();
		}
	}

	private String appendListenerOutput(ServiceRequest request, GenericDebugListener listener, String body) {
		if ("true".equalsIgnoreCase(request.getHeader("x-output-debug"))) {
			return listener.toJson(body); //outputs JSON with the listener collected data
		}
		return body;
	}

	private void enableMixpanel(KieSession ksession) {
		MixPanelEventListener listener = new MixPanelEventListener(getConfig());
		ksession.addEventListener((AgendaEventListener) listener);
		ksession.addEventListener((ProcessEventListener) listener);
		ksession.addEventListener((RuleRuntimeEventListener) listener);
	}
	
	private GenericDebugListener enableDebugging(KieSession ksession, ServiceRequest request) {
		String defaultEnabled = System.getProperty("service.debug.enabled", "false");
		String header = request.getHeader("x-enable-debug");
		String header2 = request.getHeader("x-output-debug"); 
		if ("true".equalsIgnoreCase(defaultEnabled) || (header != null && "true".equalsIgnoreCase(header)) || (header2 != null && "true".equalsIgnoreCase(header2))) {
			//enables listeners
			return new GenericDebugListener(getId(), ksession, "true".equalsIgnoreCase(header2));
		}
		return null;
	}

	private boolean ignoreScrubbing() {
		Predicate<? super Object> f = k -> k.toString().equalsIgnoreCase("kie.project.ignorescrub"); 
		Optional<Object> ignorescrub = config.keySet().stream().filter(f).findFirst();
		if (ignorescrub.isPresent()) {
			return "true".equalsIgnoreCase(config.getProperty(ignorescrub.get().toString()));
		}
		return false;
	}
	
	private String getProcessId(String method) {
		if (method == null) {
			return processId;
		}
		Optional<Object> kieProcessId = config.keySet().stream().
				filter(k -> k.toString().toLowerCase().startsWith(KIE_PROJECT_PROCESSID + method.toLowerCase())).findFirst();
		if (kieProcessId.isPresent()) {
			return config.getProperty(kieProcessId.get().toString());
		} else if (config.keySet().stream().
				filter(k -> k.toString().toLowerCase().startsWith(KIE_PROJECT_PROCESSID)).count() > 0) {
			return NOT_ALLOWED_METHOD_KEY;
		} else {
			return processId;
		}
	}

	private void cleanup(RuntimeEngine runtime, ClassLoader oldCL) {
		if (runtime == null) {
			return;
		}
		KieSession ksession = runtime.getKieSession();
		if (ksession != null) {
			((KnowledgeBaseImpl) getKieBase())
				.disposeStatefulSession(
						asInternalKieSession(ksession));
		}
		
		getManager().disposeRuntimeEngine(runtime);
		
		if (oldCL != null) {
			Thread.currentThread().setContextClassLoader(oldCL);
		}
	}

	private StatefulKnowledgeSessionImpl asInternalKieSession(KieSession ksession) {
		if (ksession instanceof StatefulKnowledgeSessionImpl) {
			return (StatefulKnowledgeSessionImpl) ksession;
		}
		if (ksession instanceof CommandBasedStatefulKnowledgeSession) {
			CommandBasedStatefulKnowledgeSession cmdSession = (CommandBasedStatefulKnowledgeSession) ksession;
			PersistableRunner runner = (PersistableRunner) cmdSession.getRunner();
			return (StatefulKnowledgeSessionImpl) runner.getKieSession();
		} 
		return null;
	}

	private ServiceResponse defaultResponse() {
		return new ServiceResponse("Process for service " + getDependency().getArtifactId() + " is getting started", 200);
	}

	@Override
	protected boolean shouldLogExecution() {
		return logExecution;
	}

	public Properties getConfig() {
		if (this.config == null && getDependency() != null) {
			try {
				this.config = ServiceUtils.getConfig(getDependency(), AppContextUtils.getInstance().getProfileName());
			} catch (Exception e) {
				LOG.warn("COULD NOT INITIALIZE CONFIG ON DEMAND", e);
			}
		}
		return config;
	}
	
	public List<String> getServiceTypes() {
		String pkgNamesString = config.getProperty("cds.based.project.packages");
		if (pkgNamesString == null) {
			return new ArrayList<>();
		}
		return Arrays.asList(pkgNamesString.split(","));
	}
	
	/**
	 * @return if no http method specific entry is provided, serve all cases. Otherwise serve only specified methods
	 */
	public List<String> getAvailableMethods() {
		List<String> values = config.keySet().stream().
				filter(k -> k.toString().toLowerCase().startsWith(KIE_PROJECT_PROCESSID)).
				map(k -> k.toString().toUpperCase().replace(KIE_PROJECT_PROCESSID, "")).collect(Collectors.toList());
		return values == null ? EXISTING_METHODS : values;
	}
	
	@Override
	public void updateTask(Task task) throws GenericServiceException {
		RuntimeEngine runtime = getManager().getRuntimeEngine(ProcessInstanceIdContext.get(task.getTaskData().getProcessInstanceId()));
		TaskService taskService = runtime.getTaskService();
		if (taskService != null) {
			taskService.execute(new InternalUpdateTaskCommand(task));
			if (task.getTaskData().getStatus() == Status.Completed) {
				runtime.getKieSession().getWorkItemManager().completeWorkItem(
						task.getTaskData().getWorkItemId(), 
						task.getTaskData().getTaskOutputVariables());
			} else if (task.getTaskData().getStatus() == Status.Exited) {
				runtime.getKieSession().getWorkItemManager().abortWorkItem(task.getTaskData().getWorkItemId());
			}
		}
	}

	@Override
	public Task getTask(Long taskId) {
		return getManager().getRuntimeEngine(ProcessInstanceIdContext.get()).getTaskService().getTaskById(taskId);
	}

	public void setOtherCustomers(List<String> otherCustomers) {
		this.otherCustomers = otherCustomers;
	}
	
	public List<String> getOtherCustomers() {
		return otherCustomers;
	}
}
