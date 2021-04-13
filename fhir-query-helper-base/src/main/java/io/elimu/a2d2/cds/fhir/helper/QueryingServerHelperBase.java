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

package io.elimu.a2d2.cds.fhir.helper;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IRestfulClientFactory;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.impl.BaseClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.client.interceptor.SimpleRequestHeaderInterceptor;
import io.elimu.a2d2.cds.fhir.cache.CacheService;
import io.elimu.a2d2.cds.fhir.cache.CacheUtil;
import io.elimu.a2d2.oauth.BodyBuilder;
import io.elimu.a2d2.oauth.OAuthUtils;
import io.elimu.a2d2.serviceapi.performance.PerformanceHelper;

@SuppressWarnings("unchecked")
public abstract class QueryingServerHelperBase<T, U extends IBaseResource> implements Externalizable {

	protected static final Logger log = LoggerFactory.getLogger(QueryingServerHelperBase.class);

	private static final String TIMEOUT = "30000";
	private static final String FHIR_CLIENT_TIMEOUT = "fhir.client.timeout";
	private static final String ERROR_MSG = "Error executing FHIR query. ";
	private static final String QUERYINGSERVERHELPER = "QueryingServerHelper";
	private static final Integer FHIR_CLIENT_CONPOOL = Integer.valueOf(System.getProperty("fhir.client.conpool", "20"));
	private static final Integer FHIR_CLIENT_POOLMAXTOTAL = Integer.valueOf(System.getProperty("fhir.client.conpool", "20"));
	private static final Integer FHIR_CLIENT_REQUEST_TIMEOUT = Integer.valueOf(System.getProperty(FHIR_CLIENT_TIMEOUT, TIMEOUT));
	private static final Integer FHIR_CLIENT_CONN_TIMEOUT = Integer.valueOf(System.getProperty(FHIR_CLIENT_TIMEOUT, TIMEOUT));
	private static final Integer FHIR_CLIENT_SOCKET_TIMEOUT = Integer.valueOf(System.getProperty(FHIR_CLIENT_TIMEOUT, TIMEOUT));

	
	private transient final ExecutorService pool = Executors.newFixedThreadPool(FHIR_CLIENT_POOLMAXTOTAL);
	protected transient FhirContext ctx;
	protected String fhirUrl = null;
	protected final Rotator<FhirClientWrapper> clients;
	private static final Map<String, Rotator<FhirClientWrapper>> FHIR_CLIENTS = new ConcurrentHashMap<>();

	protected static CacheService<ResponseEvent<FhirResponse>> cacheService = CacheUtil.getCacheService();

	public static final String BEARER = "Bearer";
	protected static final String BASIC = "Basic";
	protected static final String HEADER = "Header";
	public static final String OAUTH = "OAuth";

	protected final List<IClientInterceptor> interceptors = new LinkedList<>();

	public static final long RESPONSE_EVENT_TIMEOUT = 300_000; // 5 minutes

	private Class<U> bundleType;
	protected FhirVersionAbs fhirVersion;

	private FhirVersionEnum fhirVersionEnum;

	protected ThreadLocal<Boolean> avoidCache = new ThreadLocal<Boolean>() {
		@Override protected Boolean initialValue() {
			return Boolean.FALSE;
		}
	};

	public QueryingServerHelperBase(String url, FhirVersionAbs fhirVersion, FhirVersionEnum fhirVersionEnum) {
		if (fhirVersion != null) {
			ctx = fhirVersion.getCtx();
		}
		fhirUrl = url;
		clients = getClientInstance(ctx, fhirUrl);
		this.fhirVersion = fhirVersion;
		this.fhirVersionEnum = fhirVersionEnum;

		log.info("Cache config: {}", (cacheService == null) ? "NO_CACHE" : cacheService.getCacheType());
	}

