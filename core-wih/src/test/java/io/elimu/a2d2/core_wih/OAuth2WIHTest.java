// Copyright 2018-2021 Elimu Informatics
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

package io.elimu.a2d2.core_wih;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import io.elimu.a2d2.corewih.OAuth2WorkItemHandler;

public class OAuth2WIHTest {

	private static String user;
	private static String pass;
	private static String tokenUrl;
	private static String clientId;

	@BeforeClass
	public static void setup() {
		OAuth2WIHTest.user = System.getProperty("authusername");
		OAuth2WIHTest.pass = System.getProperty("authpassword");
		OAuth2WIHTest.tokenUrl = System.getProperty("tokenurl");
		OAuth2WIHTest.clientId = System.getProperty("clientid");
        org.junit.Assume.assumeTrue(user != null && pass != null && tokenUrl != null && clientId != null);
	}
	
	@Test
	public void testSuccessCase() {
		OAuth2WorkItemHandler handler = new OAuth2WorkItemHandler();
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setId(1L);
		workItem.setParameter("username", user);
		workItem.setParameter("password", pass);
		workItem.setParameter("tokenUrl", tokenUrl);
		workItem.setParameter("clientId", clientId);
		workItem.setParameter("grantType", "password");
		TestWorkItemManager manager = new TestWorkItemManager();
		handler.executeWorkItem(workItem, manager);

		Assert.assertTrue(manager.isCompleted(workItem.getId()));
		Object accessToken = workItem.getResults().get("accessToken");
		Object expiresIn = workItem.getResults().get("expiresIn");
		Object refreshToken = workItem.getResults().get("refreshToken");
		Object refreshExpiresIn = workItem.getResults().get("refreshExpiresIn");
		Assert.assertNotNull(accessToken);
		Assert.assertNotNull(expiresIn);
		Assert.assertNotNull(refreshToken);
		Assert.assertNotNull(refreshExpiresIn);
		
		WorkItemImpl workItem2 = new WorkItemImpl();
		workItem2.setId(2L);
		workItem2.setParameter("tokenUrl", tokenUrl);
		workItem2.setParameter("clientId", clientId);
		workItem2.setParameter("refreshToken", refreshToken);
		workItem2.setParameter("grantType", "refresh_token");
		handler.executeWorkItem(workItem2, manager);
		
		Assert.assertTrue(manager.isCompleted(workItem2.getId()));
		accessToken = workItem2.getResults().get("accessToken");
		expiresIn = workItem2.getResults().get("expiresIn");
		refreshToken = workItem2.getResults().get("refreshToken");
		refreshExpiresIn = workItem2.getResults().get("refreshExpiresIn");
		Assert.assertNotNull(accessToken);
		Assert.assertNotNull(expiresIn);
		Assert.assertNotNull(refreshToken);
		Assert.assertNotNull(refreshExpiresIn);
		
	}
	
	@Test
	public void testBadCredentials() {
		OAuth2WorkItemHandler handler = new OAuth2WorkItemHandler();
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setId(3L);
		workItem.setParameter("grantType", "password");
		workItem.setParameter("username", user);
		workItem.setParameter("password", "SomethingElse"+pass);
		workItem.setParameter("tokenUrl", tokenUrl);
		workItem.setParameter("clientId", clientId);
		TestWorkItemManager manager = new TestWorkItemManager();
		handler.executeWorkItem(workItem, manager);

		Assert.assertTrue(manager.isCompleted(workItem.getId()));
		Assert.assertNotNull(workItem.getResults().get("error"));
		Assert.assertNotNull(workItem.getResults().get("errorMessage"));
	}
	
	public static class TestWorkItemManager implements WorkItemManager {

		private List<Long> completed = new LinkedList<>();
		
		public boolean isCompleted(long id) {
			return completed.contains(id);
		}
		
		@Override
		public void completeWorkItem(long id, Map<String, Object> results) {
			completed.add(id);
		}

		@Override
		public void abortWorkItem(long id) {
		}

		@Override
		public void registerWorkItemHandler(String workItemName, WorkItemHandler handler) {
		}
		
	}
}
