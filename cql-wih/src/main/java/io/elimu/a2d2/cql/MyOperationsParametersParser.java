package io.elimu.a2d2.cql;

import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.cql.engine.fhir.converter.FhirTypeConverter;
import org.opencds.cqf.cql.evaluator.fhir.adapter.AdapterFactory;
import org.opencds.cqf.cql.evaluator.plandefinition.r4.OperationParametersParser;

public class MyOperationsParametersParser extends OperationParametersParser {

	public MyOperationsParametersParser(AdapterFactory adapterFactory, FhirTypeConverter fhirTypeConverter) {
		super(adapterFactory, fhirTypeConverter);
	}

	@Override
	public IBaseDatatype getValueChild(IBaseParameters parameters, String name) {
		return super.getValueChild(parameters, name);
	}
	
	@Override
	public IBaseResource getResourceChild(IBaseParameters parameters, String name) {
		return super.getResourceChild(parameters, name);
	}

}
