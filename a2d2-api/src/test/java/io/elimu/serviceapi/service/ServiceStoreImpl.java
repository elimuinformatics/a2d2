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

package io.elimu.serviceapi.service;

import io.elimu.genericapi.service.GenericKieBasedService;

//mock implementation for testing
public class ServiceStoreImpl implements ServiceStore {

	private static final ServiceStoreImpl INSTANCE = new ServiceStoreImpl();

	public static ServiceStoreImpl getInstance() {
		return INSTANCE;
	}
	
	private ServiceStoreImpl() {
	}
	
	@Override
	public int delete(String id) {
		//do nothing
		return 0;
	}

	@Override
	public void updateFailed(String id) {
		//do nothing
	}

	@Override
	public void markUpdate(GenericKieBasedService service) {
		//do nothing
	}

	@Override
	public void updateDone(GenericKieBasedService service) {
		//do nothing
	}
}
