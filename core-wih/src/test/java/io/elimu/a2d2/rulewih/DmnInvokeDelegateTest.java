package io.elimu.a2d2.rulewih;

import java.net.URL;
import java.util.Map;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import io.elimu.a2d2.genericmodel.NamedDataObject;

public class DmnInvokeDelegateTest {

	public static KieSession initKieSession(URL dmnFile) {
		KieServices ks = KieServices.get();
		KieFileSystem kfs = ks.newKieFileSystem();
		kfs.write("src/main/resources/myDMN.dmn", ks.getResources().newUrlResource(dmnFile));
		KieBuilder kbuilder = ks.newKieBuilder(kfs);
		kbuilder.buildAll();
		if (kbuilder.getResults().hasMessages(Message.Level.ERROR)) {
			throw new IllegalStateException("Cannot compile DMN into kbase: " + kbuilder.getResults());
		}
		KieContainer kc = ks.newKieContainer(kbuilder.getKieModule().getReleaseId());
		KieBase kieBase = kc.getKieBase();
		KieSession ksession = kieBase.newKieSession();
		return ksession;
	}
	
	@Test
	public void testAllDmnResults() {
		WorkItemImpl item = new WorkItemImpl();
		item.setParameter("dmninput_NDO_name", new NamedDataObject("name", "Peter"));
		KieSession ksession = initKieSession(getClass().getResource("/myDMN.dmn"));
		DmnInvokeDelegate delegate = new DmnInvokeDelegate(ksession);
		delegate.executeWorkItem(item, ksession.getWorkItemManager());
		Map<String, Object> results = item.getResults();
		Assert.assertNotNull(results);
		Assert.assertTrue(results.containsKey("result_namedAndAlive"));
		Assert.assertTrue(results.containsKey("result_has valid name"));
		Assert.assertEquals(true, results.get("result_has valid name"));
	}

	@Test
	public void testOneDmnResultByName() {
		WorkItemImpl item = new WorkItemImpl();
		item.setParameter("dmninput_NDO_name", new NamedDataObject("name", "Peter"));
		item.setParameter("decisionNames", "has valid name");
		KieSession ksession = initKieSession(getClass().getResource("/myDMN.dmn"));
		DmnInvokeDelegate delegate = new DmnInvokeDelegate(ksession);
		delegate.executeWorkItem(item, ksession.getWorkItemManager());
		Map<String, Object> results = item.getResults();
		Assert.assertNotNull(results);
		Assert.assertFalse(results.containsKey("result_namedAndAlive"));
		Assert.assertTrue(results.containsKey("result_has valid name"));
		Assert.assertEquals(true, results.get("result_has valid name"));
	}
	
	@Test
	public void testOneDmnResultByNameAndRightModel() {
		WorkItemImpl item = new WorkItemImpl();
		item.setParameter("dmninput_NDO_name", new NamedDataObject("name", "Peter"));
		item.setParameter("decisionNames", "has valid name");
		item.setParameter("modelNamespace", "https://kiegroup.org/dmn/_7A79D5E5-0C18-48D6-9349-8A72A20E0B05");
		item.setParameter("modelName", "myDMN");
		KieSession ksession = initKieSession(getClass().getResource("/myDMN.dmn"));
		DmnInvokeDelegate delegate = new DmnInvokeDelegate(ksession);
		delegate.executeWorkItem(item, ksession.getWorkItemManager());
		Map<String, Object> results = item.getResults();
		Assert.assertNotNull(results);
		Assert.assertFalse(results.containsKey("result_namedAndAlive"));
		Assert.assertTrue(results.containsKey("result_has valid name"));
		Assert.assertEquals(true, results.get("result_has valid name"));
	}

	@Test
	public void testOneDmnResultByNameAndWrongModel() {
		WorkItemImpl item = new WorkItemImpl();
		item.setParameter("dmninput_NDO_name", new NamedDataObject("name", "Peter"));
		item.setParameter("decisionNames", "has valid name");
		item.setParameter("modelNamespace", "https://kiegroup.org/dmn/_7A79D5E5-0C18-48D6-9349-8A72A20E0B05");
		item.setParameter("modelName", "myDMNWrongName");
		KieSession ksession = initKieSession(getClass().getResource("/myDMN.dmn"));
		DmnInvokeDelegate delegate = new DmnInvokeDelegate(ksession);
		delegate.executeWorkItem(item, ksession.getWorkItemManager());
		Map<String, Object> results = item.getResults();
		Assert.assertNotNull(results);
		Assert.assertFalse(results.containsKey("result_namedAndAlive"));
		Assert.assertFalse(results.containsKey("result_has valid name"));
		Assert.assertTrue(results.containsKey("exception"));
		Exception e = (Exception) results.get("exception");
		Assert.assertEquals("A model is required", e.getMessage());
	}
	
}
