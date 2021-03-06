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

import io.elimu.a2d2.cds.fhir.cache.CacheUtil.CACHE_TYPE;

public interface CacheService<T> {

	/**
	 *
	 */
	void connect();

	/**
	 *
	 * @param key
	 * @return
	 */
	T get(String key);

	/**
	 *
	 * @param key
	 * @param value
	 */
	void put(String key, T value);

	/**
	 *
	 * @param key
	 */
	void delete(String key);

	/**
	 *
	 */
	void deleteAll();

	/**
	 *
	 * @param key
	 * @return
	 */
	boolean containsKey(String key);

	/**
	 *
	 * @param key
	 * @return
	 */
	boolean isNewKey(String key);

	/**
	 *
	 * @return
	 */
	long length();

	/**
	 *
	 * @return
	 */
	CACHE_TYPE getCacheType();

}
