package org.codegeny.beans.base;

import java.util.Comparator;

public interface EquivalenceVisitor<T, R> {

    R visitIdentity();

    R visitEquality();

    R visitComparison(Comparator<? super T> comparator);
}
