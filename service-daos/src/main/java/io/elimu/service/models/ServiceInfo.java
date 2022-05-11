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

package io.elimu.service.models;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "serviceinfo")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceInfo {

    @EmbeddedId
    private ServiceInfoKey id;

    @Column(nullable = false)
    private String serviceData;

    @Column(nullable = false)
    private String serviceType;

    @Column(nullable = true)
    private String defaultCustomer;

    @Column(nullable = true)
    private String serviceCategory;

    @Column(nullable = false)
    private Long timestamp;

    @Column(nullable = false)
    private String status;

    @ElementCollection
    @CollectionTable(name="othercustomers", joinColumns= {@JoinColumn(name="ServiceId"), @JoinColumn(name="ServiceVersion")})
    @Column(name="element")
    private Set<String> otherCustomers = new HashSet<String>();
    
    public ServiceInfo() {
    }

    public ServiceInfo(String id, Long version, String serviceData, String serviceType, String defaultCustomer) {
        this.id = new ServiceInfoKey(id, version);
        this.serviceData = serviceData;
        this.serviceType = serviceType;
        this.defaultCustomer = defaultCustomer;
        this.timestamp = System.currentTimeMillis();
    }

    public ServiceInfo(String id, Long version, String serviceData, String serviceType, String defaultCustomer, String serviceCategory, String status, List<String> otherCustomers) {
    	this(id, version, serviceData, serviceType, defaultCustomer);
        this.serviceCategory=serviceCategory;
        this.status = status;
        this.otherCustomers = new HashSet<>(otherCustomers);
    }

    public ServiceInfoKey getId() {
		return id;
	}

	public void setId(ServiceInfoKey id) {
		this.id = id;
	}

	public String getServiceData() {
		return serviceData;
	}

	public void setServiceData(String serviceData) {
		this.serviceData = serviceData;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getDefaultCustomer() {
		return defaultCustomer;
	}

	public void setDefaultCustomer(String defaultCustomer) {
		this.defaultCustomer = defaultCustomer;
	}

	public String getServiceCategory() {
		return serviceCategory;
	}

	public void setServiceCategory(String serviceCategory) {
		this.serviceCategory = serviceCategory;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setOtherCustomers(Set<String> otherCustomers) {
		this.otherCustomers = otherCustomers;
	}
	
	public Set<String> getOtherCustomers() {
		return otherCustomers;
	}
	
	@Override
    public String toString() {
        return "ServiceInfo {" +
                "id=" + id +
                ", serviceType='" + serviceType + '\'' +
                ", serviceData='" + serviceData + '\'' +
                ", defaultCustomer='" + defaultCustomer + '\'' +
                ", serviceCategory='"+ serviceCategory + '\'' +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                ", otherCustomers='" + otherCustomers + "'}";
    }
}
