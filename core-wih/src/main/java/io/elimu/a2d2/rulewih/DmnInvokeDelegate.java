package io.elimu.a2d2.rulewih;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.kie.api.KieBase;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.dmn.api.core.DMNMessage;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.core.api.DMNFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.elimu.a2d2.exception.WorkItemHandlerException;

public class DmnInvokeDelegate implements WorkItemHandler {

	private static final Logger LOG = LoggerFactory.getLogger(DmnInvokeDelegate.class);
	private final KieBase kbase;
	private Environment env;

	public DmnInvokeDelegate(KieSession ksession) {
		super();
		this.kbase = ksession.getKieBase();
		this.env = ksession.getEnvironment();
	}

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		Map<String, Object> results = workItem.getResults();
		KieSession ksession = kbase.newKieSession(null, env);
		try {
			DMNRuntime runtime = ksession.getKieRuntime(DMNRuntime.class);
			DMNContext context = DMNFactory.newContext();
			for (Map.Entry<String, Object> entry : workItem.getParameters().entrySet()) {
				if (entry.getKey().startsWith("dmninput_")) {
					context.set(entry.getKey().replace("dmninput_", ""), entry.getValue());
				}
			}
			DMNModel model = null;
			if (workItem.getParameter("modelNamespace") != null && workItem.getParameter("modelName") != null) {
				String namespace = (String) workItem.getParameter("modelNamespace");
				String modelName = (String) workItem.getParameter("modelName");
				model = runtime.getModel(namespace, modelName);
			} else {
				model = runtime.getModels().isEmpty() ? null : runtime.getModels().get(0);
			}
			if (model == null) {
				throw new WorkItemHandlerException("A model is required");
			}
			DMNResult result = null;
			List<String> decisionNames = new ArrayList<>();
			if (workItem.getParameter("decisionNames") != null) {
				String[] names = ((String) workItem.getParameter("decisionNames")).split(",");
				decisionNames = Arrays.asList(names);
				result = runtime.evaluateByName(model, context, names);
			} else {
				result = runtime.evaluateAll(model, context);
			}
			for (DMNDecisionResult dmnres : result.getDecisionResults()) {
				if (!decisionNames.isEmpty() && decisionNames.contains(dmnres.getDecisionName())) {
					LOG.info("Registering result_{} with value {}", dmnres.getDecisionName(), dmnres.getResult());
					results.put("result_" + dmnres.getDecisionName(), dmnres.getResult());
				} else if (decisionNames.isEmpty()) {
					LOG.info("Registering result_{} with value {}", dmnres.getDecisionName(), dmnres.getResult());
					results.put("result_" + dmnres.getDecisionName(), dmnres.getResult());
				}
			}
			results.put("hasErrors", result.hasErrors());
			for (DMNMessage message : result.getMessages()) {
				LOG.info("Message from executing DMN: {}", message.toString());
			}
			results.put("messages", result.getMessages().toString());
			manager.completeWorkItem(workItem.getId(), results);
		} catch (Exception e) {
			results.put("exception", e);
			manager.completeWorkItem(workItem.getId(), results);
		} finally {
			ksession.dispose();
		}
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// do nothing
	}

}
