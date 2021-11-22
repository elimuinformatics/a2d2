package io.elimu.a2d2.smswih;

import java.util.Map;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

public class ValidationTest {

	@Test
	public void testPhoneValidation() {
		String sid = System.getProperty("twilio.sid");
		String auth = System.getProperty("twilio.authtoken");
		if (sid == null || auth == null) {
			return;
		}
		PhoneValidationWorkItemHandler handler = new PhoneValidationWorkItemHandler();
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setParameter("phone", "16313466143");
		handler.executeWorkItem(workItem, new NoOpWorkItemManager());
		Assert.assertNotNull(workItem.getResult("isValid"));
		Assert.assertEquals(Boolean.TRUE, workItem.getResult("isValid"));
		System.out.println("Phone 1 is valid");
		workItem.setParameter("phone", "12035555555");
		handler.executeWorkItem(workItem, new NoOpWorkItemManager());
		Assert.assertNotNull(workItem.getResult("isValid"));
		Assert.assertEquals(Boolean.FALSE, workItem.getResult("isValid"));
		Assert.assertNotNull(workItem.getResult("errorMessage"));
		Assert.assertNotNull(workItem.getResult("errorCode"));
		Assert.assertEquals("Unprovisioned or out of coverage", workItem.getResult("errorMessage"));
		Assert.assertEquals("60600", workItem.getResult("errorCode"));
		System.out.println("Phone 2 is invalid");
		
		//workItem.setParameter("phone", "12035024615");
		workItem.setParameter("phone", "18042221111");
		handler.executeWorkItem(workItem, new NoOpWorkItemManager());
		Assert.assertNotNull(workItem.getResult("isValid"));
		Assert.assertEquals(Boolean.FALSE, workItem.getResult("isValid"));
		Assert.assertNotNull(workItem.getResult("errorMessage"));
		Assert.assertNotNull(workItem.getResult("errorCode"));
		Assert.assertEquals("-1", workItem.getResult("errorCode"));
		Assert.assertEquals("Phone is a landline, and not SMS enabled", workItem.getResult("errorMessage"));
		System.out.println("Phone 3 is not landline");
	}

	@Test
	public void parseErrorMessage() {
		PhoneValidationWorkItemHandler handler = new PhoneValidationWorkItemHandler();
		Assert.assertNotNull(handler.parseErrorMessage("60600"));
		Assert.assertNotNull(handler.parseErrorMessage("14100"));
		Assert.assertNotNull(handler.parseErrorMessage("30025"));
	}
	
	public static class NoOpWorkItemManager implements WorkItemManager {
		@Override 
		public void abortWorkItem(long id) { 
			// do nothing
		}
		@Override
		public void completeWorkItem(long id, Map<String, Object> results) {
			// do nothing
		}
		@Override
		public void registerWorkItemHandler(String workItemName, WorkItemHandler handler) {
			// do nothing
		}
	}
}
