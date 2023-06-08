package io.elimu.a2d2.cql;
import java.util.HashMap;
import java.util.Map;

import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.terminology.CodeSystemInfo;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;
import org.opencds.cqf.cql.engine.terminology.ValueSetInfo;

public class ImprovedCachingTerminologyProviderDecorator implements TerminologyProvider  {

    private Map<String, Iterable<Code>> valueSetIndexById = new HashMap<>();

    private TerminologyProvider innerProvider;

    public ImprovedCachingTerminologyProviderDecorator(TerminologyProvider terminologyProvider) {
    	this.innerProvider = terminologyProvider;
    }

    @Override
    public boolean in(Code code, ValueSetInfo valueSet) {
        if (!valueSetIndexById.containsKey(valueSet.getId())) {
            this.expand(valueSet);
        }

        Iterable<Code> codes = valueSetIndexById.get(valueSet.getId());

        if (codes == null) {
            return false;
        }
        for (Code c : codes) {
            if (c.getCode().equals(code.getCode()) && c.getSystem().equals(code.getSystem())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Iterable<Code> expand(ValueSetInfo valueSet) {
    	String originalId = valueSet.getId();
        if (!valueSetIndexById.containsKey(originalId)) {
        	Iterable<Code> codes = this.innerProvider.expand(valueSet);
            valueSetIndexById.put(originalId, codes);
            valueSetIndexById.put(valueSet.getId(), codes);
        }
        return valueSetIndexById.get(valueSet.getId());
    }

    @Override
    public Code lookup(Code code, CodeSystemInfo codeSystem) {
        return this.innerProvider.lookup(code, codeSystem);
    }
}
