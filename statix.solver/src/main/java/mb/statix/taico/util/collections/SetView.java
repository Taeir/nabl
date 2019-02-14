package mb.statix.taico.util.collections;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import io.usethesource.capsule.Set;
import io.usethesource.capsule.util.EqualityComparator;

public abstract class SetView<K> implements Set<K> {
    
    protected Set<K> main;
    
    protected abstract Set<? extends Set<K>> sets();
    
    /**
     * Sets the set that will be modified when additions are made to this set.
     * 
     * @param set
     *      the new main set
     */
    public void setMain(Set<K> set) {
        if (set != null && !sets().contains(set)) throw new IllegalArgumentException("The given set must be part of this view!");
        
        this.main = set;
    }

    @Override
    public boolean add(K e) {
        if (main == null) throw new UnsupportedOperationException("Cannot add to a view with no main!");
        
        return this.main.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
        if (main == null) throw new UnsupportedOperationException("Cannot add to a view with no main!");
        
        return this.main.addAll(c);
    }

    @Override
    public void clear() {
        for (Set<K> set : sets()) {
            set.clear();
        }
    }

    @Override
    public Iterator<K> iterator() {
        return MultiIterator.fromCollections(sets());
    }

    @Override
    public boolean remove(Object o) {
        return sets().stream().map(set -> set.remove(o)).anyMatch(t -> t);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return sets().stream().map(set -> set.removeAll(c)).anyMatch(t -> t);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return sets().stream().map(set -> set.retainAll(c)).anyMatch(t -> t);
    }

    @Override
    public Object[] toArray() {
        int size = size();
        Object[] array = new Object[size];
        Iterator<K> it = iterator();
        for (int i = 0; i < size; i++) {
            if (!it.hasNext()) return Arrays.copyOf(array, i);
            array[i] = it.next();
        }
        
        //TODO Taico: consider case where there are more new elements.
        return array;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        int size = size();
        T[] array = a.length >= size ? a : (T[]) Array.newInstance(a.getClass().getComponentType(), size);
        
        Iterator<K> it = iterator();
        for (int i = 0; i < size; i++) {
            if (!it.hasNext()) { // fewer elements than expected
                if (a == array) {
                    array[i] = null; //null-terminate
                } else if (a.length < i) {
                    return Arrays.copyOf(array, i);
                } else {
                    System.arraycopy(array, 0, a, 0, i);
                    if (a.length > i) {
                        a[i] = null;
                    }
                }
                return a;
            }
            array[i] = (T) it.next();
        }
        
      //TODO Taico: consider case where there are more new elements.
        return array;
    }

    @Override
    public boolean equivalent(Object o, EqualityComparator<Object> cmp) {
        if (o == this) return true;
        if (o == null) return false;
        if (o instanceof java.util.Set) {
            java.util.Set<?> that = (java.util.Set<?>) o;
            if (this.size() != that.size()) return false;
            //TODO TAICO: IMPORTANT
        }
        return true;
    }

    @Override
    public int size() {
        return sets().stream().mapToInt(Set::size).sum();
    }

    @Override
    public boolean isEmpty() {
        return sets().stream().allMatch(Set::isEmpty);
    }

