package mb.statix.taico.scopegraph;

import java.util.List;

import io.usethesource.capsule.Set;
import mb.nabl2.util.collections.IRelation3;

/**
 * Interface to represent a scope graph.
 * 
 * <p>A scope graph consists of the following:
 * <ul>
 * <li>A set of labels {@link #getLabels()}</li>
 * <li>A set of relations {@link #getRelations()}</li>
 * <li>A set of scopes {@link #getAllScopes()}</li>
 * <li>A set of edges (scope -label> scope) {@link #getEdges()}</li>
 * <li>A set of data lists (scope -relation> data) {@link #getData()}</li>
 * <ul>
 *
 * @param <V>
 *      the type of scopes
 * @param <L>
 *      the type of labels
 * @param <R>
 *      the type of relations
 */
public interface IMScopeGraph<V, L, R> {

    Set.Immutable<L> getLabels();

    L getEndOfPath();

    Set.Immutable<R> getRelations();

    Set<V> getAllScopes();

    IRelation3<V, L, V> getEdges();

    IRelation3<V, R, List<V>> getData();
    
    Set<? extends IMScopeGraph<V, L, R>> getLeafScopeGraphs();

    /**
     * @see IMScopeGraph
     */
    interface Immutable<V, L, R> extends IMScopeGraph<V, L, R> {

        @Override Set.Immutable<V> getAllScopes();

        @Override IRelation3.Immutable<V, L, V> getEdges();

        @Override IRelation3.Immutable<V, R, List<V>> getData();
        
        /**
         * Creates a (shallow) copy of this immutable scope graph with the given scope graph added
         * as a leaf.
         * 
         * @param leaf
         *      the scope graph to add as leaf
         * 
         * @return
         *      the copy with the given leaf added
         */
        Immutable<V, L, R> addLeafScopeGraph(IMScopeGraph.Immutable<V, L, R> leaf);
        
        /**
         * Creates a (shallow) copy of this immutable scope graph with the given scope graph
         * removed from the leaves.
         * 
         * @param leaf
         *      the scope graph to remove
         * 
         * @return
         *      the copy with the given leaf removed
         */
        Immutable<V, L, R> removeLeafScopeGraph(IMScopeGraph.Immutable<V, L, R> leaf);

        /**
         * Creates a copy of this immutable scope graph with the given edge added.
         * 
         * @param sourceScope
         *      the source scope of the edge
         * @param label
         *      the label of the edge
         * @param targetScope
         *      the target scope of the edge
         * 
         * @return
         *      the copy with the given edge added
         */
        Immutable<V, L, R> addEdge(V sourceScope, L label, V targetScope);

        /**
         * Creates a copy of this immutable scope graph with the given data added.
         * 
         * @param sourceScope
         *      the source scope
         * @param relation
         *      the relation
         * @param datum
         *      the data
         * 
         * @return
         *      the copy with the given relation added
         */
        Immutable<V, L, R> addDatum(V scope, R relation, Iterable<V> datum);

        /**
         * Melts this IMScopeGraph.
         * 
         * @return
         *      the transient version of this IMScopeGraph
         */
        Transient<V, L, R> melt();
    }

    /**
     * @see IMScopeGraph
     */
    interface Transient<V, L, R> extends IMScopeGraph<V, L, R> {
        
        /**
         * Adds the given scope graph to this aggregate as a leaf.
         * 
         * @param leaf
         *      the scope graph to add as leaf
         * 
         * @return
         *      true if the scope graph changed as a result of this call
         */
        boolean addLeafScopeGraph(IMScopeGraph.Transient<V, L, R> leaf);
        
        /**
         * Removes the given scope graph from this aggregate.
         * 
         * @param leaf
         *      the scope graph to remove
         * 
         * @return
         *      true if the scope graph changed as a result of this call
         */
        boolean removeLeafScopeGraph(IMScopeGraph.Transient<V, L, R> leaf);

        /**
         * @param sourceScope
         *      the source scope
         * @param label
         *      the label
         * @param targetScope
         *      the target scope
         * 
         * @return
         *      true if this edge was added, false if it already existed
         */
        boolean addEdge(V sourceScope, L label, V targetScope);

        /**
         * @param scope
         *      the scope
         * @param relation
         *      the relation
         * @param datum
         *      the datum
         * 
         * @return
         *      true if this scope graph changed as a result of this call, false otherwise
         */
        boolean addDatum(V scope, R relation, Iterable<V> datum);

        /**
         * Add all scopes, edges and relations from the given scope graph to this scope graph.
         * 
         * @param other
         *      the other scope graph
         * 
         * @return
         *      true if this scope graph changed as a result of this call, false otherwise
         */
        boolean addAll(IMScopeGraph<V, L, R> other);

        // -----------------------

        /**
         * Freezes this IMScopeGraph.
         * 
         * @return
         *      an Immutable version of this scope graph
         */
        IMScopeGraph.Immutable<V, L, R> freeze();

    }

}