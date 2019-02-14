package mb.statix.taico.util.collections.observe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mb.nabl2.util.collections.IRelation3;

public class ObserverTrieRelation3<O extends IRelation3.Transient<K, L, V> , K, L, V> {
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

    public void put(K key, L label, V value) {
        for (O observer : observers) {
            observer.put(key, label, value);
        }
    }

    public void putAll(IRelation3<K, L, V> other) {
        for (O observer : observers) {
            observer.putAll(other);
        }
    }
    
    public void remove(K key) {
        for (O observer : observers) {
            observer.remove(key);
        }
    }

    public void remove(K key, L label) {
        for (O observer : observers) {
            observer.remove(key, label);
        }
    }

    public void remove(K key, L label, V value) {
        for (O observer : observers) {
            observer.remove(key, label, value);
        }
    }
}
