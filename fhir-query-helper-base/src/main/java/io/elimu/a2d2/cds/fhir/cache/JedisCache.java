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

import java.time.Duration;
import java.util.Set;
import io.elimu.a2d2.cds.fhir.cache.CacheUtil.CACHE_TYPE;
import io.elimu.a2d2.cds.fhir.helper.FhirResponse;
import io.elimu.a2d2.cds.fhir.helper.ResponseEvent;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisCache implements CacheService<ResponseEvent<FhirResponse<?>>> {

	private CACHE_TYPE cacheType = null;

	private static final int JEDIS_PORT = Integer.parseInt(System.getProperty("cache.jedis.port", "6379"));
	private static final String JEDIS_HOST = System.getProperty("cache.jedis.host", "localhost");
	private static final int JEDIS_TIMEOUT_CONNECTION = Integer.parseInt(System.getProperty("cache.jedis.connection.timeout", "10000")); //milliseconds (default timeout 2000)
	private static final int JEDIS_TIMEOUT_KEY_SECONDS = Integer.parseInt(System.getProperty("cache.jedis.key.timeout", "1800")); //1800 = 30 minutes

	//private static final String REDIS_MAP_ID = "A2D2";
	//private byte[] jedisMap = REDIS_MAP_ID.getBytes();

	private JedisPool jedisPool = null;
	private Jedis jedis = null;

	public JedisCache() {
		this.connect();
		cacheType = CacheUtil.CACHE_TYPE.JEDIS;
	}

	@Override
	public void connect() {
		if(jedisPool == null) {
			jedisPool = new JedisPool(buildPoolConfig(), JEDIS_HOST, JEDIS_PORT, JEDIS_TIMEOUT_CONNECTION);
			jedis = jedisPool.getResource();
		}
	}

	/**
	 * TODO: we should check which is the correct config here
	 *
	 * @return
	 */
	private JedisPoolConfig buildPoolConfig() {
	    final JedisPoolConfig poolConfig = new JedisPoolConfig();
	    poolConfig.setMaxTotal(128);
	    poolConfig.setMaxIdle(128);
	    poolConfig.setMinIdle(16);
	    poolConfig.setTestOnBorrow(true);
	    poolConfig.setTestOnReturn(true);
	    poolConfig.setTestWhileIdle(true);
	    poolConfig.setMinEvictableIdleTime(Duration.ofSeconds(60));
	    poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(30));
	    poolConfig.setNumTestsPerEvictionRun(3);
	    poolConfig.setBlockWhenExhausted(true);
	    return poolConfig;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEvent<FhirResponse<?>> get(String key) {
		return (jedis.exists(key.getBytes())) ? (ResponseEvent<FhirResponse<?>>) CacheUtil.getObjectFromByte(jedis.get(key.getBytes())) : null;
	}

	@Override
	public void put(String key, ResponseEvent<FhirResponse<?>> value) {
		jedis.setex(key.getBytes(), JEDIS_TIMEOUT_KEY_SECONDS, CacheUtil.getByteFromObject(value));
	}

	@Override
	public void delete(String key) {
		jedis.del(key);
	}

	@Override
	public void deleteAll() {
		Set<String> keys = jedis.keys(CacheUtil.PREFIX_KEY.concat("*"));
		if(keys != null && keys.size() > 0) {
			jedis.del(keys.toArray(new String[keys.size()]));
		}

	}

	@Override
	public boolean containsKey(String key) {
		return jedis.exists(key.getBytes());
	}

	@Override
	public boolean isNewKey(String key) {
		return this.containsKey(key);
	}

	@Override
	public long length() {
		return jedis.keys(CacheUtil.PREFIX_KEY.concat("*")).size();
	}

	@Override
	public CACHE_TYPE getCacheType() {
		return cacheType;
	}

}
