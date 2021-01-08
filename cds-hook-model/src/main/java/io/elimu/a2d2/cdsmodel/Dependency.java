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

package io.elimu.a2d2.cdsmodel;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Dependency implements Serializable {

	private static final long serialVersionUID = 1L;

	private String groupId;
	private String artifactId;
	private String version;
	private String resourceType;
	private String content; //base64 encoded, for resourceType "binary"
	private String url; //for resourceType "remote"
	private String auth; //for resourceType "remote"
	private String drlContent; //for resourceType "drl"
	private String kbaseName; //for resourceType "drl"

	public Dependency() {
	}

	public Dependency(String groupId, String artifactId, String version) {
		super();
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
	}

	public Dependency(String releaseId) {
		if (releaseId == null) {
			throw new IllegalArgumentException("releaseId must not be null");
		}
		String[] split = releaseId.split(":");
		if (split.length != 3) {
			throw new IllegalArgumentException("releaseId must contain 3 parts (groupId, artifactId, and version): " + releaseId + " contains " + split.length);
		}
		this.groupId = split[0];
		this.artifactId = split[1];
		this.version = split[2];
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	public String getDrlContent() {
		return drlContent;
	}

	public void setDrlContent(String drlContent) {
		this.drlContent = drlContent;
	}

	public String getKbaseName() {
		return kbaseName;
	}

	public void setKbaseName(String kbaseName) {
		this.kbaseName = kbaseName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((artifactId == null) ? 0 : artifactId.hashCode());
		result = prime * result + ((auth == null) ? 0 : auth.hashCode());
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result
				+ ((drlContent == null) ? 0 : drlContent.hashCode());
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result
				+ ((resourceType == null) ? 0 : resourceType.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		result = prime * result + ((kbaseName == null) ? 0 : kbaseName.hashCode());
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
		Dependency other = (Dependency) obj;
		return new EqualsBuilder().appendSuper(super.equals(obj)).append(artifactId, other.artifactId)
				.append(auth, other.auth).append(content, other.content).append(drlContent, other.drlContent)
				.append(groupId, other.groupId).append(resourceType, other.resourceType).append(version, other.version)
				.append(kbaseName, other.kbaseName).isEquals();
	}

	public String getExternalForm() {
		return groupId + ":" + artifactId + ":" + version;
	}

	@Override
	public String toString() {
		return "Dependency [groupId=" + groupId + ", artifactId=" + artifactId
				+ ", version=" + version + ", resourceType=" + resourceType
				+ ", content=" + content + ", url=" + url + ", auth=" + auth
				+ ", drlContent=" + drlContent + "]";
	}
}
