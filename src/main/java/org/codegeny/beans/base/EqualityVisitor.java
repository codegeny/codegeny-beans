package org.codegeny.beans.base;

import java.util.Comparator;
import java.util.Objects;

final class EqualityVisitor<T> implements EquivalenceVisitor<T, Boolean> {

    private final T left;
    private final T right;

    EqualityVisitor(T left, T right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public Boolean visitIdentity() {
        return left == right;
    }

    @Override
    public Boolean visitEquality() {
        return Objects.equals(left, right);
    }

    @Override
    public Boolean visitComparison(Comparator<? super T> comparator) {
        return Comparator.nullsLast(comparator).compare(left, right) == 0;
    }
}
