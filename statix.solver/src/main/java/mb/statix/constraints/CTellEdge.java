package mb.statix.constraints;

import java.io.Serializable;
import java.util.Optional;

import javax.annotation.Nullable;

import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.substitution.ISubstitution;
import mb.nabl2.util.TermFormatter;
import mb.statix.solver.IConstraint;

public class CTellEdge implements IConstraint, Serializable {
    private static final long serialVersionUID = 1L;

    private final ITerm sourceTerm;
    private final ITerm label;
    private final ITerm targetTerm;

    private final @Nullable IConstraint cause;

    public CTellEdge(ITerm sourceTerm, ITerm label, ITerm targetTerm) {
        this(sourceTerm, label, targetTerm, null);
    }

    public CTellEdge(ITerm sourceTerm, ITerm label, ITerm targetTerm, @Nullable IConstraint cause) {
        this.sourceTerm = sourceTerm;
        this.label = label;
        this.targetTerm = targetTerm;
        this.cause = cause;
    }

    public ITerm sourceTerm() {
        return sourceTerm;
    }

    public ITerm label() {
        return label;
    }

    public ITerm targetTerm() {
        return targetTerm;
    }

    @Override public Optional<IConstraint> cause() {
        return Optional.ofNullable(cause);
    }

    @Override public CTellEdge withCause(@Nullable IConstraint cause) {
        return new CTellEdge(sourceTerm, label, targetTerm, cause);
    }

    @Override public <R> R match(Cases<R> cases) {
        return cases.caseTellEdge(this);
    }

    @Override public <R, E extends Throwable> R matchOrThrow(CheckedCases<R, E> cases) throws E {
        return cases.caseTellEdge(this);
    }

    @Override public CTellEdge apply(ISubstitution.Immutable subst) {
        return new CTellEdge(subst.apply(sourceTerm), label, subst.apply(targetTerm), cause);
    }

    @Override public String toString(TermFormatter termToString) {
        final StringBuilder sb = new StringBuilder();
        sb.append(termToString.format(sourceTerm));
        sb.append(" -");
        sb.append(termToString.format(label));
        sb.append("-> ");
        sb.append(termToString.format(targetTerm));
        return sb.toString();
    }

    @Override public String toString() {
        return toString(ITerm::toString);
    }

}