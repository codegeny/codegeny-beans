package org.codegeny.beans.base;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

public interface Equivalence<T> extends BiPredicate<T, T> {

    <R> R accept(EquivalenceVisitor<T, R> visitor);

    static <T> Equivalence<T> byIdentity() {
        return new IdentityEquivalence<>();
    }

    static <T> Equivalence<T> byEquality() {
        return new EqualityEquivalence<>();
    }

    static <T> Equivalence<T> byComparison(Comparator<? super T> comparator) {
        return new ComparisonEquivalence<>(comparator);
    }

    static <T extends Comparable<? super T>> Equivalence<T> byComparison() {
        return byComparison(Comparator.naturalOrder());
    }

    default Set<T> newSet() {
        return accept(new SetMaker<>());
    }

    default <V> Map<T, V> newMap() {
        return accept(new MapMaker<>());
    }

    @Override
    default boolean test(T left, T right) {
        return accept(new EqualityVisitor<>(left, right));
    }
}
