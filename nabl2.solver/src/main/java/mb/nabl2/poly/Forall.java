package mb.nabl2.poly;

import static mb.nabl2.terms.build.TermBuild.B;
import static mb.nabl2.terms.matching.TermMatch.M;

import java.util.List;
import java.util.Set;

import org.immutables.serial.Serial;
import org.immutables.value.Value;

import com.google.common.collect.ImmutableList;

import mb.nabl2.terms.IApplTerm;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.build.AbstractApplTerm;
import mb.nabl2.terms.matching.TermMatch.IMatcher;

@Value.Immutable
@Serial.Version(value = 42L)
public abstract class Forall extends AbstractApplTerm implements IApplTerm {

    private static final String OP = "Forall";

    // IOccurrence implementation

    @Value.Parameter public abstract Set<TypeVar> getTypeVars();

    @Value.Parameter public abstract ITerm getType();

    // IApplTerm implementation

    @Override @Value.Check protected Forall check() {
        return this;
    }

    @Value.Lazy @Override public String getOp() {
        return OP;
    }

    @Value.Lazy @Override public List<ITerm> getArgs() {
        ITerm vars = B.newList(getTypeVars());
        return ImmutableList.of(vars, getType());
    }

    public static IMatcher<Forall> matcher() {
        return M.preserveAttachments(M.appl2(OP, M.listElems(TypeVar.matcher()), M.term(), (t, vars, type) -> {
            return ImmutableForall.of(vars, type);
        }));
    }

    // Object implementation

    @Override public boolean equals(Object other) {
        return super.equals(other);
    }

    @Override public int hashCode() {
        return super.hashCode();
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("forall");
        sb.append(getTypeVars());
        sb.append(".");
        sb.append(getType());
        return sb.toString();
    }

}