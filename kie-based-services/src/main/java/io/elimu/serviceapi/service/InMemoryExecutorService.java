package io.elimu.serviceapi.service;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.kie.api.executor.CommandContext;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

public class InMemoryExecutorService extends EmptyExecutorService {

	private static final String DEFAULT_THREADPOOL_COUNT = System.getProperty("elimu.async.threadpool.count", "3");
	private final int threadpoolCount;
	private final ExecutorService executor;
	
	public InMemoryExecutorService(String serviceId) {
		super();
		this.threadpoolCount = Integer.parseInt(System.getProperty(serviceId + ".async.threadpool.count", DEFAULT_THREADPOOL_COUNT));
		this.executor = Executors.newFixedThreadPool(this.threadpoolCount);
	}
	
	@Override
	public Long scheduleRequest(String commandId, Date date, final CommandContext ctx) {
		String deploymentId = (String) ctx.getData("deploymentId");
		final Long processInstanceId = (Long) ctx.getData("processInstanceId");
        final String signal = (String) ctx.getData("Signal");
        final Object event = ctx.getData("Event");
		RuntimeManager manager = RuntimeManagerRegistry.get().getManager(deploymentId);
		final RuntimeEngine engine = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
        executor.execute(() -> engine.getKieSession().signalEvent(signal, event, processInstanceId));
        return -1L;
	}
}
