package mb.statix.taico.util.collections;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class JavaSetView<K> implements Set<K> {
    private LinkedHashSet<Set<K>> sets = new LinkedHashSet<>();
    private Set<K> main;
    
    public JavaSetView(Collection<Set<K>> sets) {
        this.sets.addAll(sets);
    }
    
    public JavaSetView(Set<K> main, Collection<Set<K>> sets) {
        if (main != null) this.sets.add(main);
        this.main = main;
        this.sets.addAll(sets);
    }
    
    public JavaSetView(Iterator<Set<K>> sets) {
        while (sets.hasNext()) {
            this.sets.add(sets.next());
        }
    }
    
    public JavaSetView(Set<K> main, Iterator<Set<K>> sets) {
        if (main != null) this.sets.add(main);
        this.main = main;
        while (sets.hasNext()) {
            this.sets.add(sets.next());
        }
    }

    @Override
    public boolean add(K e) {
        if (main == null) throw new UnsupportedOperationException("Cannot add to set view without main!");
        
        return main.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
        if (main == null) throw new UnsupportedOperationException("Cannot add to set view without main!");
        
        return main.addAll(c);
    }

    @Override
    public void clear() {
        for (Set<K> set : sets) {
            set.clear();
        }
    }

    @Override
    public boolean contains(Object o) {
        return sets.stream().anyMatch(set -> set.contains(o));
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return sets.stream().anyMatch(set -> set.containsAll(c));
    }

    @Override
    public boolean isEmpty() {
        return sets.stream().allMatch(Set::isEmpty);
    }

    @Override
    public Iterator<K> iterator() {
        return MultiIterator.fromCollections(sets);
    }

    @Override
    public boolean remove(Object o) {
        return sets.stream().map(set -> set.remove(o)).anyMatch(t -> t);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return sets.stream().map(set -> set.removeAll(c)).anyMatch(t -> t);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return sets.stream().map(set -> set.retainAll(c)).anyMatch(t -> t);
    }

    @Override
    public int size() {
        return sets.stream().mapToInt(Set::size).sum();
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
    public int hashCode() {
        return sets.hashCode();
    }
    
    //TODO TAICO: Equals and toString
    
}
