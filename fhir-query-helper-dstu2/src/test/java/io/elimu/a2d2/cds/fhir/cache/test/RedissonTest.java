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

import static org.mockito.Mockito.spy;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Assert;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import io.elimu.a2d2.cds.fhir.cache.CacheUtil;
import io.elimu.a2d2.cds.fhir.helper.FhirResponse;
import io.elimu.a2d2.cds.fhir.helper.QueryingServerHelper;

/**
 *
 * For this test we should have running Redis in our local environment
 * - default config => 127.0.0.1:6379
 *
 */
public class RedissonTest {

	public static QueryingServerHelper queryingServerHelper = spy(QueryingServerHelper.class)
			.addAuthentication(CacheUtil.BASIC, "user", "pass");

	private static final String responseStatusInfo_1 = "responseStatusInfo - response first user_1";
	private static final String responseStatusInfo_2 = "responseStatusInfo - response first user_2";
	private static final String responseStatusInfo_3 = "responseStatusInfo - response first user_3";
	private static final String responseStatusInfo_4 = "responseStatusInfo - response first user_4";
	private static FhirResponse<List<IBaseResource>> retval_1 = new FhirResponse<>(new LinkedList<IBaseResource>(), 200, responseStatusInfo_1);
	private static FhirResponse<List<IBaseResource>> retval_2 = new FhirResponse<>(new LinkedList<IBaseResource>(), 200, responseStatusInfo_2);
	private static FhirResponse<List<IBaseResource>> retval_3 = new FhirResponse<>(new LinkedList<IBaseResource>(), 401, responseStatusInfo_3);
	private static FhirResponse<List<IBaseResource>> retval_4 = new FhirResponse<>(new LinkedList<IBaseResource>(), 200, responseStatusInfo_4);
	private static RedissonClient redisson = null;

	public static void main(String[] args) throws Exception {
	    redisConnection();

	    System.out.println("*** Init Redisson test ***");
	    RMapTest();
	    RMapCacheTest();
	    System.out.println("*** End Redisson test ***");

	    redisson.shutdown();
	}

	/**
	 *
	 * This test is to check the expire time
	 */
	private static void RMapCacheTest() throws InterruptedException {
		RMapCache<String, FhirResponse<List<IBaseResource>>> rMapCache = redisson.getMapCache("queryServerRMapCache");
		rMapCache.put("cacheKey1_RMapCache", retval_1, 5, TimeUnit.SECONDS);
		rMapCache.put("cacheKey2_RMapCache", retval_2, 10, TimeUnit.SECONDS);
		rMapCache.put("cacheKey3_RMapCache", retval_3);

	    boolean contains = rMapCache.containsKey("cacheKey1_RMapCache");
	    Assert.assertTrue(contains);
	    System.out.println("Map size: " + rMapCache.size());
	    System.out.println("Is map contains key 'cacheKey1_RMapCache': " + contains);

	    FhirResponse<List<IBaseResource>> retVal3 = rMapCache.get("cacheKey3_RMapCache");
	    System.out.println("Value mapped by key 'cacheKey3_RMapCache': " + retVal3.toString() + " - status: " + retVal3.getResponseStatusCode());
	    Assert.assertNotNull(retVal3);
	    Assert.assertEquals(401, retVal3.getResponseStatusCode());

	    boolean added = rMapCache.putIfAbsent("cacheKey4_RMapCache", retval_4) == null;
	    System.out.println("Is value mapped by key 'cacheKey4_RMapCache' added: " + added);
	    FhirResponse<List<IBaseResource>> retVal4 = rMapCache.get("cacheKey4_RMapCache");
	    Assert.assertNotNull(retVal4);
	    Assert.assertEquals(200, retVal4.getResponseStatusCode());


	    Assert.assertNotNull(rMapCache.get("cacheKey1_RMapCache"));
	    Assert.assertNotNull(rMapCache.get("cacheKey2_RMapCache"));
	    Assert.assertNotNull(rMapCache.get("cacheKey3_RMapCache"));
	    Assert.assertNotNull(rMapCache.get("cacheKey4_RMapCache"));
	    Thread.sleep(5000 + 1);
	    Assert.assertNull(rMapCache.get("cacheKey1_RMapCache"));
	    Assert.assertNotNull(rMapCache.get("cacheKey2_RMapCache"));
	    Assert.assertNotNull(rMapCache.get("cacheKey3_RMapCache"));
	    Assert.assertNotNull(rMapCache.get("cacheKey4_RMapCache"));
	    Thread.sleep(5000);
	    Assert.assertNull(rMapCache.get("cacheKey1_RMapCache"));
	    Assert.assertNull(rMapCache.get("cacheKey2_RMapCache"));
	    Assert.assertNotNull(rMapCache.get("cacheKey3_RMapCache"));
	    Assert.assertNotNull(rMapCache.get("cacheKey4_RMapCache"));

	    rMapCache.clear();
	    Assert.assertNull(rMapCache.get("cacheKey1_RMapCache"));
	    Assert.assertNull(rMapCache.get("cacheKey2_RMapCache"));
	    Assert.assertNull(rMapCache.get("cacheKey3_RMapCache"));
	    Assert.assertNull(rMapCache.get("cacheKey4_RMapCache"));
	}

	/**
	 * Redis based distributed Map object for Java implements java.util.concurrent.ConcurrentMap and java.util.Map interfaces.
	 * This map keeps elements in insertion order. Consider to use Live Object service if you plan to store your POJO objects as
	 * Redis Map objects.
	 */
	private static void RMapTest() {
		// implements java.util.concurrent.ConcurrentMap
	    RMap<String, FhirResponse<List<IBaseResource>>> rMap =  redisson.getMap("queryServerRMap");
	    rMap.put("cacheKey1", retval_1);
	    rMap.put("cacheKey2", retval_2);
	    rMap.put("cacheKey3", retval_3);

	    boolean contains = rMap.containsKey("cacheKey1");
	    Assert.assertTrue(contains);
	    System.out.println("Map size: " + rMap.size());
	    System.out.println("Is map contains key 'cacheKey1': " + contains);

	    FhirResponse<List<IBaseResource>> retVal3 = rMap.get("cacheKey3");
	    System.out.println("Value mapped by key 'cacheKey3': " + retVal3.toString() + " - status: " + retVal3.getResponseStatusCode());
	    Assert.assertNotNull(retVal3);
	    Assert.assertEquals(401, retVal3.getResponseStatusCode());

	    boolean added = rMap.putIfAbsent("cacheKey4", retval_4) == null;
	    System.out.println("Is value mapped by key 'cacheKey4' added: " + added);
	    FhirResponse<List<IBaseResource>> retVal4 = rMap.get("cacheKey4");
	    Assert.assertNotNull(retVal4);
	    Assert.assertEquals(200, retVal4.getResponseStatusCode());

	    Assert.assertNotNull(rMap.get("cacheKey1"));
	    Assert.assertNotNull(rMap.get("cacheKey2"));
	    Assert.assertNotNull(rMap.get("cacheKey3"));
	    Assert.assertNotNull(rMap.get("cacheKey4"));
	    rMap.clear();
	    Assert.assertNull(rMap.get("cacheKey1"));
	    Assert.assertNull(rMap.get("cacheKey2"));
	    Assert.assertNull(rMap.get("cacheKey3"));
	    Assert.assertNull(rMap.get("cacheKey4"));
	}

	/**
	 *  Default connection: 127.0.0.1:6379
	 *
	 */
	private static void redisConnection() {
		redisson = Redisson.create();
	}

}