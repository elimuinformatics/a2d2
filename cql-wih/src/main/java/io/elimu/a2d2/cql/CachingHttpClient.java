package io.elimu.a2d2.cql;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

public class CachingHttpClient implements HttpClient {
	
	private CloseableHttpClient wrap;
	private static final Map<String, TimeObject<HttpResponse>> CACHED_CALLS = new HashMap<>();

	public CachingHttpClient() {
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(5000,
				TimeUnit.MILLISECONDS);
		connectionManager.setMaxTotal(200);
		connectionManager.setDefaultMaxPerRoute(200);
		connectionManager.setValidateAfterInactivity(300000);
		RequestConfig defaultRequestConfig =
			RequestConfig.custom()
				.setSocketTimeout(30000)
				.setConnectTimeout(30000)
				.setConnectionRequestTimeout(30000)
				.build();
		HttpClientBuilder builder = HttpClients.custom()
				.useSystemProperties()
				.setConnectionManager(connectionManager)
				.setDefaultRequestConfig(defaultRequestConfig)
				.disableCookieManagement();
		this.wrap = builder.build();
	}
			
	@Override @SuppressWarnings("deprecation")
	public org.apache.http.params.HttpParams getParams() {
		return wrap.getParams();
	}

	@Override @SuppressWarnings("deprecation")
	public org.apache.http.conn.ClientConnectionManager getConnectionManager() {
		return wrap.getConnectionManager();
	}

	@Override
	public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
		HttpResponse cached = cachedValue(request);
		if (cached != null) {
			return cached;
		}
		HttpResponse retval = repetitive(wrap.execute(request));
		CACHED_CALLS.put(getKey(request), new TimeObject<>(retval));
		return retval;
	}

	@Override
	public HttpResponse execute(HttpUriRequest request, HttpContext context)
			throws IOException, ClientProtocolException {
		HttpResponse cached = cachedValue(request);
		if (cached != null) {
			return cached;
		}
		HttpResponse retval = repetitive(wrap.execute(request, context));
		CACHED_CALLS.put(getKey(request), new TimeObject<>(retval));
		return retval;
	}

	private String getKey(HttpUriRequest request) {
		return request.getMethod() + "_" + request.getURI().toASCIIString();
	}

	private HttpResponse cachedValue(HttpUriRequest request) {
		String key = getKey(request);
		if (CACHED_CALLS.containsKey(key)) {
			TimeObject<HttpResponse> resp = CACHED_CALLS.get(key);
			if (resp != null && !resp.olderThan(30000)) {
				return resp.getValue();
			}
		}
		return null;
	}

	private HttpResponse repetitive(HttpResponse retval) throws IOException {
		if (retval instanceof CachedHttpResponse) {
			return retval;
		}
		return new CachedHttpResponse(retval);
	}

	@Override
	public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException, ClientProtocolException {
		return wrap.execute(target, request);
	}

	@Override
	public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context)
			throws IOException, ClientProtocolException {
		return wrap.execute(target, request, context);
	}

	@Override
	public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler)
			throws IOException, ClientProtocolException {
		HttpResponse cached = cachedValue(request);
		if (cached != null) {
			return responseHandler.handleResponse(cached);
		}
		HttpResponse retval = repetitive(wrap.execute(request));
		CACHED_CALLS.put(getKey(request), new TimeObject<>(retval));
		return responseHandler.handleResponse(retval);
	}

	@Override
	public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context)
			throws IOException, ClientProtocolException {
		HttpResponse cached = cachedValue(request);
		if (cached != null) {
			return responseHandler.handleResponse(cached);
		}
		HttpResponse retval = repetitive(wrap.execute(request, context));
		CACHED_CALLS.put(getKey(request), new TimeObject<>(retval));
		return responseHandler.handleResponse(retval);
	}

	@Override
	public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler)
			throws IOException, ClientProtocolException {
		return wrap.execute(target, request, responseHandler);
	}

	@Override
	public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler,
			HttpContext context) throws IOException, ClientProtocolException {
		return wrap.execute(target, request, responseHandler, context);
	}

}
