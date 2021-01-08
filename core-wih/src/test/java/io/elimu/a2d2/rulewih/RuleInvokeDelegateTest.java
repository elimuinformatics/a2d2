package io.elimu.a2d2.rulewih;

import java.util.Map;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;

import io.elimu.a2d2.corewih.IncorrectParametersException;
import io.elimu.a2d2.exception.WorkItemHandlerException;

public class RuleInvokeDelegateTest {

	public static KieSession initKieSession(String drl) {
		KieHelper kieHelper = new KieHelper();
		KieBase kieBase = kieHelper.addContent(drl, ResourceType.DRL).build();
		KieSession ksession = kieBase.newKieSession();
		return ksession;
	}

	@Test
	public void testKieSessionWithNoGroup() {
		WorkItemImpl item = new WorkItemImpl();
		item.setParameter("fact_a", "Hello");
		item.setParameter("namedfact_b", "Hello again");
		item.setParameter("inferencesGlobalVariableName", "mystrings");
		//item.setParameter("ruleFlowGroup", "non-existing-group");
		String drl = "package a\n"
				+ "global java.util.Map mystrings;"
				+ "rule \"X\"\n"
				+ "when io.elimu.a2d2.genericmodel.NamedDataObject($key: name, $s: value)\n"
				+ "then mystrings.put($key, $s);\n"
				+ "end\n\n"
				+ "\n"
				+ "rule \"Y\"\n"
				+ "when s: String()\n"
				+ "then mystrings.put(s, s);\n"
				+ "end\n\n";
		KieSession ksession = initKieSession(drl);
		RuleInvokeDelegate delegate = new RuleInvokeDelegate(ksession);
		delegate.executeWorkItem(item, ksession.getWorkItemManager());
		Map<String, Object> results = item.getResults();
		Assert.assertNotNull(results);
		Assert.assertEquals("Hello", results.get("inference_Hello"));
		Assert.assertEquals("Hello again", results.get("inference_b"));
	}

	@Test(expected = WorkItemHandlerException.class)
	public void testRuleInvokeWithWrongGlobal() {
		WorkItemImpl item = new WorkItemImpl();
		item.setParameter("fact_a", "Hello");
		item.setParameter("namedfact_b", "Hello again");
		//item.setParameter("inferencesGlobalVariableName", "mystrings2");
		//item.setParameter("ruleFlowGroup", "non-existing-group");
		String drl = "package a\n"
				+ "global java.util.Map mystrings;"
				+ "rule \"X\"\n"
				+ "when io.elimu.a2d2.genericmodel.NamedDataObject($key: name, $s: value)\n"
				+ "then mystrings.put($key, $s);\n"
				+ "end\n\n"
				+ "\n"
				+ "rule \"Y\"\n"
				+ "when s: String()\n"
				+ "then mystrings.put(s, s);\n"
				+ "end\n\n";
		KieSession ksession = initKieSession(drl);
		RuleInvokeDelegate delegate = new RuleInvokeDelegate(ksession);
		try {
			delegate.executeWorkItem(item, ksession.getWorkItemManager());
			Assert.fail("Should not succeed");
		} catch (RuleEvaluationException e) {
			Assert.assertNotNull(e.getMessage());
		}
	}

	@Test
	public void testRuleInvokeWithDefaultGlobal() {
		WorkItemImpl item = new WorkItemImpl();
		item.setParameter("fact_a", "Hello");
		item.setParameter("namedfact_b", "Hello again");
		//item.setParameter("inferencesGlobalVariableName", "mystrings2");
		//item.setParameter("ruleFlowGroup", "non-existing-group");
		String drl = "package a\n"
				+ "global java.util.Map inferences;"
				+ "rule \"X\"\n"
				+ "when io.elimu.a2d2.genericmodel.NamedDataObject($key: name, $s: value)\n"
				+ "then inferences.put($key, $s);\n"
				+ "end\n\n"
				+ "\n"
				+ "rule \"Y\"\n"
				+ "when s: String()\n"
				+ "then inferences.put(s, s);\n"
				+ "end\n\n";
		KieSession ksession = initKieSession(drl);
		RuleInvokeDelegate delegate = new RuleInvokeDelegate(ksession);
		delegate.executeWorkItem(item, ksession.getWorkItemManager());
		Map<String, Object> results = item.getResults();
		Assert.assertNotNull(results);
		Assert.assertEquals("Hello", results.get("inference_Hello"));
		Assert.assertEquals("Hello again", results.get("inference_b"));
	}

