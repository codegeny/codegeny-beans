/*-
 * #%L
 * codegeny-beans
 * %%
 * Copyright (C) 2016 - 2018 Codegeny
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.codegeny.beans.model.visitor;

import org.codegeny.beans.model.BeanModel;
import org.codegeny.beans.model.ListModel;
import org.codegeny.beans.model.MapModel;
import org.codegeny.beans.model.ModelVisitor;
import org.codegeny.beans.model.Property;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Comparator.nullsLast;

/**
 * Compare 2 objects whose structures are expressed by the same {@link org.codegeny.beans.model.Model}&gt;T&lt;.
 *
 * @param <T> The common type of the 2 objects to compare (also the model type).
 * @author Xavier DURY
 */
public final class CompareModelVisitor<T> implements ModelVisitor<T, Integer> {

    /**
     * The left value to compare.
     */
    private final T left;

    /**
     * The right value to compare.
     */
    private final T right;

    /**
     * Constructor.
     *
     * @param left  The left value to compare.
     * @param right The right value to compare.
     */
    public CompareModelVisitor(T left, T right) {
        this.left = left;
        this.right = right;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer visitBean(BeanModel<T> bean) {
        return bean.getProperties().stream().mapToInt(this::visitProperty).filter(i -> i != 0).findFirst().orElse(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer visitValue(ValueModel<T> value) {
        return nullsLast(value).compare(left, right);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Integer visitMap(MapModel<T, K, V> map) {
        Map<K, V> leftMap = map.toMap(left);
        Map<K, V> rightMap = map.toMap(right);
        Set<K> keys = new TreeSet<>(map.getKeyModel());
        keys.addAll(leftMap.keySet());
        keys.addAll(rightMap.keySet());
        Comparator<V> valueComparator = map.getValueModel();
        return keys.stream().mapToInt(k -> valueComparator.compare(leftMap.get(k), rightMap.get(k))).filter(i -> i != 0).findFirst().orElse(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> Integer visitSet(SetModel<T, E> values) {
        Comparator<E> comparator = values.getElementModel();
        Iterator<E> leftIterator = values.toSet(left).stream().sorted(comparator).iterator();
        Iterator<E> rightIterator = values.toSet(right).stream().sorted(comparator).iterator();
        return compareIterators(comparator, leftIterator, rightIterator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> Integer visitList(ListModel<T, E> values) {
        Comparator<E> comparator = values.getElementModel();
        Iterator<E> leftIterator = values.toList(left).stream().iterator();
        Iterator<E> rightIterator = values.toList(right).stream().iterator();
        return compareIterators(comparator, leftIterator, rightIterator);
    }

    /**
     * Visit a {@link BeanModel}'s property.
     *
     * @param property The property.
     * @param <P>      The property type.
     * @return An integer indicating the result of the comparison.
     */
    private <P> Integer visitProperty(Property<? super T, P> property) {
        return property.getModel().compare(property.get(left), property.get(right));
    }

    /**
     * Perform the comparison on the 2 given iterators using the given comparator.
     *
     * @param comparator    The comparator used to compare elements from the iterators.
     * @param leftIterator  The iterator of left values.
     * @param rightIterator The iterator of right values.
     * @param <E>           The type of objects returned by the 2 iterators.
     * @return An integer indicating the result of the comparison.
     */
    private <E> Integer compareIterators(Comparator<E> comparator, Iterator<E> leftIterator, Iterator<E> rightIterator) {
        while (leftIterator.hasNext() && rightIterator.hasNext()) {
            int comparison = comparator.compare(leftIterator.next(), rightIterator.next());
            if (comparison != 0) {
                return comparison;
            }
        }
        return leftIterator.hasNext() ? -1 : rightIterator.hasNext() ? +1 : 0;
    }
}
