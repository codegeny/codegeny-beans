package org.codegeny.beans.base;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.TreeSet;

final class SetMaker<E> implements EquivalenceVisitor<E, Set<E>> {

    @Override
    public Set<E> visitIdentity() {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }

    @Override
    public Set<E> visitEquality() {
        return new HashSet<>();
    }

    @Override
    public Set<E> visitComparison(Comparator<? super E> comparator) {
        return new TreeSet<>(comparator);
    }
}
