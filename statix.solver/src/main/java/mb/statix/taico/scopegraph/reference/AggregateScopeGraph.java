package mb.statix.taico.scopegraph.reference;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.ImmutableList;

import io.usethesource.capsule.Set;
import mb.nabl2.util.collections.HashTrieRelation3;
import mb.nabl2.util.collections.IRelation3;
import mb.statix.taico.scopegraph.IMScopeGraph;
import mb.statix.util.Capsules;

public abstract class AggregateScopeGraph<V, L, R> implements IMScopeGraph<V, L, R> {

    protected AggregateScopeGraph() {
    }

    @Override public Set.Immutable<V> getAllScopes() {
        Set.Transient<V> allScopes = Set.Transient.of();
        allScopes.__insertAll(getEdges().keySet());
        allScopes.__insertAll(getEdges().valueSet());
        allScopes.__insertAll(getData().keySet());
        return allScopes.freeze();
    }

    // ------------------------------------

    public static class Immutable<V, L, R> extends AggregateScopeGraph<V, L, R>
            implements IMScopeGraph.Immutable<V, L, R>, Serializable {
        private static final long serialVersionUID = 42L;

        private final Set.Immutable<L> labels;
        private final L endOfPath;
        private final Set.Immutable<R> relations;

        private final IRelation3.Immutable<V, L, V> ownEdges;
        private final IRelation3.Immutable<V, R, List<V>> ownData;
        
        private IRelation3.Immutable<V, L, V> allEdges;
        private IRelation3.Immutable<V, R, List<V>> allData;
        
        private final Set.Immutable<IMScopeGraph.Immutable<V, L, R>> leafSGs;

        Immutable(Set.Immutable<L> labels, L endOfPath, Set.Immutable<R> relations, IRelation3.Immutable<V, L, V> ownEdges,
                IRelation3.Immutable<V, R, List<V>> ownData, Set.Immutable<IMScopeGraph.Immutable<V, L, R>> leafSGs) {
            this.labels = labels;
            this.endOfPath = endOfPath;
            assert labels.contains(endOfPath);
            this.relations = relations;
            this.ownEdges = ownEdges;
            this.ownData = ownData;
            this.leafSGs = leafSGs;
            
            //TODO TAICO: Build sets on first request instead
            HashTrieRelation3.Transient<V, L, V> edges = HashTrieRelation3.Transient.of();
            for (IMScopeGraph<V, L, R> leaf : leafSGs) {
                edges.putAll(leaf.getEdges());
            }
            edges.putAll(ownEdges);
            this.allEdges = edges.freeze();
            
            HashTrieRelation3.Transient<V, R, List<V>> data = HashTrieRelation3.Transient.of();
            for (IMScopeGraph<V, L, R> leaf : leafSGs) {
                data.putAll(leaf.getData());
            }
            data.putAll(ownData);
            this.allData = data.freeze();
        }
        
        Immutable(Set.Immutable<L> labels, L endOfPath, Set.Immutable<R> relations, IRelation3.Immutable<V, L, V> ownEdges,
                IRelation3.Immutable<V, R, List<V>> ownData, Set.Immutable<IMScopeGraph.Immutable<V, L, R>> leafSGs,
                IRelation3.Immutable<V, L, V> allEdges, IRelation3.Immutable<V, R, List<V>> allData) {
            this.labels = labels;
            this.endOfPath = endOfPath;
            assert labels.contains(endOfPath);
            this.relations = relations;
            this.ownEdges = ownEdges;
            this.ownData = ownData;
            this.leafSGs = leafSGs;
            this.allEdges = allEdges;
            this.allData = allData;
        }

        public Set.Immutable<L> getLabels() {
            return labels;
        }

        public L getEndOfPath() {
            return endOfPath;
        }

        public Set.Immutable<R> getRelations() {
            return relations;
        }
        
        @Override
        public IMScopeGraph.Immutable<V, L, R> addLeafScopeGraph(IMScopeGraph.Immutable<V, L, R> leaf) {
            return new AggregateScopeGraph.Immutable<>(labels, endOfPath, relations, ownEdges,
                    ownData, leafSGs.__insert(leaf));
        }

        @Override
        public IMScopeGraph.Immutable<V, L, R> removeLeafScopeGraph(IMScopeGraph.Immutable<V, L, R> leaf) {
            return new AggregateScopeGraph.Immutable<>(labels, endOfPath, relations, ownEdges,
                    ownData, leafSGs.__remove(leaf));
        }
        
        public Set.Immutable<? extends IMScopeGraph.Immutable<V, L, R>> getLeafScopeGraphs() {
            return leafSGs;
        }

        // ------------------------------------------------------------

        @Override public IRelation3.Immutable<V, L, V> getEdges() {
            return allEdges;
        }

        @Override public IRelation3.Immutable<V, R, List<V>> getData() {
            return allData;
        }

        // ------------------------------------------------------------

        @Override public AggregateScopeGraph.Immutable<V, L, R> addEdge(V sourceScope, L label, V targetScope) {
            return new AggregateScopeGraph.Immutable<>(labels, endOfPath, relations,
                    ownEdges.put(sourceScope, label, targetScope), ownData,
                    leafSGs, allEdges.put(sourceScope, label, targetScope), allData);
        }

        @Override public AggregateScopeGraph.Immutable<V, L, R> addDatum(V sourceScope, R relation, Iterable<V> datum) {
            return new AggregateScopeGraph.Immutable<>(labels, endOfPath, relations,
                    ownEdges, ownData.put(sourceScope, relation, ImmutableList.copyOf(datum)),
                    leafSGs, allEdges, allData.put(sourceScope, relation, ImmutableList.copyOf(datum)));
        }

        // ------------------------------------------------------------

        public AggregateScopeGraph.Transient<V, L, R> melt() {
            //Melt all the leaves
            Set.Transient<IMScopeGraph.Transient<V, L, R>> leaves = Set.Transient.of();
            leafSGs.stream()
                    .map(l -> (LeafScopeGraph.Immutable<V, L, R>) l)
                    .map(LeafScopeGraph.Immutable::melt)
                    .forEach(l -> leaves.__insert(l));
            
            return new AggregateScopeGraph.Transient<V, L, R>(labels, endOfPath, relations, ownEdges.melt(), ownData.melt(), leaves);
        }

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + allEdges.hashCode();
            result = prime * result + allData.hashCode();
            return result;
        }

