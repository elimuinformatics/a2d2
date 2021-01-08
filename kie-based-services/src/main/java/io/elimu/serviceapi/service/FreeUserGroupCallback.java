package io.elimu.serviceapi.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kie.internal.task.api.UserGroupCallback;

public class FreeUserGroupCallback implements UserGroupCallback {

	private Set<String> discoveredGroups = new HashSet<>();
	
	@Override
	public boolean existsUser(String userId) {
		return true;
	}

	@Override
	public boolean existsGroup(String groupId) {
		discoveredGroups.add(groupId);
		return true;
	}

	@Override
	public List<String> getGroupsForUser(String userId) {
		return new ArrayList<>(discoveredGroups);
	}

}
