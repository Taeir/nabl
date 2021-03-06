package mb.nabl2.terms.build;

import org.immutables.serial.Serial;
import org.immutables.value.Value;

import com.google.common.collect.ImmutableMultiset;

import mb.nabl2.terms.IIntTerm;
import mb.nabl2.terms.ITermVar;

@Value.Immutable
@Serial.Version(value = 42L)
abstract class IntTerm extends AbstractTerm implements IIntTerm {

    @Value.Parameter @Override public abstract int getValue();

    @Override public boolean isGround() {
        return true;
    }

    @Value.Lazy @Override public ImmutableMultiset<ITermVar> getVars() {
        return ImmutableMultiset.of();
    }

    @Override public <T> T match(Cases<T> cases) {
        return cases.caseInt(this);
    }

    @Override public <T, E extends Throwable> T matchOrThrow(CheckedCases<T, E> cases) throws E {
        return cases.caseInt(this);
    }

    @Override public int hashCode() {
        return Integer.hashCode(getValue());
    }

    @Override public boolean equals(Object other) {
        if(other == null) {
            return false;
        }
        if(!(other instanceof IIntTerm)) {
            return false;
        }
        IIntTerm that = (IIntTerm) other;
        if(getValue() != that.getValue()) {
            return false;
        }
        return true;
    }

    @Override public String toString() {
        return Integer.toString(getValue());
    }

}