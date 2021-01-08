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

package io.elimu.a2d2.cds.fhir.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import io.elimu.a2d2.cds.fhir.cache.CacheUtil.CACHE_TYPE;
import io.elimu.a2d2.cds.fhir.helper.FhirResponse;
import io.elimu.a2d2.cds.fhir.helper.ResponseEvent;

public class InMemoryCache implements CacheService<ResponseEvent<FhirResponse>> {

	private Map<String, ResponseEvent<FhirResponse>> responsesCache = null;
	private CACHE_TYPE cacheType = null;

	public InMemoryCache() {
		this.connect();
		cacheType = CacheUtil.CACHE_TYPE.MEMORY;
	}

	@Override
	public void connect() {
		if(responsesCache == null) {
			responsesCache = new ConcurrentHashMap<>();
		}
	}

	@Override
	public ResponseEvent<FhirResponse> get(String key) {
		return responsesCache.get(key);
	}

	@Override
	public void put(String key, ResponseEvent<FhirResponse> value) {
		responsesCache.put(key, value);

	}

	@Override
	public void delete(String key) {
		if(responsesCache.containsKey(key)) {
			responsesCache.remove(key);
		}
	}

	@Override
	public void deleteAll() {
		responsesCache.clear();
	}

	@Override
	public boolean containsKey(String key) {
		return (responsesCache.containsKey(key) && this.isNewKey(key));
	}

	@Override
	public boolean isNewKey(String key) {
		return responsesCache.get(key).isNew();
	}

	@Override
	public long length() {
		return responsesCache.size();
	}

	@Override
	public CACHE_TYPE getCacheType() {
		return cacheType;
	}

}
