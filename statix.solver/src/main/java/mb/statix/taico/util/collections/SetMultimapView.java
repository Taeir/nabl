package mb.statix.taico.util.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import io.usethesource.capsule.SetMultimap;
import io.usethesource.capsule.Set;

public class SetMultimapView<K, V> implements SetMultimap<K, V> {

    private Set<SetMultimap<K, V>> multimaps = Set.Transient.of();
    
    public SetMultimapView(Collection<SetMultimap<K, V>> multimaps) {
        this.multimaps.addAll(multimaps);
    }
    
    @Override
    public int size() {
        return multimaps.stream().mapToInt(SetMultimap::size).sum();
    }

    @Override
    public boolean isEmpty() {
        return multimaps.stream().allMatch(SetMultimap::isEmpty);
    }

    @Override
    public boolean containsKey(Object o) {
        return multimaps.stream().anyMatch(sm -> sm.containsKey(o));
    }

    @Override
    public boolean containsValue(Object o) {
        return multimaps.stream().anyMatch(sm -> sm.containsValue(o));
    }

    @Override
    public boolean containsEntry(Object o0, Object o1) {
        return multimaps.stream().anyMatch(sm -> sm.containsEntry(o0, o1));
    }

    @Override
    public Set.Immutable<V> get(Object o) {
        for (SetMultimap<K, V> map : multimaps) {
            Set.Immutable<V> v = map.get(o);
            if (v != null) return v;
        }
        return null;
    }

    @Override
    public Set<K> keySet() {
        // TODO Auto-generated method stub
        return null;
    }
    
    private class KeySetView extends Set<K> {
        
    }

    @Override
    public Collection<V> values() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<K> keyIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<V> valueIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<Entry<K, V>> entryIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> Iterator<T> tupleIterator(BiFunction<K, V, T> dataConverter) {
        // TODO Auto-generated method stub
        return null;
    }

}
