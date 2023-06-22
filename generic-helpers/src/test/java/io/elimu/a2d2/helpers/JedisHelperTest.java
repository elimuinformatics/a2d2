package io.elimu.a2d2.helpers;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ai.grakn.redismock.RedisServer;

public class JedisHelperTest {

	private static RedisServer redis;
	
	@BeforeClass
	public static void startup() throws IOException {
		String host = System.getProperty("cache.jedis.host", "localhost");
		if ("localhost".equals(host)) {
			int port = Integer.valueOf(System.getProperty("cache.jedis.port", "6379"));
			redis = RedisServer.newRedisServer(port);
			redis.start();
			System.setProperty("cache.jedis.port", String.valueOf(redis.getBindPort()));//port might differ from initial configuration
		}
	}
	
	@AfterClass
	public static void shutdown() {
		if (redis != null) {
			redis.stop();
		}
	}
	
	@Test
	public void testJedisKeys() throws Exception {
		String key = "htn-engage-program_18582221234";
		Assert.assertFalse(JedisHelper.getInstance().containsKey(key));
		JedisHelper.getInstance().register(key, 2);
		Assert.assertTrue(JedisHelper.getInstance().containsKey(key));
		Thread.sleep(5000);
		Assert.assertFalse(JedisHelper.getInstance().containsKey(key));
	}
}
