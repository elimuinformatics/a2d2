package io.elimu.a2d2.helpers;

import java.io.IOException;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ai.grakn.redismock.RedisServer;

public class JedisHelperTest {

	private static RedisServer redis;
	
	@BeforeClass
	public static void startup() throws IOException {
		int port = new Random().nextInt(5000) + 3000;
		boolean loaded = false;
		int attempts = 0;
		while (!loaded && attempts < 10) {
			try {
				System.err.println("Attempting redis server mock at port " + port + "...");
				redis = RedisServer.newRedisServer(port);
				redis.start();
				loaded = true;
			} catch (Exception e) {
				port = new Random().nextInt(5000) + 3000;
				attempts++;
			}
		}
		System.setProperty("cache.jedis.port", String.valueOf(redis.getBindPort()));//port might differ from initial configuration
	}
	
	@AfterClass
	public static void shutdown() {
		if (redis != null) {
			redis.stop();
		}
	}
	
	@Test(expected = RuntimeException.class)
	public void testNoConfig() {
		System.clearProperty("cache.jedis.host");
		JedisHelper.INSTANCE = null;
		JedisHelper.getInstance();
	}
	
	@Test
	public void testJedisKeys() throws Exception {
		try {
			System.setProperty("cache.jedis.host", "localhost");
			String key = "htn-engage-program_18582221234";
			Assert.assertFalse(JedisHelper.getInstance().containsKey(key));
			JedisHelper.getInstance().register(key, 2);
			Assert.assertTrue(JedisHelper.getInstance().containsKey(key));
			Thread.sleep(5000);
			Assert.assertFalse(JedisHelper.getInstance().containsKey(key));
		} finally {
			System.clearProperty("cache.jedis.host");
		}
	}
}
