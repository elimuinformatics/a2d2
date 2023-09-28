// Copyright 2018-2020 Elimu Informatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.elimu.a2d2.rulewih;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.drools.core.common.InternalAgenda;
import org.kie.api.KieBase;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.elimu.a2d2.exception.WorkItemHandlerException;
import io.elimu.a2d2.genericmodel.NamedDataObject;

public class RuleInvokeDelegate implements WorkItemHandler {

	private static final Logger LOG = LoggerFactory.getLogger(RuleInvokeDelegate.class);
	private final KieBase kbase;
	private final KieSession ksession;
	private Environment env;

	public RuleInvokeDelegate(KieSession ksession) {
		super();
		this.kbase = ksession.getKieBase();
		this.ksession = ksession;
		this.env = ksession.getEnvironment();
	}

	private List<Object> extractListeners(KieSession ksession) {
		List<Object> listeners = new LinkedList<>();
		for (AgendaEventListener listener : ksession.getAgendaEventListeners()) {
			if (listener.getClass().getName().endsWith("GenericDebugListener")) {
				listeners.add(listener);
			} else if (listener.getClass().getName().endsWith("MixPanelEventListener")) {
				listeners.add(listener);
			}
		}
		return listeners;
	}

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		Map<String, Object> facts = new HashMap<>();
		workItem.getParameters().keySet().stream().
				filter(key -> key.startsWith("fact_")).
				forEach(key -> facts.put(key.substring(5), workItem.getParameter(key)));
		workItem.getParameters().keySet().stream().
				filter(key -> key.startsWith("namedfact_")).
				forEach(key -> facts.put(key.substring(10),
						new NamedDataObject(key.substring(10), workItem.getParameter(key))));
		String ruleFlowGroup = (String) workItem.getParameter("ruleFlowGroup");
		String agendaGroup = (String) workItem.getParameter("agendaGroup");
		String globalVar = (String) workItem.getParameter("inferencesGlobalVariableName");
		String sRuleLimit = (String) workItem.getParameter("ruleLimit");
		Integer ruleLimit = null;
		if (sRuleLimit != null) {
			ruleLimit = Integer.valueOf(sRuleLimit);
		}
		if (ruleLimit == null) {
			ruleLimit = 100000;
		} else if (ruleLimit <= 0) {
			ruleLimit = null;
		}
		if (ruleFlowGroup != null && agendaGroup != null) {
			LOG.error("Cannot pass both ruleFlowGroup and agendaGroup pararameters");
			throw new WorkItemHandlerException("Cannot pass both ruleFlowGroup and agendaGroup pararameters");
		}
		if (globalVar == null) {
			globalVar = "inferences";
		}
		
		KieSession ksession = kbase.newKieSession(null, env);
		List<Object> listeners = extractListeners(this.ksession);
		if (listeners != null) {
			for (Object listener : listeners) {
				if (listener instanceof AgendaEventListener) {
					ksession.addEventListener((AgendaEventListener) listener);
				}
				if (listener instanceof RuleRuntimeEventListener) {
					ksession.addEventListener((RuleRuntimeEventListener) listener);
				}
			}
		}
		facts.values().forEach(object -> ksession.insert(object));
		if (agendaGroup != null) {
			ksession.getAgenda().getAgendaGroup(agendaGroup).setFocus();
		}
		if (ruleFlowGroup != null) {
			((InternalAgenda) ksession.getAgenda()).activateRuleFlowGroup(ruleFlowGroup);
		}
		try {
			Map<String, Object> inferences = new HashMap<>();
			ksession.setGlobal(globalVar, inferences);
			long rulesFired = 0;
			if (ruleLimit > 0) {
				rulesFired = ksession.fireAllRules(ruleLimit);
				if (rulesFired == ruleLimit) {
					LOG.warn("Rule firing ended due to rule limit (" + ruleLimit + " rules) rather than completion. Possible infinite loop in rules");
				}
			} else {
				ksession.fireAllRules();
			}
			LOG.debug("After rule firing, inferences has " + inferences.size() + " elements");
			Map<String, Object> results = workItem.getResults();
			for (Map.Entry<String, Object> entry : inferences.entrySet()) {
				if (entry.getValue() != null) {
					LOG.debug("Adding to the results [inference_" + entry.getKey() + " with object of type " + entry.getValue().getClass().getName() +"]");
				} else {
					LOG.debug("Adding to the results [inference_" + entry.getKey() + " with NULL]");
				}
				results.put("inference_" + entry.getKey(), entry.getValue());
			}
			inferences.forEach((key, value) -> results.put("inference_" + key, value));
			manager.completeWorkItem(workItem.getId(), results);
			LOG.trace("RuleInvoke Delegate is completed");
		} catch (Exception e) {
			LOG.error("Problem invoking rules", e);
			throw new WorkItemHandlerException("Problem invoking rules", e);
		} finally {
			ksession.dispose();
		}
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// not needed. Sync task
	}
}
