package org.metaborg.meta.nabl2.constraints.namebinding;

import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.metaborg.meta.nabl2.constraints.IConstraint;
import org.metaborg.meta.nabl2.terms.ITerm;

@Value.Immutable
@Serial.Structural
public abstract class Import implements INamebindingConstraint {

    @Value.Parameter public abstract ITerm getScope();

    @Value.Parameter public abstract ITerm getLabel();

    @Value.Parameter public abstract ITerm getReference();

    @Override public <T> T match(Cases<T> cases) {
        return cases.caseImport(this);
    }

    @Override public <T> T match(IConstraint.Cases<T> cases) {
        return cases.caseNamebinding(this);
    }

    @Override public <T, E extends Throwable> T matchOrThrow(CheckedCases<T,E> cases) throws E {
        return cases.caseImport(this);
    }

    @Override public <T, E extends Throwable> T matchOrThrow(IConstraint.CheckedCases<T,E> cases) throws E {
        return cases.caseNamebinding(this);
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getScope());
        sb.append("=");
        sb.append(getLabel());
        sb.append("=>");
        sb.append(getReference());
        return sb.toString();
    }

}