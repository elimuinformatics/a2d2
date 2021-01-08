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

package io.elimu.service.dao.jpa;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import io.elimu.service.models.ServiceInfo;
import io.elimu.service.models.ServiceInfoKey;

public interface CDSServiceRepository extends CrudRepository<ServiceInfo, ServiceInfoKey> {

	@Query("from ServiceInfo c where c.id.id = ?1 and c.id.version in "
			+ "(select max(c1.id.version) from ServiceInfo c1 where c.id.id = c1.id.id and c1.status = 'done')")
	ServiceInfo findLastVersion(String id);

	@Query("from ServiceInfo c where c.id.id = ?1 and c.id.version in "
			+ "(select max(c1.id.version) from ServiceInfo c1 where c.id.id = c1.id.id)")
	ServiceInfo findLastVersionDoneOrNot(String id);

	@Override
	@Query("select count(c) from ServiceInfo c where c.id.version in (select max(c1.id.version) from ServiceInfo c1 where c.id.id = c1.id.id) and c.status = 'done'")
	long count();

	@Override
	@Query("from ServiceInfo c where c.id.version in (select max(c1.id.version) from ServiceInfo c1 where c.id.id = c1.id.id) and c.status = 'done'")
	Iterable<ServiceInfo> findAll();

	@Query("from ServiceInfo c where c.id.version in (select max(c1.id.version) from ServiceInfo c1 where c.id.id = c1.id.id) and c.serviceType = 'json' and c.status = 'done'")
	Iterable<ServiceInfo> findAllJson();

	@Query("from ServiceInfo c where c.id.version in (select max(c1.id.version) from ServiceInfo c1 where c.id.id = c1.id.id) and c.serviceType = 'kie' and c.status = 'done'")
	Iterable<ServiceInfo> findAllKie();

	@Query("from ServiceInfo c where c.id.version in (select max(c1.id.version) from ServiceInfo c1 where c.id.id = c1.id.id) and c.serviceType = 'generic' and c.status = 'done'")
	Iterable<ServiceInfo> findAllGeneric();

	@Modifying
	@Transactional
	@Query("delete from ServiceInfo c where c.id.id = ?1")
	int deleteByID(String id);

}
