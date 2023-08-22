package io.elimu.a2d2.cql;

import java.util.HashSet;
import java.util.Set;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.cql.engine.fhir.retrieve.RestFhirRetrieveProvider;
import org.opencds.cqf.cql.engine.fhir.searchparam.SearchParameterMap;
import org.opencds.cqf.cql.engine.fhir.searchparam.SearchParameterResolver;

import ca.uhn.fhir.rest.client.api.IGenericClient;

public class CustomRestFhirRetrieveProvider extends RestFhirRetrieveProvider {

	public CustomRestFhirRetrieveProvider(SearchParameterResolver searchParameterResolver, IGenericClient fhirClient) {
		super(searchParameterResolver, fhirClient);
	}

	@Override
	protected IBaseResource queryById(String dataType, SearchParameterMap map) {
		if (map.containsKey("_id") && map.entrySet().size() > 1) {
			Set<String> keys = new HashSet<>(map.keySet());
			for (String key : keys) {
				if (!"_id".equals(key)) {
					map.remove(key);
				}
			}
		}
		return super.queryById(dataType, map);
	}

}
