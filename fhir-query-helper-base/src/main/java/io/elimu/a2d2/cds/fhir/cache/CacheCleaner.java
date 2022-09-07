package io.elimu.a2d2.cds.fhir.cache;

import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheCleaner implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(CacheCleaner.class);
	private static final CacheCleaner INSTANCE = new CacheCleaner();
	private Thread main;
	
	private CacheCleaner() {
		this.main = Executors.defaultThreadFactory().newThread(this);
		this.main.setName("FhirQueryHelperCacheCleaner-" + this.main.getId()); 
		this.main.start();
	}
	
	public static CacheCleaner getInstance() {
		return INSTANCE;
	}
	
	@Override
	protected void finalize() throws Throwable {
		this.main.interrupt();
	}

	public static class CacheReference {
		private final String key;
		private final CacheService<?> service;
		private final long timestamp;
		
		public CacheReference(String key, CacheService<?> service, long timestamp) {
			this.key = key;
			this.service = service;
			this.timestamp = timestamp;
		}
		
		public String getKey() {
			return key;
		}
		
		public CacheService<?> getService() {
			return service;
		}
		
		public long getTimestamp() {
			return timestamp;
		}
	}
	
	Queue<CacheReference> queue = new LinkedBlockingQueue<>();
	
	@Override
	public void run() {
		try {
			while(true) {
				if (queue.isEmpty()) {
					Thread.sleep(100);
					continue;
				}
				CacheReference ref = queue.peek();
				int cleanupsDone = 0;
				if (ref != null && System.currentTimeMillis() > ref.getTimestamp()) {
					queue.remove();
					ref.getService().delete(ref.getKey());
					cleanupsDone++;
				} else {
					Thread.sleep(100);
				}
				if (cleanupsDone > 0) {
					LOG.debug("Cache cleaned up " + cleanupsDone + " old entries.");
				}
			}
		} catch (Exception e) { }
	}

	public void register(String key, long timeout, CacheService<?> service) {
		queue.add(new CacheReference(key, service, timeout));
	}
}
