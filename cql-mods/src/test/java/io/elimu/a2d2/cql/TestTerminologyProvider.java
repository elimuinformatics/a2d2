package io.elimu.a2d2.cql;

import java.util.Arrays;

import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.terminology.CodeSystemInfo;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;
import org.opencds.cqf.cql.engine.terminology.ValueSetInfo;

public class TestTerminologyProvider implements TerminologyProvider {

	@Override
	public Code lookup(Code code, CodeSystemInfo codeSystem) {
		return null;
	}
	
	@Override
	public boolean in(Code code, ValueSetInfo valueSet) {
		return false;
	}
	
	@Override
	public Iterable<Code> expand(ValueSetInfo valueSet) {
		String originalId = valueSet.getId();
		valueSet.setId(originalId.substring(originalId.lastIndexOf("/") + 1));
		return Arrays.asList(new Code().withCode("1234"), new Code().withCode("4321"));
	}
}
