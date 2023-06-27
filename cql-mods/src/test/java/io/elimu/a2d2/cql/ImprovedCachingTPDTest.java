package io.elimu.a2d2.cql;

import org.junit.Assert;
import org.junit.Test;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;
import org.opencds.cqf.cql.engine.terminology.ValueSetInfo;

public class ImprovedCachingTPDTest {

	@Test
	public void testReturnCodes() {
		TerminologyProvider myProvider = new TestTerminologyProvider();
		ImprovedCachingTerminologyProviderDecorator decorator = new ImprovedCachingTerminologyProviderDecorator(myProvider);
		ValueSetInfo valueSet = new ValueSetInfo();
		valueSet.setId("https://elimu.io/ValueSet/MyId");
		Iterable<Code> codes = decorator.expand(valueSet);
		Assert.assertNotNull(codes);
		Assert.assertSame(codes, decorator.expand(valueSet));
	}
}
