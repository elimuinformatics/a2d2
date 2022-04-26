package io.elimu.a2d2.core_wih;

import java.util.Map;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import io.elimu.a2d2.corewih.MixPanelWorkItemHandler;
import io.elimu.a2d2.genericmodel.ServiceRequest; 

public class MixPanelWIHTest {

	@Test
	public void testMixPanelEvent() {
		MixPanelWorkItemHandler handler = new MixPanelWorkItemHandler();
		if (System.getProperty("mixpanel.app.token") == null) {
			return;
		}
		WorkItemImpl workItem = new WorkItemImpl();
		ServiceRequest sreq = new ServiceRequest();
		sreq.addHeaderValue("mixpanel-some-value", "Some Value");
		workItem.setParameter("serviceRequest", sreq);
		workItem.setParameter("eventDescription", "My Test Event");
		workItem.setParameter("mixpanel_info", "Some value");
		workItem.setParameter("mixpanel_otherInfo", "Something else");
		NoOpWorkItemManager manager = new NoOpWorkItemManager();
		handler.executeWorkItem(workItem, manager);
		Assert.assertEquals("completed", manager.getStatus());
	}
	
	class NoOpWorkItemManager implements WorkItemManager {

		private String status;
		
		public String getStatus() {
			return status;
		}
		
		@Override
		public void completeWorkItem(long id, Map<String, Object> results) {
			status = "completed";
		}

		@Override
		public void abortWorkItem(long id) {
			status = "aborted";
		}

		@Override
		public void registerWorkItemHandler(String workItemName, WorkItemHandler handler) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