    @Override
    public boolean contains(Object o) {
        return sets().stream().anyMatch(s -> s.contains(o));
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public K get(Object o) {
        for (Set<K> set : sets()) {
            K k = set.get(o);
            if (k != null) return k;
        }
        return null;
    }

    @Override
    public Iterator<K> keyIterator() {
        return iterator();
    }

    @Override
    public boolean containsAllEquivalent(Collection<?> c, EqualityComparator<Object> cmp) {
        for (Object o : c) {
            if (!containsEquivalent(o, cmp)) return false;
        }
        return true;
    }
    
    @Override
    public boolean containsEquivalent(Object o, EqualityComparator<Object> cmp) {
        return sets().stream().anyMatch(set -> set.containsEquivalent(o, cmp));
    }
    
    @Override
    public K getEquivalent(Object o, EqualityComparator<Object> cmp) {
        for (Set<K> set : sets()) {
            K k = set.getEquivalent(o, cmp);
            if (k != null) return k;
        }
        return null;
    }
    
    //TODO TAICO: Hashcode, Equals, ToString
    
    public static class Immutable<K> extends SetView<K> implements Set.Immutable<K> {
        
        protected Set<Set.Immutable<K>> sets = Set.Transient.of();
        
        public Immutable(Collection<Set.Immutable<K>> sets) {
            this.sets.addAll(sets);
        }
        
        public Immutable(Iterator<Set.Immutable<K>> sets) {
            while (sets.hasNext()) {
                this.sets.add(sets.next());
            }
        }
        
        public Immutable(Set.Immutable<K> mainSet, Iterator<Set.Immutable<K>> sets) {
            while (sets.hasNext()) {
                this.sets.add(sets.next());
            }
            if (mainSet != null) this.sets.add(mainSet);
            this.main = mainSet;
        }
        
        @Override
        protected Set<Set.Immutable<K>> sets() {
            return sets;
        }
        
        protected Set.Immutable<K> main() {
            return (Set.Immutable<K>) main;
        }
        
        @Override
        public void setMain(Set<K> set) {
            if (!(set instanceof Set.Immutable)) throw new IllegalArgumentException("Main must be immutable!");
            
            super.setMain(set);
        }

        @Override
        public Set.Immutable<K> __insert(K key) {
            if (main == null) throw new UnsupportedOperationException("Cannot add to a view with no main!");
            
            return new SetView.Immutable<>(main().__insert(key), sets().stream().filter(imset -> imset != main).iterator());
        }

        @Override
        public Set.Immutable<K> __remove(K key) {
            if (main == null) {
                return new SetView.Immutable<>(sets().stream().map(set -> set.__remove(key)).iterator());
            }
            
            Iterator<Set.Immutable<K>> it = sets.stream().filter(set -> set != main).map(set -> set.__remove(key)).iterator();
            Set.Immutable<K> newMain = main().__remove(key);
            
            return new SetView.Immutable<>(newMain, it);
        }

        @Override
        public Set.Immutable<K> __insertAll(java.util.Set<? extends K> set) {
            if (main == null) throw new UnsupportedOperationException("Cannot add to a view with no main!");
            
            return new SetView.Immutable<>(main().__insertAll(set), sets().stream().filter(imset -> imset != main).iterator());
        }

        @Override
        public Set.Immutable<K> __removeAll(java.util.Set<? extends K> set) {
            if (main == null) {
                return new SetView.Immutable<>(sets().stream().map(imset -> imset.__removeAll(set)).iterator());
            }
            
            Iterator<Set.Immutable<K>> it = sets.stream().filter(imset -> imset != main).map(imset -> imset.__removeAll(set)).iterator();
            Set.Immutable<K> newMain = main().__removeAll(set);
            
            return new SetView.Immutable<>(newMain, it);
        }

        @Override
        public Set.Immutable<K> __retainAll(java.util.Set<? extends K> set) {
            if (main == null) {
                return new SetView.Immutable<>(sets().stream().map(imset -> imset.__retainAll(set)).iterator());
            }
            
            Iterator<Set.Immutable<K>> it = sets.stream().filter(imset -> imset != main).map(imset -> imset.__retainAll(set)).iterator();
            Set.Immutable<K> newMain = main().__retainAll(set);
            
            return new SetView.Immutable<>(newMain, it);
        }

        @Override
        public boolean isTransientSupported() {
            return true;
        }

        @Override
        public Set.Transient<K> asTransient() {
            //Convert all sets except for the main set
            Iterator<Set.Transient<K>> it = sets.stream().filter(set -> set != main).map(set -> set.asTransient()).iterator();
            //We need to handle the main set separately, since it needs to be identity equivalent to the one in the sets.
            Set.Transient<K> newMain = main == null ? null : main().asTransient();
            return new SetView.Transient<K>(newMain, it);
        }
    }
    
    public static class Transient<K> extends SetView<K> implements Set.Transient<K> {

        protected Set<Set.Transient<K>> sets = Set.Transient.of();
        
        public Transient(Collection<Set.Transient<K>> sets) {
            this.sets.addAll(sets);
        }
        
        public Transient(Set.Transient<K> main, Iterator<Set.Transient<K>> sets) {
            while (sets.hasNext()) {
                this.sets.add(sets.next());
            }
            if (main != null) this.sets.add(main);
            this.main = main;
        }
        
        public Transient(Iterator<Set.Transient<K>> sets) {
            while (sets.hasNext()) {
                this.sets.add(sets.next());
            }
        }
        
        @Override
        protected Set<Set.Transient<K>> sets() {
            return sets;
        }
        
        protected Set.Transient<K> main() {
            return (Set.Transient<K>) main;
        }
        
        @Override
        public boolean __insert(K key) {
            if (main == null) throw new UnsupportedOperationException("Cannot add to a view with no main!");
            
            return main().__insert(key);
        }

        @Override
        public boolean __remove(K key) {
            return sets.stream().map(imset -> imset.__remove(key)).anyMatch(t -> t);
        }

        @Override
        public boolean __insertAll(java.util.Set<? extends K> set) {
            if (main == null) throw new UnsupportedOperationException("Cannot add to a view with no main!");
            
            return main().__insertAll(set);
        }

        @Override
        public boolean __removeAll(java.util.Set<? extends K> set) {
            return sets.stream().map(imset -> imset.__removeAll(set)).anyMatch(t -> t);
        }

        @Override
        public boolean __retainAll(java.util.Set<? extends K> set) {
            return sets.stream().map(imset -> imset.__retainAll(set)).anyMatch(t -> t);
        }

        @Override
        public Set.Immutable<K> freeze() {
            return new SetView.Immutable<>(main == null ? null : main().freeze(),
                    sets.stream().filter(set -> set != main).map(set -> set.freeze()).iterator());
        }
    }
}
