package mb.statix.taico.util.collections.observe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import io.usethesource.capsule.Set;
import io.usethesource.capsule.util.EqualityComparator;

public class ObserverTransientSet<O extends Set.Transient<K> , K> {
    private List<O> observers = new ArrayList<>();

    public void addObserver(O o) {
        observers.add(o);
    }
    
    public void removeObserver(O o) {
        observers.remove(o);
    }
    
    public int observerCount() {
        return observers.size();
    }
    
    public Iterator<O> observerIterator() {
        return observers.iterator();
    }
    
    public void add(K e) {
        for (O observer : observers) {
            observer.add(e);
        }
    }

    public void addAll(Collection<? extends K> c) {
        for (O observer : observers) {
            observer.addAll(c);
        }
    }

    public void remove(Object o) {
        for (O observer : observers) {
            observer.remove(o);
        }
    }

    public void removeAll(Collection<?> c) {
        for (O observer : observers) {
            observer.removeAll(c);
        }
    }

    public void __insert(K key) {
        for (O observer : observers) {
            observer.__insert(key);
        }
    }

    public void __remove(K key) {
        for (O observer : observers) {
            observer.__remove(key);
        }
    }

    public void __insertAll(java.util.Set<? extends K> set) {
        for (O observer : observers) {
            observer.__insertAll(set);
        }
    }

    public void __removeAll(java.util.Set<? extends K> set) {
        for (O observer : observers) {
            observer.__removeAll(set);
        }
    }
    
    public void __insertAllEquivalent(java.util.Set<? extends K> set, EqualityComparator<Object> cmp) {
        for (O observer : observers) {
            observer.__insertAllEquivalent(set, cmp);
        }
    }
    
    public void __insertEquivalent(K key, EqualityComparator<Object> cmp) {
        for (O observer : observers) {
            observer.__insertEquivalent(key, cmp);
        }
    }
    
    public void __removeEquivalent(K key, EqualityComparator<Object> cmp) {
        for (O observer : observers) {
            observer.__removeEquivalent(key, cmp);
        }
    }
    
    public void __removeAllEquivalent(java.util.Set<? extends K> set, EqualityComparator<Object> cmp) {
        for (O observer : observers) {
            observer.__removeAllEquivalent(set, cmp);
        }
    }
}
