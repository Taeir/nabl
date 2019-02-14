package mb.statix.taico.util.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

import mb.nabl2.util.collections.IRelation3;

/**
 * View for having multiple relations function as one relation.
 * 
 * @param <K>
 *      keys
 * @param <L>
 *      labels
 * @param <V>
 *      values
 */
public abstract class IRelation3View<K, L, V> implements IRelation3<K, L, V> {
    
    public IRelation3View() {}
    
    protected abstract Set<? extends IRelation3<K, L, V>> relations();
    
    protected abstract IRelation3<K, L, V> main();
    
    @Override
    public Set<K> keySet() {
        return new JavaSetView<K>(relations().stream().map(rel -> rel.keySet()).iterator());
    }

    @Override
    public Set<V> valueSet() {
        return new JavaSetView<V>(relations().stream().map(rel -> rel.valueSet()).iterator());
    }

    @Override
    public boolean contains(K key) {
        return relations().stream().anyMatch(rel -> rel.contains(key));
    }

    @Override
    public boolean contains(K key, L label) {
        return relations().stream().anyMatch(rel -> rel.contains(key, label));
    }

    @Override
    public boolean contains(K key, L label, V value) {
        return relations().stream().anyMatch(rel -> rel.contains(key, label, value));
    }

    @Override
    public boolean isEmpty() {
        return relations().stream().allMatch(IRelation3::isEmpty);
    }

    @Override
    public Set<? extends Entry<L, V>> get(K key) {
        for (IRelation3<K, L, V> relation : relations()) {
            Set<? extends Entry<L, V>> set = relation.get(key);
            if (set != null) return set;
        }
        return null;
    }

    @Override
    public Set<V> get(K key, L label) {
        for (IRelation3<K, L, V> relation : relations()) {
            Set<V> set = relation.get(key, label);
            if (set != null) return set;
        }
        return null;
    }
    
    public static class Immutable<K, L, V> extends IRelation3View<K, L, V> implements IRelation3.Immutable<K, L, V> {
        
        protected LinkedHashSet<IRelation3.Immutable<K, L, V>> relations = new LinkedHashSet<>();
        protected IRelation3.Immutable<K, L, V> main;
        
        public Immutable(Collection<IRelation3.Immutable<K, L, V>> relations) {
            for (IRelation3.Immutable<K, L, V> relation : relations) {
                this.relations.add(relation);
            }
        }
        
        public Immutable(Iterator<IRelation3.Immutable<K, L, V>> relations) {
            while (relations.hasNext()) {
                this.relations.add(relations.next());
            }
        }
        
        public Immutable(IRelation3.Immutable<K, L, V> main, Collection<IRelation3.Immutable<K, L, V>> relations) {
            this.main = main;
            this.relations.add(main);
            for (IRelation3.Immutable<K, L, V> relation : relations) {
                this.relations.add(relation);
            }
        }
        
        public Immutable(IRelation3.Immutable<K, L, V> main, Iterator<IRelation3.Immutable<K, L, V>> relations) {
            this.main = main;
            this.relations.add(main);
            while (relations.hasNext()) {
                this.relations.add(relations.next());
            }
        }
        
        @Override
        protected Set<IRelation3.Immutable<K, L, V>> relations() {
            return relations;
        }
        
        @Override
        protected IRelation3.Immutable<K, L, V> main() {
            return main;
        }
        
        @Override
        public IRelation3.Immutable<K, L, V> put(K key, L label, V value) {
            if (main == null) throw new UnsupportedOperationException("Cannot put values into a view whenever there is no main!");
            
            IRelation3.Immutable<K, L, V> newMain = main.put(key, label, value);
            Iterator<IRelation3.Immutable<K, L, V>> it = relations.stream().filter(rel -> rel != main).iterator();
            return new IRelation3View.Immutable<>(newMain, it);
        }

        @Override
        public IRelation3View.Transient<K, L, V> melt() {
            //TODO TAICO
            return new IRelation3View.Transient<K, L, V>()
        }
        
        @Override
        public IRelation3.Immutable<V, L, K> inverse() {
            return new IRelation3View.Immutable<V, L, K>((IRelation3.Immutable<V, L, K>) main.inverse(),
                    relations().stream().map(rel -> (IRelation3.Immutable<V, L, K>) rel.inverse()).iterator());
        }
        
    }
    
    public static class Transient<K, L, V> extends IRelation3View<K, L, V> implements IRelation3.Transient<K, L, V> {
        
        protected LinkedHashSet<IRelation3.Transient<K, L, V>> relations = new LinkedHashSet<>();
        protected IRelation3.Transient<K, L, V> main;
        
        public Transient(Collection<IRelation3.Transient<K, L, V>> relations) {
            for (IRelation3.Transient<K, L, V> relation : relations) {
                this.relations.add(relation);
            }
        }
        
        public Transient(Iterator<IRelation3.Transient<K, L, V>> relations) {
            while (relations.hasNext()) {
                this.relations.add(relations.next());
            }
        }
        
        public Transient(IRelation3.Transient<K, L, V> main, Collection<IRelation3.Transient<K, L, V>> relations) {
            this.main = main;
            this.relations.add(main);
            for (IRelation3.Transient<K, L, V> relation : relations) {
                this.relations.add(relation);
            }
        }
        
        public Transient(IRelation3.Transient<K, L, V> main, Iterator<IRelation3.Transient<K, L, V>> relations) {
            this.main = main;
            this.relations.add(main);
            while (relations.hasNext()) {
                this.relations.add(relations.next());
            }
        }
        
        @Override
        protected Set<IRelation3.Transient<K, L, V>> relations() {
            return relations;
        }
        
        @Override
        protected IRelation3.Transient<K, L, V> main() {
            return main;
        }
        
        
        @Override
        public IRelation3.Transient<V, L, K> inverse() {
            return new IRelation3View.Transient<V, L, K>((IRelation3.Transient<V, L, K>) main.inverse(),
                    relations().stream().map(rel -> (IRelation3.Transient<V, L, K>) rel.inverse()).iterator());
        }

        @Override
        public boolean put(K key, L label, V value) {
            if (main == null) throw new UnsupportedOperationException("Can only put in views when there is a main!");
            
            return main.put(key, label, value);
        }

        @Override
        public boolean putAll(IRelation3<K, L, V> other) {
            if (main == null) throw new UnsupportedOperationException("Can only put in views when there is a main!");
            
            return main.putAll(other);
        }

        @Override
        public boolean remove(K key) {
            return relations.stream().map(rel -> rel.remove(key)).anyMatch(t -> t);
        }

        @Override
        public boolean remove(K key, L label) {
            return relations.stream().map(rel -> rel.remove(key, label)).anyMatch(t -> t);
        }

        @Override
        public boolean remove(K key, L label, V value) {
            return relations.stream().map(rel -> rel.remove(key, label, value)).anyMatch(t -> t);
        }

        @Override
        public IRelation3.Immutable<K, L, V> freeze() {
            IRelation3.Immutable<K, L, V> newMain = main.freeze();
            Iterator<IRelation3.Immutable<K, L, V>> it = relations.stream().filter(rel -> rel != main).map(rel -> rel.freeze()).iterator();
            return new IRelation3View.Immutable<>(newMain, it);
        }
        
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + relations().hashCode();
        result = prime * result + main().hashCode();
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        @SuppressWarnings("unchecked")
        IRelation3View<K, L, V> other = (IRelation3View<K, L, V>) obj;
        if (!this.relations().equals(other.relations())) return false;
        if (this.main() == null) return other.main() == null;
        return this.main().equals(other.main());
    }
}
