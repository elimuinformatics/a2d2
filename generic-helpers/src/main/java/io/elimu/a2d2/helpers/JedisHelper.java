package io.elimu.a2d2.helpers;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisException;

public class JedisHelper {

	private static final int JEDIS_PORT = Integer.parseInt(System.getProperty("cache.jedis.port", "6379"));
	private static final String JEDIS_PWD = System.getProperty("cache.jedis.password");
	private static String JEDIS_HOST;
	private static final Boolean JEDIS_SSL = Boolean.valueOf(System.getProperty("cache.jedis.use_ssl", "false"));
	private static final int JEDIS_TIMEOUT_CONNECTION = Integer.parseInt(System.getProperty("cache.jedis.connection.timeout", "10000")); //milliseconds

	static JedisHelper INSTANCE;
	
	private static final Object _sync = new Object();
	private static final Logger LOG = LoggerFactory.getLogger(JedisHelper.class);
	
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

	private JedisHelper() {
		this.init();
	}

	protected void init() {
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
		}
	}
	
	protected Jedis connect() {
		return jedisPool.getResource();
	}

	public boolean containsKey(String key) {
		try (Jedis jedis = connect()) {
			Object o = jedis.exists(key);
			return Boolean.valueOf(String.valueOf(o)).booleanValue();
		} catch (JedisException e) {
			LOG.error("Cannot connect to Redis due to exception. Returing from containsKey with default false", e);
			return false;
		}
	}

	public void register(String key, int timeoutInSeconds) {
		try (Jedis jedis = connect()) {
			jedis.set(key, "x");
			jedis.expire(key, timeoutInSeconds);
		} catch (JedisException e) {
			LOG.error("Cannot connect to Redis due to exception. Returning from register", e);
		}
	}
}
