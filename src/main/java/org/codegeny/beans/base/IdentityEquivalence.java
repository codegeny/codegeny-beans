package org.codegeny.beans.base;

final class IdentityEquivalence<T> implements Equivalence<T> {

    @Override
    public <R> R accept(EquivalenceVisitor<T, R> visitor) {
        return visitor.visitIdentity();
    }
}
