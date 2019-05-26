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

import org.codegeny.beans.model.*;

import java.util.*;

import static java.util.Comparator.nullsLast;

public final class CompareModelVisitor<T> implements ModelVisitor<T, Integer> {

    private final T left, right;

    public CompareModelVisitor(T left, T right) {
        this.left = left;
        this.right = right;
    }

    public Integer visitBean(BeanModel<T> bean) {
        return bean.getProperties().stream().mapToInt(this::visitProperty).filter(i -> i != 0).findFirst().orElse(0);
    }

    private <P> Integer visitProperty(Property<? super T, P> property) {
        return new ModelComparator<>(property.getModel()).compare(property.get(left), property.get(right));
    }

    public Integer visitValue(ValueModel<T> value) {
        return nullsLast(value).compare(left, right);
    }

    public <K, V> Integer visitMap(MapModel<T, K, V> map) {
        Map<? extends K, ? extends V> leftMap = map.toMap(this.left);
        Map<? extends K, ? extends V> rightMap = map.toMap(this.right);
        Set<K> keys = new TreeSet<>(new ModelComparator<>(map.getKeyModel()));
        keys.addAll(leftMap.keySet());
        keys.addAll(rightMap.keySet());
        Comparator<? super V> valueComparator = new ModelComparator<>(map.getValueModel());
        return keys.stream().mapToInt(k -> valueComparator.compare(leftMap.get(k), rightMap.get(k))).filter(i -> i != 0).findFirst().orElse(0);
    }

    public <E> Integer visitSet(SetModel<T, E> values) {
        Comparator<? super E> comparator = new ModelComparator<>(values.getElementModel());
        Iterator<? extends E> leftIterator = values.toSet(this.left).stream().sorted(comparator).iterator();
        Iterator<? extends E> rightIterator = values.toSet(this.right).stream().sorted(comparator).iterator();
        while (leftIterator.hasNext() && rightIterator.hasNext()) {
            int comparison = comparator.compare(leftIterator.next(), rightIterator.next());
            if (comparison != 0) {
                return comparison;
            }
        }
        return leftIterator.hasNext() ? -1 : rightIterator.hasNext() ? +1 : 0;
    }

    public <E> Integer visitList(ListModel<T, E> values) {
        Comparator<? super E> comparator = new ModelComparator<>(values.getElementModel());
        Iterator<? extends E> leftIterator = values.toList(this.left).stream().iterator();
        Iterator<? extends E> rightIterator = values.toList(this.right).stream().iterator();
        while (leftIterator.hasNext() && rightIterator.hasNext()) {
            int comparison = comparator.compare(leftIterator.next(), rightIterator.next());
            if (comparison != 0) {
                return comparison;
            }
        }
        return leftIterator.hasNext() ? -1 : rightIterator.hasNext() ? +1 : 0;
    }
}
