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
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Assert;
import io.elimu.a2d2.cds.fhir.cache.CacheUtil;
import io.elimu.a2d2.cds.fhir.helper.FhirResponse;
import io.elimu.a2d2.cds.fhir.helper.QueryingServerHelper;
import net.spy.memcached.MemcachedClient;

public class MemCachedTest {

	private static MemcachedClient crunchifySpyMemCached = null;

	public static QueryingServerHelper queryingServerHelper = spy(QueryingServerHelper.class)
			.addAuthentication(CacheUtil.BASIC, "user", "pass");

	private static final String responseStatusInfo_1 = "responseStatusInfo - response first user_1";
	private static final String responseStatusInfo_2 = "responseStatusInfo - response first user_2";
	private static final String responseStatusInfo_3 = "responseStatusInfo - response first user_3";
	private static FhirResponse<List<IBaseResource>> retval_1 = new FhirResponse<>(new LinkedList<IBaseResource>(), 200, responseStatusInfo_1);
	private static FhirResponse<List<IBaseResource>> retval_2 = new FhirResponse<>(new LinkedList<IBaseResource>(), 200, responseStatusInfo_2);
	private static FhirResponse<List<IBaseResource>> retval_3 = new FhirResponse<>(new LinkedList<IBaseResource>(), 401, responseStatusInfo_3);


	public static void main(String[] args) throws Exception {
	    memcachedConnection();

	    System.out.println("*** Init crunchifySpyMemCached test ***");
	    mapTest();
	    System.out.println("*** End crunchifySpyMemCached test ***");

	}

	private static void mapTest() throws InterruptedException {
		crunchifySpyMemCached.set("cacheKey1", 5, retval_1);
		crunchifySpyMemCached.set("cacheKey2", 10, retval_2);
		crunchifySpyMemCached.set("cacheKey3", 60, retval_3);


		boolean contains = crunchifySpyMemCached.get("cacheKey1") != null;
		Assert.assertTrue(contains);
		contains = crunchifySpyMemCached.get("cacheKey4") != null;
		Assert.assertFalse(contains);

		Thread.sleep(5000 + 1);
	    Assert.assertNull(crunchifySpyMemCached.get("cacheKey1"));
	    Assert.assertNotNull(crunchifySpyMemCached.get("cacheKey2"));
	    Thread.sleep(5000);
	    Assert.assertNull(crunchifySpyMemCached.get("cacheKey1"));
	    Assert.assertNull(crunchifySpyMemCached.get("cacheKey2"));
	    Assert.assertNotNull(crunchifySpyMemCached.get("cacheKey3"));

	    crunchifySpyMemCached.delete("cacheKey3");

	    Assert.assertNull(crunchifySpyMemCached.get("cacheKey1"));
	    Assert.assertNull(crunchifySpyMemCached.get("cacheKey2"));
	    Assert.assertNull(crunchifySpyMemCached.get("cacheKey3"));

	}


	private static void memcachedConnection() throws IOException {
		crunchifySpyMemCached = new MemcachedClient(new InetSocketAddress("127.0.0.1", 11211));
	}

}
