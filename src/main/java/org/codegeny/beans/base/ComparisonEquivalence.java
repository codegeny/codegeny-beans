package org.codegeny.beans.base;

import java.util.Comparator;

final class ComparisonEquivalence<T> implements Equivalence<T> {

    private final Comparator<? super T> comparator;

    ComparisonEquivalence(Comparator<? super T> comparator) {
        this.comparator = comparator;
    }

    @Override
    public <R> R accept(EquivalenceVisitor<T, R> visitor) {
        return visitor.visitComparison(comparator);
    }
}