	private synchronized static Rotator<FhirClientWrapper> getClientInstance(FhirContext ctxt, String fhirUrl) {
		String key = genClientKey(ctxt, fhirUrl);
		if (FHIR_CLIENTS.get(key) == null) {
			FHIR_CLIENTS.put(key, new Rotator<FhirClientWrapper>(() -> {
				IGenericClient client = ctxt.newRestfulGenericClient(fhirUrl);
				client.setEncoding(EncodingEnum.JSON);
				((BaseClient) client).setDontValidateConformance(true);
				return new FhirClientWrapper(client);
			}, Integer.valueOf(System.getProperty("fhir.client.instancecount", "10"))));
			log.debug("Creating new set of clients for " + fhirUrl);
		} else {
			log.debug("Client already exists for fhirUrl " + fhirUrl
					+ " with version "
					+ ctxt.getVersion().getVersion().name());
		}
		return FHIR_CLIENTS.get(key);
	}

	private static String genClientKey(FhirContext ctxt,String fhirUrl) {
		return ctxt.getVersion().getVersion().name()+fhirUrl;
	}

	public T setHeaders(Map<String, String> headers) {
		if (headers != null) {
			for(Map.Entry<String, String> entry : headers.entrySet()) {
				addHeader(entry.getKey(), entry.getValue());
			}
		}
		return (T) this;
	}

