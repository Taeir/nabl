package mb.statix.spoofax;

import static mb.nabl2.terms.matching.TermMatch.M;

import java.util.List;
import java.util.Optional;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;

import com.google.inject.Inject;

import mb.nabl2.terms.ITerm;
import mb.statix.solver.persistent.SolverResult;

public class STX_analysis_has_errors extends StatixPrimitive {

    @Inject public STX_analysis_has_errors() {
        super(STX_analysis_has_errors.class.getSimpleName(), 0);
    }

    @Override protected Optional<? extends ITerm> call(IContext env, ITerm term, List<ITerm> terms)
            throws InterpreterException {
        final SolverResult analysis = M.blobValue(SolverResult.class).match(term)
                .orElseThrow(() -> new InterpreterException("Expected solver result."));
        if(analysis.hasErrors()) {
            return Optional.of(term);
        } else {
            return Optional.empty();
        }
    }

}