package org.metaborg.meta.nabl2.constraints.namebinding;

import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.metaborg.meta.nabl2.constraints.IConstraint;
import org.metaborg.meta.nabl2.constraints.messages.IMessageContent;
import org.metaborg.meta.nabl2.constraints.messages.IMessageInfo;
import org.metaborg.meta.nabl2.constraints.messages.MessageContent;
import org.metaborg.meta.nabl2.terms.ITerm;
import org.metaborg.meta.nabl2.terms.ITermVar;

import io.usethesource.capsule.Set;

@Value.Immutable
@Serial.Version(value = 42L)
public abstract class CResolve implements INamebindingConstraint {

    @Value.Parameter public abstract ITerm getReference();

    @Value.Parameter public abstract ITerm getDeclaration();

    @Value.Parameter @Override public abstract IMessageInfo getMessageInfo();

    @Override public Set.Immutable<ITermVar> getVars() {
        return getReference().getVars().__insertAll(getDeclaration().getVars());
    }

    @Override public <T> T match(Cases<T> cases) {
        return cases.caseResolve(this);
    }

    @Override public <T> T match(IConstraint.Cases<T> cases) {
        return cases.caseNamebinding(this);
    }

    @Override public <T, E extends Throwable> T matchOrThrow(CheckedCases<T, E> cases) throws E {
        return cases.caseResolve(this);
    }

    @Override public <T, E extends Throwable> T matchOrThrow(IConstraint.CheckedCases<T, E> cases) throws E {
        return cases.caseNamebinding(this);
    }

    @Override public IMessageContent pp() {
        return MessageContent.builder().append(getReference()).append(" |-> ").append(getDeclaration()).build();
    }

    @Override public String toString() {
        return pp().toString();
    }

}