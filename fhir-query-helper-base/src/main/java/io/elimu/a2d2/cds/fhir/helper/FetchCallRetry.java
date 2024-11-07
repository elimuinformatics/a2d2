package io.elimu.a2d2.cds.fhir.helper;

import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;

public class FetchCallRetry<T> {

	private static final Logger log = LoggerFactory.getLogger(FetchCallRetry.class);
	
	private Function<Object, T> function;
	private QueryBuilder builder;

	public FetchCallRetry(QueryBuilder builder, Function<Object, T> function) {
		this.builder = builder;
		this.function = function;
	}

	public T retryRestCall(FhirClientWrapper client)  {
		int retries = builder.getRetries();
		int delay = builder.getDelay();
		int count = 0;
		boolean success = false;
		while (count < retries && !success) {
			try {
				if (count > 0) {
					Thread.sleep(getDelay(count, delay));
				}
				T retval = function.apply(null);
				if (client.getTracker().getResponseStatusCode() < 500) {
					success = true;
					return retval;
				}
				log.info("FHIR REST call failed attempt number " + count + " with status " + client.getTracker().getResponseStatusCode() + ". Retrying after waiting " + (delay * count) + "ms");
				count++;
			} catch (BaseServerResponseException t) {
				count++;			
				log.error("Invocation of call failed with body response: " + t.getResponseBody());
			}
			catch (Throwable t) {
				if (client.getTracker().getResponseStatusCode() >= 0 && client.getTracker().getResponseStatusCode() < 500) {
					throw new RuntimeException("Invocation of call failed with status code " + client.getTracker().getResponseStatusCode() + ". No further retries will be performed");
				}
				count++; // and retry
				log.warn("FHIR REST call failed attempt number " + count + " with status " + client.getTracker().getResponseStatusCode() + " and error. Retrying after waiting " + (delay * count) + "ms", t);
			}
		}
		throw new RuntimeException("After " + count + " retries, invocation of call still failed");
	}

	public int getDelay(int count, int delay) {
		if (count == 2) {
			delay *= 3;
		} else if (count >= 3) {
			delay *= 5;
		}
		return delay;
	}
}
