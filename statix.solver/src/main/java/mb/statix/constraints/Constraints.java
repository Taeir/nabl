package mb.statix.constraints;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.functions.CheckedFunction1;
import org.metaborg.util.functions.Function1;

import com.google.common.collect.ImmutableList;

import mb.nabl2.terms.substitution.ISubstitution;
import mb.nabl2.util.TermFormatter;
import mb.statix.solver.IConstraint;

public final class Constraints {

    private Constraints() {
    }

    // @formatter:off
    public static <R> IConstraint.Cases<R> cases(
                Function1<CConj,R> onConj,
                Function1<CEqual,R> onEqual,
                Function1<CExists,R> onExists,
                Function1<CFalse,R> onFalse,
                Function1<CInequal,R> onInequal,
                Function1<CNew,R> onNew,
                Function1<CPathLt,R> onPathLt,
                Function1<CPathMatch,R> onPathMatch,
                Function1<CResolveQuery,R> onResolveQuery,
                Function1<CTellEdge,R> onTellEdge,
                Function1<CTellRel,R> onTellRel,
                Function1<CAstId,R> onTermId,
                Function1<CAstProperty,R> onTermProperty,
                Function1<CTrue,R> onTrue,
                Function1<CUser,R> onUser
            ) {
        return new IConstraint.Cases<R>() {

            @Override public R caseConj(CConj c) {
                return onConj.apply(c);
            }

            @Override public R caseEqual(CEqual c) {
                return onEqual.apply(c);
            }

            @Override public R caseExists(CExists c) {
                return onExists.apply(c);
            }

            @Override public R caseFalse(CFalse c) {
                return onFalse.apply(c);
            }

            @Override public R caseInequal(CInequal c) {
                return onInequal.apply(c);
            }

            @Override public R caseNew(CNew c) {
                return onNew.apply(c);
            }

            @Override public R casePathLt(CPathLt c) {
                return onPathLt.apply(c);
            }

            @Override public R casePathMatch(CPathMatch c) {
                return onPathMatch.apply(c);
            }

            @Override public R caseResolveQuery(CResolveQuery c) {
                return onResolveQuery.apply(c);
            }

            @Override public R caseTellEdge(CTellEdge c) {
                return onTellEdge.apply(c);
            }

            @Override public R caseTellRel(CTellRel c) {
                return onTellRel.apply(c);
            }

            @Override public R caseTermId(CAstId c) {
                return onTermId.apply(c);
            }

            @Override public R caseTermProperty(CAstProperty c) {
                return onTermProperty.apply(c);
            }

            @Override public R caseTrue(CTrue c) {
                return onTrue.apply(c);
            }

            @Override public R caseUser(CUser c) {
                return onUser.apply(c);
            }

        };
    }
    // @formatter:on

    // @formatter:off
    public static <R, E extends Throwable> IConstraint.CheckedCases<R, E> checkedCases(
                CheckedFunction1<CConj, R, E> onConj,
                CheckedFunction1<CEqual, R, E> onEqual,
                CheckedFunction1<CExists, R, E> onExists,
                CheckedFunction1<CFalse, R, E> onFalse,
                CheckedFunction1<CInequal, R, E> onInequal,
                CheckedFunction1<CNew, R, E> onNew,
                CheckedFunction1<CPathLt, R, E> onPathLt,
                CheckedFunction1<CPathMatch, R, E> onPathMatch,
                CheckedFunction1<CResolveQuery, R, E> onResolveQuery,
                CheckedFunction1<CTellEdge, R, E> onTellEdge,
                CheckedFunction1<CTellRel, R, E> onTellRel,
                CheckedFunction1<CAstId, R, E> onTermId,
                CheckedFunction1<CAstProperty, R, E> onTermProperty,
                CheckedFunction1<CTrue, R, E> onTrue,
                CheckedFunction1<CUser, R, E> onUser
            ) {
        return new IConstraint.CheckedCases<R, E>() {

            @Override public R caseConj(CConj c) throws E {
                return onConj.apply(c);
            }

            @Override public R caseEqual(CEqual c) throws E {
                return onEqual.apply(c);
            }

            @Override public R caseExists(CExists c) throws E {
                return onExists.apply(c);
            }

            @Override public R caseFalse(CFalse c) throws E {
                return onFalse.apply(c);
            }

            @Override public R caseInequal(CInequal c) throws E {
                return onInequal.apply(c);
            }

            @Override public R caseNew(CNew c) throws E {
                return onNew.apply(c);
            }

            @Override public R casePathLt(CPathLt c) throws E {
                return onPathLt.apply(c);
            }

            @Override public R casePathMatch(CPathMatch c) throws E {
                return onPathMatch.apply(c);
            }

            @Override public R caseResolveQuery(CResolveQuery c) throws E {
                return onResolveQuery.apply(c);
            }

            @Override public R caseTellEdge(CTellEdge c) throws E {
                return onTellEdge.apply(c);
            }

            @Override public R caseTellRel(CTellRel c) throws E {
                return onTellRel.apply(c);
            }

            @Override public R caseTermId(CAstId c) throws E {
                return onTermId.apply(c);
            }

            @Override public R caseTermProperty(CAstProperty c) throws E {
                return onTermProperty.apply(c);
            }

            @Override public R caseTrue(CTrue c) throws E {
                return onTrue.apply(c);
            }

            @Override public R caseUser(CUser c) throws E {
                return onUser.apply(c);
            }

        };
    }
    // @formatter:on

    public static List<IConstraint> apply(List<IConstraint> constraints, ISubstitution.Immutable subst) {
        return Constraints.apply(constraints, subst, null);
    }

    public static List<IConstraint> apply(List<IConstraint> constraints, ISubstitution.Immutable subst,
            @Nullable IConstraint cause) {
        return constraints.stream().map(c -> c.apply(subst).withCause(cause)).collect(ImmutableList.toImmutableList());
    }

    public static String toString(Iterable<? extends IConstraint> constraints, TermFormatter termToString) {
        final StringBuilder sb = new StringBuilder();
        boolean first = true;
        for(IConstraint constraint : constraints) {
            if(!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(constraint.toString(termToString));
        }
        return sb.toString();
    }

    public static IConstraint conjoin(Iterable<? extends IConstraint> constraints) {
        // FIXME What about causes? Unfolding this conjunction might overwrite
        //       causes in the constraints by null.
        IConstraint conj = null;
        for(IConstraint constraint : constraints) {
            conj = (conj != null) ? new CConj(constraint, conj) : constraint;
        }
        return (conj != null) ? conj : new CTrue();
    }

}