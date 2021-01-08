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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.interceptor.SimpleRequestHeaderInterceptor;
import io.elimu.a2d2.cds.fhir.helper.FhirResponse;
import io.elimu.a2d2.cds.fhir.helper.ResponseEvent;

public class CacheUtil {

	private static final Logger log = LoggerFactory.getLogger(CacheUtil.class);

	//expected values(CACHE_TYPE => JEDIS, MEMORY, NO_CACHE) default config is NO_CACHE
	public static final String CACHE_TYPE_PROPERTY = System.getProperty("cache.type.impl", "NO_CACHE");
	public static final String PREFIX_KEY = System.getProperty("cache.prefix", "a2d2");
	public static final String BASIC = "Basic";

	private static final List<Integer> VALID_STATES = Arrays.asList(200);

	private CacheUtil() {
		throw new IllegalStateException("Utility class");
	}
	 
	public static enum CACHE_TYPE {
		MEMORY, JEDIS, NO_CACHE
	}

	public static CacheService<ResponseEvent<FhirResponse>> getCacheService() {
		CacheService<ResponseEvent<FhirResponse>> cacheService = null;
		CACHE_TYPE type = CACHE_TYPE.valueOf(CACHE_TYPE_PROPERTY);
		try {
			switch (type) {
			case JEDIS:
				cacheService = new JedisCache();
				break;
			case MEMORY:
				cacheService = new InMemoryCache();
				break;
			case NO_CACHE:
				cacheService = null;
				break;
			default:
				throw new Exception("The CACHE_TYPE_PROPERTY was set incorrecctly: " + CACHE_TYPE_PROPERTY);
			}
		} catch (Exception e) {
			log.error("{} - Error trying to connect with {} - doing a default config to work without caching",
					e.getMessage(), CacheUtil.CACHE_TYPE_PROPERTY);
			cacheService = null;
		}
		return cacheService;
	}

	public static String getCacheKey(String resourceQuery, List<IClientInterceptor> interceptors) {
		String cacheKey = PREFIX_KEY.concat(resourceQuery);
		String token = getTokenFromHeader(resourceQuery, interceptors);
		if (token != null) {
			cacheKey = PREFIX_KEY.concat(token).concat("_").concat(resourceQuery);
		}
		return cacheKey;
	}

	public static boolean isValidState(int status) {
		return VALID_STATES.contains(status);
	}

	private static String getTokenFromHeader(String resourceQuery, List<IClientInterceptor> interceptors) {
		String token = null;
		for (int i = 0; i < interceptors.size(); i++) {
			try {
				SimpleRequestHeaderInterceptor header = (SimpleRequestHeaderInterceptor) interceptors.get(i);
				if (header.getHeaderName().equals(BASIC)) {
					token = header.getHeaderValue();
					break;
				}
			} catch (Exception e) {
				log.error("Error while iterating over IClientInterceptor - ", e);
			}
		}
		return token;
	}

	public static byte[] getByteFromObject(Object value) {
		byte[] result = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
			out.writeObject(value);
			result = bos.toByteArray();
		} catch (Exception e) {
			log.error("getByteFromObject: {}", e.getMessage());
		}
		return result;
	}

	public static Object getObjectFromByte(byte[] bytes) {
		Object result = null;
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		try (ObjectInput in = new ObjectInputStream(bis)) {
			result = in.readObject();
		} catch (Exception e) {
			log.error("getObjectFromByte: ", e.getMessage());
		} 
		return result;
	}

}
