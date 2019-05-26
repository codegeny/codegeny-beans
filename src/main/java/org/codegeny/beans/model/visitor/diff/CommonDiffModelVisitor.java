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
package org.codegeny.beans.model.visitor.diff;

import org.codegeny.beans.diff.Diff;
import org.codegeny.beans.diff.Diff.Status;
import org.codegeny.beans.model.*;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.codegeny.beans.diff.Diff.Status.*;

public abstract class CommonDiffModelVisitor<T> implements ModelVisitor<T, Diff<T>> {

    private static abstract class AbstractDiffModelVisitor<T> implements ModelVisitor<T, Diff<T>> {

        private final T left, right, target;
        private final Status type;

        AbstractDiffModelVisitor(T left, T right, T target, Status type) {
            this.left = left;
            this.right = right;
            this.target = target;
            this.type = type;
        }

        protected abstract <N> AbstractDiffModelVisitor<N> create(N value);

        public Diff<T> visitBean(BeanModel<T> bean) {
            return Diff.bean(type, left, right, bean.getProperties().stream().collect(toMap(Property::getName, this::visitProperty)));
        }

        public <E> Diff<T> visitSet(SetModel<T, E> values) {
            return Diff.list(type, left, right, values.toSet(target).stream().map(e -> values.acceptElement(create(e))).collect(toList()));
        }

        public <E> Diff<T> visitList(ListModel<T, E> values) {
            return Diff.list(type, left, right, values.toList(target).stream().map(e -> values.acceptElement(create(e))).collect(toList()));
        }

        public <K, V> Diff<T> visitMap(MapModel<T, K, V> map) {
            return Diff.map(type, left, right, map.toMap(target).entrySet().stream().collect(toMap(Map.Entry::getKey, e -> map.acceptValue(create(e.getValue())))));
        }

        private <P> Diff<P> visitProperty(Property<? super T, P> property) {
            return property.accept(create(property.get(target)));
        }

        public Diff<T> visitValue(ValueModel<T> value) {
            return Diff.simple(type, left, right);
        }
    }

    protected static class AddedDiffModelVisitor<T> extends AbstractDiffModelVisitor<T> {

        AddedDiffModelVisitor(T right) {
            super(null, right, right, ADDED);
        }

        protected <N> AbstractDiffModelVisitor<N> create(N value) {
            return new AddedDiffModelVisitor<>(value);
        }
    }

    protected static class NullDiffModelVisitor<T> implements ModelVisitor<T, Diff<T>> {

        public Diff<T> visitBean(BeanModel<T> bean) {
            return Diff.bean(UNCHANGED, null, null, bean.getProperties().stream().collect(toMap(Property::getName, p -> p.accept(new NullDiffModelVisitor<>()))));
        }

        public <E> Diff<T> visitSet(SetModel<T, E> collection) {
            return Diff.list(UNCHANGED, null, null, emptyList());
        }

        public <E> Diff<T> visitList(ListModel<T, E> collection) {
            return Diff.list(UNCHANGED, null, null, emptyList());
        }

        public <K, V> Diff<T> visitMap(MapModel<T, K, V> map) {
            return Diff.map(UNCHANGED, null, null, emptyMap());
        }

        public Diff<T> visitValue(ValueModel<T> value) {
            return Diff.simple(UNCHANGED, null, null);
        }
    }

    protected static class RemovedDiffModelVisitor<T> extends AbstractDiffModelVisitor<T> {

        RemovedDiffModelVisitor(T left) {
            super(left, null, left, REMOVED);
        }

        protected <N> AbstractDiffModelVisitor<N> create(N value) {
            return new RemovedDiffModelVisitor<>(value);
        }
    }

    private static <X> X throwingBinaryOperator(X left, X right) {
        throw new IllegalStateException(String.format("Duplicate key %s", left));
    }

    static Status toStatus(Collection<? extends Diff<?>> diffs) {
        return diffs.stream().map(Diff::getStatus).reduce(Status::combine).orElse(UNCHANGED);
    }

    protected final T left, right;

    CommonDiffModelVisitor(T left, T right) {
        this.left = left;
        this.right = right;
    }

    protected abstract <S> ModelVisitor<S, Diff<S>> newVisitor(S left, S right);

    public Diff<T> visitBean(BeanModel<T> bean) {
        if (left == null ^ right == null) {
            return bean.accept(left == null ? new AddedDiffModelVisitor<>(right) : new RemovedDiffModelVisitor<>(left));
        }
        Map<String, Diff<?>> properties = bean.getProperties().stream().collect(toMap(Property::getName, this::visitProperty, CommonDiffModelVisitor::throwingBinaryOperator, LinkedHashMap::new));
        return Diff.bean(toStatus(properties.values()), this.left, this.right, properties);
    }

    @Override
    public <K, V> Diff<T> visitMap(MapModel<T, K, V> map) {
        if (left == null ^ right == null) {
            return map.accept(left == null ? new AddedDiffModelVisitor<>(right) : new RemovedDiffModelVisitor<>(left));
        }
        Map<? extends K, ? extends V> leftMap = map.toMap(left);
        Map<? extends K, ? extends V> rightMap = map.toMap(right);
        Set<K> keys = new HashSet<>();
        keys.addAll(leftMap.keySet());
        keys.addAll(rightMap.keySet());
        Map<K, Diff<V>> result = keys.stream().collect(toMap(k -> k, k -> map.acceptValue(newVisitor(leftMap.get(k), rightMap.get(k)))));
        return Diff.map(toStatus(result.values()), this.left, this.right, result);
    }

    private <P> Diff<P> visitProperty(Property<? super T, P> property) {
        return property.getModel().accept(newVisitor(property.get(left), property.get(right)));
    }

    public Diff<T> visitValue(ValueModel<T> value) {
        return Diff.simple(left == null ^ right == null ? left == null ? ADDED : REMOVED : Objects.equals(left, right) ? UNCHANGED : MODIFIED, left, right);
    }
}