        @Override public boolean equals(Object obj) {
            if(this == obj)
                return true;
            if(obj == null)
                return false;
            if(getClass() != obj.getClass())
                return false;
            @SuppressWarnings("unchecked") AggregateScopeGraph.Immutable<V, L, R> other = (AggregateScopeGraph.Immutable<V, L, R>) obj;
            if(!ownEdges.equals(other.ownEdges))
                return false;
            if(!ownData.equals(other.ownData))
                return false;
            if(!leafSGs.equals(other.leafSGs))
                return false;
            return true;
        }

        public static <V, L, R> AggregateScopeGraph.Immutable<V, L, R> of(Iterable<L> labels, L endOfPath,
                Iterable<R> relations) {
            return new AggregateScopeGraph.Immutable<>(Capsules.newSet(labels), endOfPath, Capsules.newSet(relations),
                    HashTrieRelation3.Immutable.of(), HashTrieRelation3.Immutable.of(), Set.Immutable.of());
        }

    }

    public static class Transient<V, L, R> extends AggregateScopeGraph<V, L, R> implements IMScopeGraph.Transient<V, L, R> {

        private final Set.Immutable<L> labels;
        private final L endOfPath;
        private final Set.Immutable<R> relations;

        private final IRelation3.Transient<V, L, V> edges;
        private final IRelation3.Transient<V, R, List<V>> data;
        private final Set.Transient<IMScopeGraph.Transient<V, L, R>> leafSGs;

        Transient(Set.Immutable<L> labels, L endOfPath, Set.Immutable<R> relations, IRelation3.Transient<V, L, V> edges,
                IRelation3.Transient<V, R, List<V>> data, Set.Transient<IMScopeGraph.Transient<V, L, R>> leafSGs) {
            this.labels = labels;
            this.endOfPath = endOfPath;
            assert labels.contains(endOfPath);
            this.relations = relations;
            this.edges = edges;
            this.data = data;
            this.leafSGs = leafSGs;
        }

        public Set.Immutable<L> getLabels() {
            return labels;
        }

        public L getEndOfPath() {
            return endOfPath;
        }

        public Set.Immutable<R> getRelations() {
            return relations;
        }
        
        // ------------------------------------------------------------
        
        @Override
        public boolean addLeafScopeGraph(IMScopeGraph.Transient<V, L, R> leaf) {
            return leafSGs.__insert(leaf);
        }
        
        @Override
        public boolean removeLeafScopeGraph(IMScopeGraph.Transient<V, L, R> leaf) {
            return leafSGs.__remove(leaf);
        }
        
        @Override
        public Set.Transient<IMScopeGraph.Transient<V, L, R>> getLeafScopeGraphs() {
            return leafSGs;
        }

        // ------------------------------------------------------------

        @Override public IRelation3<V, L, V> getEdges() {
          //TODO TAICO: Return view of own edges + leaf edges
            return edges;
        }

        @Override public IRelation3<V, R, List<V>> getData() {
            //TODO TAICO: Return view of own data + leaf data
            return data;
        }

        // ------------------------------------------------------------

        @Override public boolean addEdge(V sourceScope, L label, V targetScope) {
            return edges.put(sourceScope, label, targetScope);
        }

        @Override public boolean addDatum(V scope, R relation, Iterable<V> datum) {
            return data.put(scope, relation, ImmutableList.copyOf(datum));
        }

        @Override public boolean addAll(IMScopeGraph<V, L, R> other) {
            boolean change = false;
            change |= edges.putAll(other.getEdges());
            change |= data.putAll(other.getData());
            return change;
        }

        // ------------------------------------------------------------

        public AggregateScopeGraph.Immutable<V, L, R> freeze() {
            Set.Transient<IMScopeGraph.Immutable<V, L, R>> leaves = Set.Transient.of();
            leafSGs.stream()
                    .map(l -> (LeafScopeGraph.Transient<V, L, R>) l)
                    .map(LeafScopeGraph.Transient::freeze)
                    .forEach(l -> leaves.__insert(l));
            leaves.freeze();
            
            return new AggregateScopeGraph.Immutable<V, L, R>(labels, endOfPath, relations, edges.freeze(), data.freeze(), leaves.freeze());
        }

        public static <V, L, R> AggregateScopeGraph.Transient<V, L, R> of(Set.Immutable<L> labels, L endOfPath,
                Set.Immutable<R> relations) {
            return new AggregateScopeGraph.Transient<>(labels, endOfPath, relations, HashTrieRelation3.Transient.of(),
                    HashTrieRelation3.Transient.of(), Set.Transient.of());
        }

    }

}