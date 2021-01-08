package io.elimu.cdshookapi.entity;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class CDSService {

	private String hook;
	
	private String title;
	
	private String description;
	
	private String id;
	
	private Map<String, String> prefetch;

	public String getHook() {
		return hook;
	}

	public void setHook(String hook) {
		this.hook = hook;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Map<String, String> getPrefetch() {
		return prefetch;
	}

	public void setPrefetch(Map<String, String> prefetch) {
		this.prefetch = prefetch;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((hook == null) ? 0 : hook.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((prefetch == null) ? 0 : prefetch.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		CDSService other = (CDSService) obj;
		return new EqualsBuilder().appendSuper(super.equals(obj)).append(description, other.description)
				.append(hook, other.hook).append(id, other.id).append(prefetch, other.prefetch)
				.append(title, other.title).isEquals();
	}

	@Override
	public String toString() {
		return "CDSService [hook=" + hook + ", title=" + title + ", description=" + description + ", id=" + id
				+ ", prefetch=" + prefetch + "]";
	}

	

}
