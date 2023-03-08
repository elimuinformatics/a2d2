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
	 * @param key key to fetch
	 * @return value associated with key
	 */
	T get(String key);

	/**
	 *
	 * @param key key to use
	 * @param value value to store
	 */
	void put(String key, T value);

	/**
	 *
	 * @param key key to remove
	 */
	void delete(String key);

	/**
	 *
	 */
	void deleteAll();

	/**
	 *
	 * @param key key to search
	 * @return true if the key has a value assigned
	 */
	boolean containsKey(String key);

	/**
	 *
	 * @param key key to search
	 * @return true if the key is new (not old cached value)
	 */
	boolean isNewKey(String key);

	/**
	 *
	 * @return length of the cache (in items count)
	 */
	long length();

	/**
	 *
	 * @return type of cache
	 */
	CACHE_TYPE getCacheType();

}
