package org.metaborg.meta.nabl2.terms.generic;

import java.util.List;

import org.immutables.value.Value;
import org.metaborg.meta.nabl2.terms.IApplTerm;
import org.metaborg.meta.nabl2.terms.ITerm;
import org.metaborg.meta.nabl2.terms.ITermIndex;
import org.metaborg.meta.nabl2.terms.Terms.IMatcher;
import org.metaborg.meta.nabl2.terms.Terms.M;

import com.google.common.collect.ImmutableList;

@Value.Immutable
public abstract class TermIndex extends AbstractApplTerm implements ITermIndex, IApplTerm {

    private static final String OP = "TermIndex";

    // ITermIndex implementation

    @Value.Parameter public abstract String getResource();

    @Value.Parameter public abstract int getId();

    // IApplTerm implementation

    @Override public String getOp() {
        return OP;
    }

    @Value.Lazy @Override public List<ITerm> getArgs() {
        return ImmutableList.of(GenericTerms.newString(getResource()), GenericTerms.newInt(getId()));
    }

    public static IMatcher<TermIndex> matcher() {
        return M.appl2("TermIndex", M.stringValue(), M.integerValue(), (t, resource, id) -> ImmutableTermIndex.of(
                resource, id).setAttachments(t.getAttachments()));
    }

    // Object implementation

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("@");
        sb.append(getResource());
        sb.append(":");
        sb.append(getId());
        return sb.toString();
    }

}
