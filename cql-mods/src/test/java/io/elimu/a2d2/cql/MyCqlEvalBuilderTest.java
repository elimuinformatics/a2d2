package io.elimu.a2d2.cql;

import org.junit.Assert;
import org.junit.Test;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;

public class MyCqlEvalBuilderTest {

	@Test
	public void testDecorate() {
		MyCqlEvaluatorBuilder builder = new MyCqlEvaluatorBuilder();
		TerminologyProvider decorator = builder.decorate(new TestTerminologyProvider());
		Assert.assertFalse(decorator instanceof TestTerminologyProvider);
		Assert.assertTrue(decorator instanceof ImprovedCachingTerminologyProviderDecorator);
	}
}
