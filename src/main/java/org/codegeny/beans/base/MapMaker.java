package org.codegeny.beans.base;

import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.TreeMap;

final class MapMaker<K, V> implements EquivalenceVisitor<K, Map<K, V>> {

    @Override
    public Map<K, V> visitIdentity() {
        return new IdentityHashMap<>();
    }

    @Override
    public Map<K, V> visitEquality() {
        return new HashMap<>();
    }

    @Override
    public Map<K, V> visitComparison(Comparator<? super K> comparator) {
        return new TreeMap<>(comparator);
    }
}
