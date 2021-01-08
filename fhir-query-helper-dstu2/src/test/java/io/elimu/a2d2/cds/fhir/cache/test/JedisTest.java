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

package io.elimu.a2d2.cds.fhir.cache.test;

import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Assert;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import io.elimu.a2d2.cds.fhir.cache.CacheService;
import io.elimu.a2d2.cds.fhir.cache.CacheUtil;
import io.elimu.a2d2.cds.fhir.helper.FhirResponse;
import io.elimu.a2d2.cds.fhir.helper.ResponseEvent;

/**
 *
 * For this test we should have running Redis in our local environment
 * - default config => 127.0.0.1:6379
 *
 */
public class JedisTest {

	private static final String CACHE_KEY1 = CacheUtil.PREFIX_KEY.concat("cacheKey1");
	private static final String CACHE_KEY2 = CacheUtil.PREFIX_KEY.concat("cacheKey2");
	private static final String responseStatusInfo_1 = "responseStatusInfo - response first user_1";
	static FhirResponse<List<IBaseResource>> retval_1 = null;

	public static final long RESPONSE_EVENT_TIMEOUT = 300_000; // 5 minutes

	private static CacheService<ResponseEvent<FhirResponse>> cacheService;

	public static void main(String[] args) throws Exception {
	    redisConnection();
	    setMockData();

	    System.out.println("*** Init Jedis test ***");
	    cacheService.deleteAll();

	    jedisTest();
	    jedisTimeoutTest();
	    System.out.println("*** End Jedis test ***");

	}

	@SuppressWarnings("unchecked")
	private static void jedisTest() {
		// implements java.util.concurrent.ConcurrentMap
		cacheService.put(CACHE_KEY1, new ResponseEvent<>(retval_1, RESPONSE_EVENT_TIMEOUT));
		FhirResponse<List<IBaseResource>> retval = (FhirResponse<List<IBaseResource>>) cacheService.get(CACHE_KEY1).getValue();
		Assert.assertNotNull(retval);
	    Assert.assertEquals(200, retval.getResponseStatusCode());

	}

	private static void jedisTimeoutTest() throws InterruptedException {
		// implements java.util.concurrent.ConcurrentMap
		cacheService.put(CACHE_KEY2, new ResponseEvent<>(retval_1, 5000));
		FhirResponse<List<IBaseResource>> retval = (FhirResponse<List<IBaseResource>>) cacheService.get(CACHE_KEY2).getValue();
		Assert.assertNotNull(retval);
	    Assert.assertEquals(200, retval.getResponseStatusCode());
	    //it depends in JEDIS_TIMEOUT_KEY_SECONDS (this test is assuming that it is 10 seconds)
	    Thread.sleep(10000 + 1);
	    Assert.assertNull(cacheService.get(CACHE_KEY2));
	}

	/**
	 *  Default connection: 127.0.0.1:6379
	 *
	 */
	private static void redisConnection() {
		cacheService = CacheUtil.getCacheService();
	}

	private static void setMockData() {
		List<IBaseResource> iBaseList = new ArrayList<IBaseResource>();
		StructureDefinition iBase = new StructureDefinition();
		//iBase.setId(new IdDt("testId"));
		iBase.setUrl("test_url");
		iBaseList.add(iBase);

		retval_1 = new FhirResponse<>(iBaseList, 200, responseStatusInfo_1);

	}

}