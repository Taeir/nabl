package mb.statix.spoofax;

import static mb.nabl2.terms.build.TermBuild.B;
import static mb.nabl2.terms.matching.TermMatch.M;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;

import com.google.inject.Inject;

import mb.nabl2.terms.ITerm;
import mb.statix.solver.IConstraint;
import mb.statix.solver.Solver;
import mb.statix.solver.SolverResult;
import mb.statix.solver.State;
import mb.statix.solver.log.IDebugContext;

public class MSTX_solve_multi_project extends StatixPrimitive {

    @Inject public MSTX_solve_multi_project() {
        super(MSTX_solve_multi_project.class.getSimpleName(), 2);
    }
    
    @Override
    protected Optional<? extends ITerm> call(IContext env, ITerm term, List<ITerm> terms) throws InterpreterException {
        // TODO Implement multi file analysis
        return null;
    }

//    @Override protected Optional<? extends ITerm> call(IContext env, ITerm term, List<ITerm> terms)
//            throws InterpreterException {
//
//        final SolverResult initial = M.blobValue(SolverResult.class).match(terms.get(0))
//                .orElseThrow(() -> new InterpreterException("Expected solver result."));
//
//        final IDebugContext debug = getDebugContext(terms.get(1));
//
//        final List<SolverResult> results = M.listElems(M.blobValue(SolverResult.class)).match(term)
//                .orElseThrow(() -> new InterpreterException("Expected list of solver results."));
//
//        final Set<IConstraint> constraints = new HashSet<>(initial.delays().keySet());
//        final Set<IConstraint> errors = new HashSet<>(initial.errors());
//        State state = initial.state();
//        for(SolverResult result : results) {
//            state = state.add(result.state());
//            constraints.addAll(result.delays().keySet());
//            errors.addAll(result.errors());
//        }
//
//        final SolverResult resultConfig;
//        try {
//            resultConfig = Solver.solve(state, constraints, (s, l, st) -> true, debug);
//        } catch(InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        errors.addAll(resultConfig.errors());
//        final ITerm resultTerm = B.newBlob(resultConfig.withErrors(errors));
//        return Optional.of(resultTerm);
//    }

}