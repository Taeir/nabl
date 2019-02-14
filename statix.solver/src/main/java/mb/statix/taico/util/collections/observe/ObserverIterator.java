package mb.statix.taico.util.collections.observe;

import java.util.Iterator;

public class ObserverIterator<K> implements Iterator<K> {
    private final Iterator<K> iterator;
    private final ObserverCollection<?, K> observers;
    private K lastElement;
    
    public ObserverIterator(Iterator<K> iterator, ObserverCollection<?, K> observers) {
        this.iterator = iterator;
        this.observers = observers;
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
