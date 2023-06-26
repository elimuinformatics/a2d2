package io.elimu.a2d2.cql;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;
import org.opencds.cqf.cql.evaluator.builder.CqlEvaluatorBuilder;

public class MyCqlEvaluatorBuilder extends CqlEvaluatorBuilder {

	@Override
	protected TerminologyProvider decorate(TerminologyProvider terminologyProvider) {
		return new ImprovedCachingTerminologyProviderDecorator(terminologyProvider);
	}
}
