package io.elimu.a2d2.cql;

import java.util.Map;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

public class CqlEngineWIHTest {

	@Test
	public void testCql() {
		String cql = "library SimpleR4Library\n"
				+ "\n"
				+ "using FHIR version '4.0.1'\n"
				+ "\n"
				+ "context Patient\n"
				+ "\n"
				+ "define simpleBooleanExpression: true\n"
				+ "\n"
				+ "define observationRetrieve: [Observation]\n"
				+ "\n"
				+ "define observationHasCode: not IsNull(([Observation]).code)\n"
				+ "\n"
				+ "define \"Initial Population\": observationHasCode\n"
				+ "\n"
				+ "define \"Denominator\": \"Initial Population\"\n"
				+ "\n"
				+ "define \"Numerator\": \"Denominator\"";
		CqlEngineWorkItemHandler handler = new CqlEngineWorkItemHandler();
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setParameter("cql", cql);
		Observation obs1 = new Observation();
		obs1.setCode(new CodeableConcept().setText("obs1"));
		Observation obs2 = new Observation();
		obs2.setCode(new CodeableConcept().setText("obs2"));
		Observation obs3 = new Observation();
		Patient patient = new Patient();
		workItem.setParameter("cql_param_obs1",  obs1);
		workItem.setParameter("cql_param_obs2",  obs2);
		workItem.setParameter("cql_param_obs3",  obs3);
		workItem.setParameter("cql_param_patient",  patient);

		handler.executeWorkItem(workItem, new WorkItemManager() {
			@Override public void abortWorkItem(long id) { }
			@Override public void completeWorkItem(long id, Map<String, Object> results) { }
			@Override public void registerWorkItemHandler(String workItemName, WorkItemHandler handler) { }
		});
		Assert.assertNotNull(workItem.getResult("Numerator"));
	}
}