	@Test
	public void testRuleInvokeWithRuleflowGroup() {
		WorkItemImpl item = new WorkItemImpl();
		item.setParameter("fact_a", "Hello");
		item.setParameter("namedfact_b", "Hello again");
		//item.setParameter("inferencesGlobalVariableName", "mystrings2");
		item.setParameter("ruleFlowGroup", "a-group");
		String drl = "package a\n"
				+ "global java.util.Map inferences;"
				+ "rule \"X\"\n"
				+ "ruleflow-group \"a-group\"\n"
				+ "when io.elimu.a2d2.genericmodel.NamedDataObject($key: name, $s: value)\n"
				+ "then inferences.put($key, $s);\n"
				+ "end\n\n"
				+ "\n"
				+ "rule \"Y\"\n"
				+ "ruleflow-group \"other-group\"\n"
				+ "when s: String()\n"
				+ "then inferences.put(s, s);\n"
				+ "end\n\n";
		KieSession ksession = initKieSession(drl);
		RuleInvokeDelegate delegate = new RuleInvokeDelegate(ksession);
		delegate.executeWorkItem(item, ksession.getWorkItemManager());
		Map<String, Object> results = item.getResults();
		Assert.assertNotNull(results);
		Assert.assertNull(results.get("inference_Hello"));
		Assert.assertEquals("Hello again", results.get("inference_b"));
	}

	@Test
	public void testRuleInvokeWithAgendaGroup() {
		WorkItemImpl item = new WorkItemImpl();
		item.setParameter("fact_a", "Hello");
		item.setParameter("namedfact_b", "Hello again");
		//item.setParameter("inferencesGlobalVariableName", "mystrings2");
		item.setParameter("agendaGroup", "a-group");
		String drl = "package a\n"
				+ "global java.util.Map inferences;"
				+ "rule \"X\"\n"
				+ "agenda-group \"a-group\"\n"
				+ "when io.elimu.a2d2.genericmodel.NamedDataObject($key: name, $s: value)\n"
				+ "then inferences.put($key, $s);\n"
				+ "end\n\n"
				+ "\n"
				+ "rule \"Y\"\n"
				+ "agenda-group \"other-group\"\n"
				+ "when s: String()\n"
				+ "then inferences.put(s, s);\n"
				+ "end\n\n";
		KieSession ksession = initKieSession(drl);
		RuleInvokeDelegate delegate = new RuleInvokeDelegate(ksession);
		delegate.executeWorkItem(item, ksession.getWorkItemManager());
		Map<String, Object> results = item.getResults();
		Assert.assertNotNull(results);
		Assert.assertNull(results.get("inference_Hello"));
		Assert.assertEquals("Hello again", results.get("inference_b"));
	}

	@Test(expected = WorkItemHandlerException.class)
	public void testRuleInvokeWithBothGroups() {
		WorkItemImpl item = new WorkItemImpl();
		item.setParameter("fact_a", "Hello");
		item.setParameter("namedfact_b", "Hello again");
		//item.setParameter("inferencesGlobalVariableName", "mystrings2");
		item.setParameter("ruleFlowGroup", "one-group");
		item.setParameter("agendaGroup", "another-group");
		String drl = "package a\n"
				+ "global java.util.Map mystrings;"
				+ "rule \"X\"\n"
				+ "when io.elimu.a2d2.genericmodel.NamedDataObject($key: name, $s: value)\n"
				+ "then mystrings.put($key, $s);\n"
				+ "end\n\n"
				+ "\n"
				+ "rule \"Y\"\n"
				+ "when s: String()\n"
				+ "then mystrings.put(s, s);\n"
				+ "end\n\n";
		KieSession ksession = initKieSession(drl);
		RuleInvokeDelegate delegate = new RuleInvokeDelegate(ksession);
		try {
			delegate.executeWorkItem(item, ksession.getWorkItemManager());
			Assert.fail("Should not succeed");
		} catch (IncorrectParametersException e) {
			Assert.assertNotNull(e.getMessage());
		}
	}

	@Test(expected = WorkItemHandlerException.class)
	public void testRuleInvokeWithNoDrl() {
		WorkItemImpl item = new WorkItemImpl();
		item.setParameter("fact_a", "Hello");
		item.setParameter("namedfact_b", "Hello again");
		KieSession ksession = initKieSession("");
		RuleInvokeDelegate delegate = new RuleInvokeDelegate(ksession);
		try {
			delegate.executeWorkItem(item, ksession.getWorkItemManager());
			Assert.fail("Should not succeed");
		} catch (RuleEvaluationException e) {
			Assert.assertNotNull(e.getMessage());
		}
	}

	@Test
	public void testRuleInvokeWithEmptyRules() {
		WorkItemImpl item = new WorkItemImpl();
		item.setParameter("fact_a", "Hello");
		item.setParameter("namedfact_b", "Hello again");
		KieSession ksession = initKieSession("package b;\n"
				+ "global java.util.Map inferences;\n"
				+ "//no further rules\n");
		RuleInvokeDelegate delegate = new RuleInvokeDelegate(ksession);
		delegate.executeWorkItem(item, ksession.getWorkItemManager());
		Assert.assertTrue(item.getResults().isEmpty());
	}
}
