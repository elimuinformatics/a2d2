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

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import io.elimu.service.models.ServiceResources;

@Repository
public interface ServiceRulesRepository extends CrudRepository<ServiceResources, Integer> {

	List<ServiceResources> findAllByServiceName(String serviceName);

	ServiceResources findByServiceName(String serviceName);

	<S extends ServiceResources> S save(S serviceRules);

	ServiceResources findById(int id);

}
