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
}
