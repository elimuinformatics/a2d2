package io.elimu.serviceapi.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.kie.api.executor.CommandContext;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryExecutorService extends EmptyExecutorService {

	private static final Logger LOG = LoggerFactory.getLogger(InMemoryExecutorService.class);
	
	private static final String DEFAULT_THREADPOOL_COUNT = System.getProperty("elimu.async.threadpool.count", "3");
	private final int threadpoolCount;
	private final ExecutorService executor;
	
	public InMemoryExecutorService(String serviceId) {
		super();
		this.threadpoolCount = Integer.parseInt(System.getProperty(serviceId + ".async.threadpool.count", DEFAULT_THREADPOOL_COUNT));
		this.executor = Executors.newFixedThreadPool(this.threadpoolCount);
	}
	
	@Override
	public Long scheduleRequest(String commandName, CommandContext ctx) {
		executor.execute(() -> {
			String deploymentId = (String) ctx.getData("deploymentId");
			Long processInstanceId = (Long) ctx.getData("processInstanceId");
	        String signal = (String) ctx.getData("Signal");
	        Object event = ctx.getData("Event");
	        LOG.info("About to send signal " + signal + " to instance " + processInstanceId);
	        RuntimeManager manager = RuntimeManagerRegistry.get().getManager(deploymentId);
    		final RuntimeEngine engine = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
        	engine.getKieSession().signalEvent(signal, event, processInstanceId);	
        	LOG.info("Signal " + signal + " sent to instance " + processInstanceId);
        });
        return -1L;
	}
}
