package mb.statix.taico.util.collections.observe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import io.usethesource.capsule.Set;
import io.usethesource.capsule.core.PersistentTrieSet;
import io.usethesource.capsule.util.EqualityComparator;

public class ObservableSet<K> implements Set.Transient<K> {
    private Set.Transient<K> collection;
    private ObserverTransientSet<ObservableSet<K>, K> observers = new ObserverTransientSet<>();
    
    public ObservableSet(Set.Transient<K> set) {
        this.collection = set;
    }
    
    public void addObserver(ObservableSet<K> set) {
        observers.addObserver(set);
    }
    
    public void removeObserver(ObservableSet<K> set) {
        observers.removeObserver(set);
    }
    
    public ObserverTransientSet<ObservableSet<K>, K> getObservers() {
        return observers;
    }

    @Override
    public boolean add(K e) {
        if (collection.add(e)) {
            observers.add(e);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
        if (collection.addAll(c)) {
            observers.addAll(c);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void clear() {
        //Use removeall to ensure consistency between set, observers and concurrent modification
        List<K> toRemove = new ArrayList<>(collection.size());
        for (K k : collection) {
            toRemove.add(k);
        }
        removeAll(toRemove);
    }

    @Override
    public Iterator<K> iterator() {
        return new ObserverIterator(collection.iterator());
    }
    
    private class ObserverIterator implements Iterator<K> {
        private final Iterator<K> iterator;
        private K lastElement;
        
        public ObserverIterator(Iterator<K> iterator) {
            this.iterator = iterator;
        }
        
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }
        
        @Override
        public K next() {
            return lastElement = iterator.next();
        }
        
        @Override
        public void remove() {
            iterator.remove();
            observers.remove(lastElement);
        }
    }

    @Override
    public boolean remove(Object o) {
        if (collection.remove(o)) {
            observers.remove(o);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if (collection.removeAll(c)) {
            observers.removeAll(c);
            return true;
        }
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        List<K> toRemove = new ArrayList<>(collection.size());
        for (K k : collection) {
            if (c.contains(k)) continue;
            toRemove.add(k);
        }
        
        if (collection.removeAll(toRemove)) {
            observers.removeAll(toRemove);
            return true;
        }
        return false;
    }

    @Override
    public Object[] toArray() {
        return collection.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return collection.toArray(a);
    }

    @Override
    public boolean equivalent(Object o, EqualityComparator<Object> cmp) {
        return collection.equivalent(o, cmp);
    }

    @Override
    public int size() {
        return collection.size();
    }

    @Override
    public boolean isEmpty() {
        return collection.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return collection.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return collection.containsAll(c);
    }

    @Override
    public K get(Object o) {
        return collection.get(o);
    }

    @Override
    public Iterator<K> keyIterator() {
        return iterator();
    }

    @Override
    public boolean __insert(K key) {
        if (collection.__insert(key)) {
            observers.__insert(key);
            return true;
        }
        
        return false;
    }

    @Override
    public boolean __remove(K key) {
        if (collection.__remove(key)) {
            observers.__remove(key);
            return true;
        }
        
        return false;
    }

    @Override
    public boolean __insertAll(java.util.Set<? extends K> set) {
        if (collection.__insertAll(set)) {
            observers.__insertAll(set);
            return true;
        }
        
        return false;
    }

    @Override
    public boolean __removeAll(java.util.Set<? extends K> set) {
        if (collection.__removeAll(set)) {
            observers.__removeAll(set);
            return true;
        }
        
        return false;
    }

    @Override
    public boolean __retainAll(java.util.Set<? extends K> set) {
        Set<K> toRemove = Set.Transient.of();
        for (K k : collection) {
            if (set.contains(k)) continue;
            toRemove.add(k);
        }
        
        if (collection.__removeAll(toRemove)) {
            observers.__removeAll(toRemove);
            return true;
        }
        return false;
    }

    @Override
    public Set.Immutable<K> freeze() {
        Set.Transient<K> clone = PersistentTrieSet.transientOf();
        for (K key : collection) {
            clone.__insert(key);
        }
        return clone.freeze();
    }
    
    @Override
    public boolean __insertAllEquivalent(java.util.Set<? extends K> set, EqualityComparator<Object> cmp) {
        if (collection.__insertAllEquivalent(set, cmp)) {
            observers.__insertAllEquivalent(set, cmp);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean __insertEquivalent(K key, EqualityComparator<Object> cmp) {
        if (collection.__insertEquivalent(key, cmp)) {
            observers.__insertEquivalent(key, cmp);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean containsEquivalent(Object o, EqualityComparator<Object> cmp) {
        return collection.containsEquivalent(o, cmp);
    }
    
    @Override
    public boolean containsAllEquivalent(Collection<?> c, EqualityComparator<Object> cmp) {
        return collection.containsAllEquivalent(c, cmp);
    }
    
    @Override
    public K getEquivalent(Object o, EqualityComparator<Object> cmp) {
        return collection.getEquivalent(o, cmp);
    }
    
    @Override
    public boolean __removeAllEquivalent(java.util.Set<? extends K> set, EqualityComparator<Object> cmp) {
        if (collection.__removeAllEquivalent(set, cmp)) {
            observers.__removeAllEquivalent(set, cmp);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean __removeEquivalent(K key, EqualityComparator<Object> cmp) {
        if (collection.__removeEquivalent(key, cmp)) {
            observers.__removeEquivalent(key, cmp);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean __retainAllEquivalent(Set.Transient<? extends K> transientSet, EqualityComparator<Object> cmp) {
        Set<K> toRemove = Set.Transient.of();
        for (K k : collection) {
            if (transientSet.containsEquivalent(k, cmp)) continue;
            toRemove.add(k);
        }
        
        return __removeAllEquivalent(toRemove, cmp);
    }
}
