package org.codegeny.beans.base;

final class EqualityEquivalence<T> implements Equivalence<T> {

    @Override
    public <R> R accept(EquivalenceVisitor<T, R> visitor) {
        return visitor.visitEquality();
    }
}
