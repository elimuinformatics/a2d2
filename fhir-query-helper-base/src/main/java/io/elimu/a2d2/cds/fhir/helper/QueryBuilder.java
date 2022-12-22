package io.elimu.a2d2.cds.fhir.helper;

import java.util.HashMap;
import java.util.Map;

public class QueryBuilder {

	private String id;
	private boolean paging = true;
	private Map<String, String> params = new HashMap<>();
	private String resourceType;
	private int count;
	private boolean useCache = true;
	private int retries = Integer.valueOf(System.getProperty("http.invoke.retries", "3"));
	private int delay = Integer.valueOf(System.getProperty("http.invoke.retrydelay", "5000"));
	
	public QueryBuilder withRetries(int retries) {
		this.retries = retries;
		return this;
	}
	
	public QueryBuilder withDelayBetweenRetries(int delay) {
		this.delay = delay;
		return this;
	}
	
	public QueryBuilder paging(boolean paging) {
		this.paging = paging;
		return this;
	}
	
	public QueryBuilder useCache(boolean useCache) {
		this.useCache = useCache;
		return this;
	}
	
	public QueryBuilder withParam(String paramName, String paramValue) {
		this.params.put(paramName, paramValue);
		return this;
	}
	
	public QueryBuilder id(String id) {
		this.id = id;
		return this;
	}
	
	public String getId() {
		return id;
	}
	
	public boolean hasParam(String paramName) {
		return this.params.containsKey(paramName);
	}
	
	public QueryBuilder resourceType(String resourceType) {
		this.resourceType = resourceType;
		return this;
	}
	
	public String getResourceType() {
		return resourceType;
	}
	
	public QueryBuilder count(int count) {
		this.count = count;
		return this;
	}
	
	public boolean hasPaging() {
		return paging;
	}
	
	public boolean useCache() {
		return useCache;
	}
	
	public int getDelay() {
		return delay;
	}
	
	public int getRetries() {
		return retries;
	}
	
	public String buildQuery(String baseUrl) {
		StringBuilder sb = new StringBuilder();
		sb.append(baseUrl).append('/').append(resourceType);
		sb.append('?');
		if (count > 0 || params.containsKey("_count")) {
			if (count > 0) {
				sb.append("_count").append('=').append(count);
			} else {
				sb.append("_count").append('=').append(params.get("_count"));
			}
		} else {
			sb.append("_count").append('=').append(50);
		}
		for (String key : params.keySet()) {
			if (!"_count".equals(key)) {
				sb.append('&').append(key).append('=').append(params.get(key));
			}
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + count;
		result = prime * result + delay;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (paging ? 1231 : 1237);
		result = prime * result + ((params == null) ? 0 : params.hashCode());
		result = prime * result + ((resourceType == null) ? 0 : resourceType.hashCode());
		result = prime * result + retries;
		result = prime * result + (useCache ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueryBuilder other = (QueryBuilder) obj;
		if (count != other.count)
			return false;
		if (delay != other.delay)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (paging != other.paging)
			return false;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		if (resourceType == null) {
			if (other.resourceType != null)
				return false;
		} else if (!resourceType.equals(other.resourceType))
			return false;
		if (retries != other.retries)
			return false;
		if (useCache != other.useCache)
			return false;
		return true;
	}
}
