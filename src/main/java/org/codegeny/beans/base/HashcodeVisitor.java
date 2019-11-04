package org.codegeny.beans.base;

import java.util.Comparator;

final class HashcodeVisitor<T> implements EquivalenceVisitor<T, Integer> {

    private final T target;

    HashcodeVisitor(T target) {
        this.target = target;
    }

    @Override
    public Integer visitIdentity() {
        return System.identityHashCode(target);
    }

    @Override
    public Integer visitEquality() {
        return target.hashCode();
    }

    @Override
    public Integer visitComparison(Comparator<? super T> comparator) {
        return 0;
    }
}
