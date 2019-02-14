package mb.statix.taico.solver;

import mb.statix.solver.Delay;
import mb.statix.solver.SolverResult;

/**
 * Solver interface for the module based solver.
 * 
 * @author taico
 */
public interface ISolver<S, L, R> {
	/**
	 * @param scope
	 * 		the scope 
	 * @param edgeLabel
	 * 		the edge label
	 * 
	 * @return
	 * 		if the requested edge is a critical edge
	 */
	public boolean isCritical(S scope, L edgeLabel);
	
	/**
	 * Solves the current module.
	 * 
	 * @return
	 * @throws Delay
	 * 		If this solver cannot complete solving and requires more information.
	 */
	SolverResult solve() throws Delay;
}
