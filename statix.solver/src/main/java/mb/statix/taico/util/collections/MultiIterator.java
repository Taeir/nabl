package mb.statix.taico.util.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Iterator to combine multiple iterators into one.
 * 
 *
 * @param <K>
 */
public class MultiIterator<K> implements Iterator<K> {

    private final List<Iterator<K>> iterators = new ArrayList<>();
    private Iterator<Iterator<K>> collectionIterator;
    private Iterator<K> itemIterator;
    
    public MultiIterator(Collection<Iterator<K>> iterators) {
        if (iterators.size() == 0) {
            this.collectionIterator = Collections.emptyIterator();
            this.itemIterator = Collections.emptyIterator();
        } else {
            this.iterators.addAll(iterators);
            this.collectionIterator = iterators.iterator();
            this.itemIterator = this.collectionIterator.next();
        }
    }
    
    @Override
    public boolean hasNext() {
        if (itemIterator.hasNext()) return true;
        if (!collectionIterator.hasNext()) return false;
        
        itemIterator = collectionIterator.next();
        return hasNext();
    }

    @Override
    public K next() {
        if (!hasNext()) throw new NoSuchElementException();
        
        return itemIterator.next();
    }
    
    @Override
    public void remove() {
        itemIterator.remove();
    }
    
    public static <K> MultiIterator<K> fromCollections(Collection<? extends Collection<K>> collections) {
        return new MultiIterator<K>(collections.stream().map(Collection::iterator).collect(Collectors.toList()));
    }
}
