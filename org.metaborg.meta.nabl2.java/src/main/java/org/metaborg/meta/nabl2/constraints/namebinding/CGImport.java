package org.metaborg.meta.nabl2.constraints.namebinding;

import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.metaborg.meta.nabl2.constraints.IConstraint;
import org.metaborg.meta.nabl2.constraints.MessageInfo;
import org.metaborg.meta.nabl2.scopegraph.terms.Label;
import org.metaborg.meta.nabl2.terms.ITerm;
import org.metaborg.meta.nabl2.unification.IUnifier;

@Value.Immutable
@Serial.Version(value = 42L)
public abstract class CGImport implements INamebindingConstraint {

    @Value.Parameter public abstract ITerm getScope();

    @Value.Parameter public abstract Label getLabel();

    @Value.Parameter public abstract ITerm getReference();

    @Value.Parameter @Override public abstract MessageInfo getMessageInfo();

    @Override public IConstraint find(IUnifier unifier) {
        return ImmutableCGImport.of(unifier.find(getScope()), getLabel(), unifier.find(getReference()),
                getMessageInfo());
    }

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