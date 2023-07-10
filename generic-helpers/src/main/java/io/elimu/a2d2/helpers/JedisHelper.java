package io.elimu.a2d2.helpers;

import java.time.Duration;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

public class JedisHelper {

	private static final int JEDIS_PORT = Integer.parseInt(System.getProperty("cache.jedis.port", "6379"));
	private static final String JEDIS_PWD = System.getProperty("cache.jedis.password");
	private static String JEDIS_HOST;
	private static final Boolean JEDIS_SSL = Boolean.valueOf(System.getProperty("cache.jedis.use_ssl", "false"));
	private static final int JEDIS_TIMEOUT_CONNECTION = Integer.parseInt(System.getProperty("cache.jedis.connection.timeout", "10000")); //milliseconds

	static JedisHelper INSTANCE;
	
	private static final Object _sync = new Object();

	
	
	public static JedisHelper getInstance() {
		synchronized(_sync) {
			if (INSTANCE == null) {
				JEDIS_HOST = System.getProperty("cache.jedis.host");
				if (JEDIS_HOST == null) {
					throw new RuntimeException("Required config variable not found: cache.jedis.host");
				}
				INSTANCE = new JedisHelper();
			}
		}
		return INSTANCE;
	}
	
	private JedisPool jedisPool = null;
	private Jedis jedis = null;

	private JedisHelper() {
		this.connect();
	}

	protected void connect() {
		if(jedisPool == null) {
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
			jedisPool = new JedisPool(poolConfig, JEDIS_HOST, JEDIS_PORT, JEDIS_TIMEOUT_CONNECTION, JEDIS_PWD, Protocol.DEFAULT_DATABASE, JEDIS_SSL);
			jedis = jedisPool.getResource();
		}
	}

	public boolean containsKey(String key) {
		return jedis.exists(key);
	}

	public void register(String key, int timeoutInSeconds) {
		jedis.setex(key, timeoutInSeconds, "x");
	}

}