	public T addAuthentication(String authType, String... params) {
		if (authType == null) {
			return (T) this;
		}
		if (BASIC.equalsIgnoreCase(authType)) {
			if (params.length != 2) {
				log.warn("Basic authentication needs to have two params. Currently we have {}", params.length);
				return (T) this;
			}
			registerInterceptor(new SimpleRequestHeaderInterceptor("Authorization",
						"Basic "+ Base64.getEncoder().encodeToString(getSrc(params).getBytes())));
		} else if (BEARER.equalsIgnoreCase(authType)) {
			if (params.length != 1) {
				log.warn("Bearer authentication needs to have one param. Currently we have {}", params.length);
				return (T) this;
			}
			registerInterceptor(new BearerTokenAuthInterceptor(params[0]));
		} else if (HEADER.equalsIgnoreCase(authType)) {
			if (params.length == 1) {
				registerInterceptor(new SimpleRequestHeaderInterceptor(params[0]));
			} else if (params.length == 2){
				registerInterceptor(new SimpleRequestHeaderInterceptor(params[0], params[1]));
			} else {
				log.warn("Header authentication needs to have one or two params. Currently we have {}", params.length);
				return (T) this;
			}
		} else if (OAUTH.equalsIgnoreCase(authType)) {
			//use OAuthUtils and as many params as needed
			Map<String, String> paramsMap = new HashMap<>();
			for (int index = 0; index < params.length; index+=2) {
				String key = params[index];
				String value = null;
				if (index+1 < params.length) {
					value = params[index + 1];
				}
				paramsMap.put(key, value);
			}
			BodyBuilder builder = new BodyBuilder();
			for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
				builder.addToBody(entry.getKey(), entry.getValue());
			}
			String tokenUrl = paramsMap.get("token_url");
			String clientId = paramsMap.get("client_id");
			String clientSecret = paramsMap.get("client_secret");
			Map<String, Object> results = OAuthUtils.authenticate(builder.build(), tokenUrl, clientId, clientSecret);
			registerInterceptor(new BearerTokenAuthInterceptor(String.valueOf(results.get("access_token"))));
		}
		return (T) this;
	}
	
	private String getSrc(String... params) {
		return (params[0] == null ? "" : params[0]) + ":" + (params[1] == null ? "" : params[1]);
	}

	private void registerInterceptor(IClientInterceptor interceptor) {
		this.interceptors.add(interceptor);
	}

	public T addHeader(String headerName, String headerValue) {
		registerInterceptor(new SimpleRequestHeaderInterceptor(headerName, headerValue));
		return (T) this;
	}

	public String getFhirUrl() {
		return fhirUrl;
	}

	public void setFhirUrl(String fhirUrl) {
		this.fhirUrl = fhirUrl;
	}

	protected String formatURL(String query) throws UnsupportedEncodingException {
		StringBuilder encodedURL = new StringBuilder("");
		String[] pairs = query.split("&");
		for (String pair : pairs) {
			int idx = pair.indexOf('=');
			try {
				encodedURL.append(pair.substring(0, idx)).append("=")
						.append(URLEncoder.encode(pair.substring(idx + 1), "UTF-8")).append("&");
			} catch (UnsupportedEncodingException e) {
				log.error( ERROR_MSG + e.getMessage() + ". [" + e.getClass().getName() + "]");
				throw e;
			}
		}
		encodedURL = encodedURL.deleteCharAt(encodedURL.length() - 1);
		return encodedURL.toString();
	}

	public void resetInterceptors() {
		interceptors.clear();
	}

	public static CacheService<ResponseEvent<FhirResponse>> getCacheService() {
		return cacheService;
	}

	protected <S> FhirResponse<S> runWithInterceptors(QueryingCallback<S> callback, FhirClientWrapper client) {
		S result = null;
		RequestDataInterceptor tracker = new RequestDataInterceptor();
		try {
			client.inUse();
			//synchronized block is on the actual fhir client object, not the client,  
			synchronized(client.unwrap()) {
				for (IClientInterceptor i : interceptors) {
					log.debug("Registering interceptor " + i);
					client.registerInterceptor(i);
				}
				SingleAuthInterceptor singleAuth = new SingleAuthInterceptor();
				client.registerInterceptor(singleAuth);
				client.registerInterceptor(tracker);
				try {
					result = callback.execute(client);
				} catch (RuntimeException t) {
					log.error(ERROR_MSG  + t.getMessage());
				}
				for (IClientInterceptor i : interceptors) {
					log.debug("Unregistering interceptor " + i);
					client.unregisterInterceptor(i);
				}
				client.unregisterInterceptor(singleAuth);
				
				client.unregisterInterceptor(tracker);
			};
		} finally {
			client.notInUse();
		}
		return new FhirResponse<>(result, tracker.getResponseStatusCode(), tracker.getResponseStatusInfo());
	}

	public String getResourceQuery(String resourceType, String subjectId,
			String subjectRefAttribute, String fhirQuery) throws UnsupportedEncodingException {
		String queryPart = null;
		String countQuery = "&_count=50&"; 
		Pattern pattern = Pattern.compile("\\W_count=|^_count=");
		if (subjectRefAttribute != null && subjectId != null) {
			queryPart = subjectRefAttribute + "=" + subjectId;
		}
		String resourceQuery = fhirUrl + "/" + resourceType;
		if (fhirQuery != null && !"".equals(fhirQuery)) {
			fhirQuery = formatURL(fhirQuery);
			Matcher matcher = pattern.matcher(fhirQuery);
			if(matcher.find()) {
				countQuery = "";
			}
			if (queryPart != null) {
				resourceQuery = resourceQuery + "?" + fhirQuery + countQuery + queryPart;
			} else {
				resourceQuery = resourceQuery + "?" + fhirQuery + countQuery;
			}
		} else if (queryPart != null && !"".equals(queryPart)) {
			resourceQuery = resourceQuery + "?" + queryPart + countQuery;
		} else {
			resourceQuery = null;
		}
		return resourceQuery;
	}

	protected FhirResponse<U> getResourceBundle(final String resourceQuery, FhirClientWrapper client) {
		FhirResponse<U> resourceBundle = null;
		log.debug("Fetching {} resources using client for version {} ", client.getFhirContext().getVersion().getVersion().name(),
				client.getFhirContext().getVersion().getVersion().name());
		resourceBundle = runWithInterceptors(new QueryingCallback<U>() {
			@Override
			public U execute(FhirClientWrapper client) {
				return client.fetchResourceFromUrl(bundleType, resourceQuery);
			}
		}, client);
		if (resourceBundle.getResult() == null) {
			return new FhirResponse<>(null, resourceBundle.getResponseStatusCode(), resourceBundle.getResponseStatusInfo());
		}
		return resourceBundle;
	}

	public FhirResponse<IBaseResource> getResourceByIdResponseNoCache(String resourceType, String resourceId) {
		try {
			avoidCache.set(Boolean.TRUE);
			return getResourceByIdResponse(resourceType, resourceId);
		} finally {
			avoidCache.set(Boolean.FALSE);
		}
	}

	public FhirResponse<IBaseResource> getResourceByIdResponse(String resourceType, String resourceId) {
		try {
			PerformanceHelper.getInstance().beginClock(QUERYINGSERVERHELPER, "getResourceById");
			String resourceQuery = "/" + resourceType + "?_id=" + resourceId;
			FhirResponse<IBaseResource> retval = null;

			String cacheKey = CacheUtil.getCacheKey(resourceQuery, interceptors);
			if (!avoidCache.get().booleanValue() && cacheService!= null && cacheService.containsKey(cacheKey)) {
				retval = (FhirResponse<IBaseResource>) cacheService.get(cacheKey).getValue();
			} else {
				retval = getResourceList(resourceQuery);
				if(!avoidCache.get().booleanValue() && cacheService!= null && CacheUtil.isValidState(retval.getResponseStatusCode())) {
					cacheService.put(cacheKey, new ResponseEvent<>(retval, RESPONSE_EVENT_TIMEOUT));
				}
			}
			return retval;
		} catch (Exception e) {
			log.error( ERROR_MSG + e.getMessage() + ". [" + e.getClass().getName() + "]");
		} finally {
			PerformanceHelper.getInstance().endClock(QUERYINGSERVERHELPER, "getResourceById");
		}
		return new FhirResponse<>(null, -1, "not invoked");
	}
	
	private FhirResponse<IBaseResource> getResourceList(String resourceQuery) {
		FhirResponse<List<IBaseResource>> resourceList = null;
		if (ctx.getVersion().getVersion().equals(this.fhirVersionEnum)) {
			resourceList = queryServer(resourceQuery);
		}
		if(resourceList != null) {
			if (resourceList.getResult() != null && resourceList.getResult().size()==1) {
				return new FhirResponse<>(resourceList.getResult().get(0),
						resourceList.getResponseStatusCode(), resourceList.getResponseStatusInfo());
			} else {
				return new FhirResponse<>(null, resourceList.getResponseStatusCode(),
						resourceList.getResponseStatusInfo());
			}
		} else {
			return new FhirResponse<>(null, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Unable to get resources");
		}
	}

	protected FhirResponse<List<IBaseResource>> getRetVal(String resourceQuery, FhirVersionEnum fhirVersion) {
		FhirResponse<List<IBaseResource>> retval = null;
		String cacheKey = CacheUtil.getCacheKey(resourceQuery, interceptors);

		if (!avoidCache.get().booleanValue() && cacheService != null && cacheService.containsKey(cacheKey)) {
			retval = (FhirResponse<List<IBaseResource>>) cacheService.get(cacheKey).getValue();
		} else {
			if (ctx.getVersion().getVersion().equals(fhirVersion)) {
				retval = queryServer(resourceQuery);
			}
			if (!avoidCache.get().booleanValue() && cacheService != null && retval != null
					&& CacheUtil.isValidState(retval.getResponseStatusCode())) {
				cacheService.put(cacheKey, new ResponseEvent<>(retval, RESPONSE_EVENT_TIMEOUT));
			}
		}
		return retval;
	}

	public FhirResponse<List<IBaseResource>> queryResourcesResponseNoCache(String resourceType, String subjectId,
			String subjectRefAttribute, String fhirQuery) {
		try {
			avoidCache.set(Boolean.TRUE);
			return queryResourcesResponse(resourceType, subjectId, subjectRefAttribute, fhirQuery);
		} finally {
			avoidCache.set(Boolean.FALSE);
		}
	}

	public FhirResponse<List<IBaseResource>> queryResourcesResponse(String resourceType, String subjectId,
			String subjectRefAttribute, String fhirQuery) {
		try {
			PerformanceHelper.getInstance().beginClock(QUERYINGSERVERHELPER, "queryResources");
			String resourceQuery = this.getResourceQuery(resourceType, subjectId, subjectRefAttribute, fhirQuery);
			return (resourceQuery == null) ? null : getRetVal(resourceQuery, this.fhirVersionEnum);
		} catch (Exception e) {
			log.error( ERROR_MSG + e.getMessage() + ". [" + e.getClass().getName() + "]");
		} finally {
			PerformanceHelper.getInstance().endClock(QUERYINGSERVERHELPER, "queryResources");
		}
		return new FhirResponse<>(new ArrayList<>(), -1, "not invoked due to internal error");
	}
	
	public FhirFuture<FhirResponse<List<IBaseResource>>> queryResourcesAsync(String resourceType, String subjectId,
			String subjectRefAttribute, String fhirQuery, String asyncId) {
		Callable<FhirResponse<List<IBaseResource>>> callable = new Callable<FhirResponse<List<IBaseResource>>>() {
			@Override
			public FhirResponse<List<IBaseResource>> call() throws Exception {
				return queryResourcesResponse(resourceType, subjectId, subjectRefAttribute, fhirQuery);
			}
		};
		return new FhirFuture<>(asyncId, pool.submit(callable));
	}
	
	public FhirFuture<FhirResponse<IBaseResource>> getResourceByIdAsync(String resourceType, String resourceId,
			String asyncId) {
		Callable<FhirResponse<IBaseResource>> callable = new Callable<FhirResponse<IBaseResource>>() {
			@Override
			public FhirResponse<IBaseResource> call() throws Exception {
				return getResourceByIdResponse(resourceType, resourceId);
			}
		};
		return new FhirFuture<>(asyncId, pool.submit(callable));
	}

	protected static void iRestfulClientFactoryConfig(IRestfulClientFactory factory) {
		factory.setPoolMaxPerRoute(FHIR_CLIENT_CONPOOL);// default 20
		factory.setPoolMaxTotal(FHIR_CLIENT_POOLMAXTOTAL);// default 20
		factory.setConnectionRequestTimeout(FHIR_CLIENT_REQUEST_TIMEOUT);// default 10000
		factory.setConnectTimeout(FHIR_CLIENT_CONN_TIMEOUT);// default 10000
		factory.setSocketTimeout(FHIR_CLIENT_SOCKET_TIMEOUT);// default 10000
		factory.setServerValidationMode(ServerValidationModeEnum.NEVER);
	}

	public abstract FhirResponse<List<IBaseResource>> queryServer(String resourceQuery);

	public interface FhirVersionAbs {
		FhirContext getCtx();
	}

	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		if (input.readBoolean()) {
			this.fhirUrl = input.readUTF();
		}
		if (input.readBoolean()) {
			this.bundleType = (Class<U>) input.readObject();
		}
		if (input.readBoolean()) {
			this.fhirVersionEnum = FhirVersionEnum.valueOf(input.readUTF());
			this.ctx = new FhirContext(this.fhirVersionEnum);
		}
	}
	
	public void writeExternal(ObjectOutput output) throws IOException {
		output.writeBoolean(this.fhirUrl != null);
		if (this.fhirUrl != null) {
			output.writeUTF(this.fhirUrl);
		}
		output.writeBoolean(bundleType != null);
		if (bundleType != null) {
			output.writeObject(bundleType);
		}
		output.writeBoolean(this.fhirVersionEnum != null);
		if (this.fhirVersionEnum != null) {
			output.writeUTF(this.fhirVersionEnum.name());
		}
	}
}
