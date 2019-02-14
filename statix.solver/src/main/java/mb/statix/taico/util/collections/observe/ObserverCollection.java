package mb.statix.taico.util.collections.observe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ObserverCollection<O extends Collection<K> , K> {
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
}
