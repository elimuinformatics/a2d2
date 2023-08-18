package io.elimu.a2d2.cql;

import java.util.List;

import org.opencds.cqf.cql.engine.fhir.searchparam.SearchParameterResolver;
import org.opencds.cqf.cql.engine.retrieve.RetrieveProvider;
import org.opencds.cqf.cql.evaluator.builder.Constants;
import org.opencds.cqf.cql.evaluator.builder.data.TypedRetrieveProviderFactory;
import org.opencds.cqf.cql.evaluator.fhir.ClientFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class CustomFhirRestRetrieveProviderFactory implements TypedRetrieveProviderFactory {

    FhirContext fhirContext;
    ClientFactory clientFactory;

    public CustomFhirRestRetrieveProviderFactory(FhirContext fhirContext, ClientFactory clientFactory){
        this.fhirContext = fhirContext;
        this.clientFactory = clientFactory;
    }

    @Override
    public String getType() {
        return Constants.HL7_FHIR_REST;
    }

    @Override
    public RetrieveProvider create(String url, List<String> headers) {
       IGenericClient fhirClient = this.clientFactory.create(url, headers);
       return new CustomRestFhirRetrieveProvider(new SearchParameterResolver(this.fhirContext), fhirClient);
    }
}
