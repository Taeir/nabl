package mb.statix.taico.module;

import java.util.Set;
import java.util.stream.StreamSupport;

import mb.nabl2.terms.ITerm;
import mb.statix.taico.paths.IQuery;
import mb.statix.taico.scopegraph.IMInternalScopeGraph;
import mb.statix.taico.scopegraph.IOwnableScope;
import mb.statix.taico.scopegraph.IOwnableTerm;

/**
 * Interface to represent a module.
 */
public interface IModule {
    String getId();
    
    Set<IQuery<IOwnableTerm, ITerm, ITerm, ITerm>> queries();
    
    IModule getParent();
    
    Set<IModule> getChildren();
    
    IMInternalScopeGraph<IOwnableTerm, ITerm, ITerm, ITerm> getScopeGraph();
    
    /**
     * @return
     *      all the modules that are descendent from this module
     */
    default Iterable<IModule> getDescendants() {
        return getChildren().stream()
                .flatMap(m -> StreamSupport.stream(m.getDescendants().spliterator(), false))
                ::iterator;
    }
    
    /**
     * @param canExtend
     *      the scopes from this module and parents that the child can extend
     * @return
     *      the child
     */
    IModule createChild(io.usethesource.capsule.Set.Immutable<IOwnableScope> canExtend);
}