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

import org.codegeny.beans.diff.BeanDiff;
import org.codegeny.beans.diff.Diff;
import org.codegeny.beans.diff.Diff.Status;
import org.codegeny.beans.diff.ListDiff;
import org.codegeny.beans.diff.MapDiff;
import org.codegeny.beans.diff.SimpleDiff;
import org.codegeny.beans.model.BeanModel;
import org.codegeny.beans.model.ListModel;
import org.codegeny.beans.model.MapModel;
import org.codegeny.beans.model.ModelVisitor;
import org.codegeny.beans.model.Property;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.codegeny.beans.diff.Diff.Status.ADDED;
import static org.codegeny.beans.diff.Diff.Status.MODIFIED;
import static org.codegeny.beans.diff.Diff.Status.REMOVED;
import static org.codegeny.beans.diff.Diff.Status.UNCHANGED;

public abstract class AbstractComputeDiffModelVisitor<T> implements ModelVisitor<T, Diff<T>> {

    protected final T left, right;

    AbstractComputeDiffModelVisitor(T left, T right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public <K, V> MapDiff<T, K, V> visitMap(MapModel<T, K, V> map) {
        if (left == null ^ right == null) {
            return (left == null ? new AddedDiffModelVisitor<>(right) : new RemovedDiffModelVisitor<>(left)).visitMap(map);
        }
        Map<K, V> leftMap = map.toMap(left);
        Map<K, V> rightMap = map.toMap(right);
        Set<K> keys = new HashSet<>();
        keys.addAll(leftMap.keySet());
        keys.addAll(rightMap.keySet());
        Map<K, Diff<V>> result = keys.stream().collect(toMap(k -> k, k -> map.acceptValue(newVisitor(leftMap.get(k), rightMap.get(k)))));
        return Diff.map(Status.combineAll(result.values()), this.left, this.right, result);
    }

    @Override
    public SimpleDiff<T> visitValue(ValueModel<T> value) {
        return Diff.simple(left == null ^ right == null ? left == null ? ADDED : REMOVED : Objects.equals(left, right) ? UNCHANGED : MODIFIED, left, right);
    }

    @Override
    public BeanDiff<T> visitBean(BeanModel<T> bean) {
        if (left == null ^ right == null) {
            return (left == null ? new AddedDiffModelVisitor<>(right) : new RemovedDiffModelVisitor<>(left)).visitBean(bean);
        }
        Map<String, Diff<?>> properties = bean.getProperties().stream().collect(toMap(Property::getName, this::visitProperty, AbstractComputeDiffModelVisitor::throwingBinaryOperator, LinkedHashMap::new));
        return Diff.bean(Status.combineAll(properties.values()), this.left, this.right, properties);
    }

    protected abstract <S> ModelVisitor<S, Diff<S>> newVisitor(S left, S right);

    private <P> Diff<P> visitProperty(Property<? super T, P> property) {
        return property.getModel().accept(newVisitor(property.get(left), property.get(right)));
    }

    private static <X> X throwingBinaryOperator(X left, X right) {
        throw new IllegalStateException(String.format("Duplicate key %s", left));
    }

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

        public BeanDiff<T> visitBean(BeanModel<T> bean) {
            return Diff.bean(type, left, right, bean.getProperties().stream().collect(toMap(Property::getName, this::visitProperty)));
        }

        public <E> ListDiff<T, E> visitSet(SetModel<T, E> values) {
            return Diff.list(type, left, right, values.toSet(target).stream().map(e -> values.acceptElement(create(e))).collect(toList()));
        }

        public <E> ListDiff<T, E> visitList(ListModel<T, E> values) {
            return Diff.list(type, left, right, values.toList(target).stream().map(e -> values.acceptElement(create(e))).collect(toList()));
        }

        public <K, V> MapDiff<T, K, V> visitMap(MapModel<T, K, V> map) {
            return Diff.map(type, left, right, map.toMap(target).entrySet().stream().collect(toMap(Map.Entry::getKey, e -> map.acceptValue(create(e.getValue())))));
        }

        private <P> Diff<P> visitProperty(Property<? super T, P> property) {
            return property.accept(create(property.get(target)));
        }

        public SimpleDiff<T> visitValue(ValueModel<T> value) {
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

    protected static class RemovedDiffModelVisitor<T> extends AbstractDiffModelVisitor<T> {

        RemovedDiffModelVisitor(T left) {
            super(left, null, left, REMOVED);
        }

        protected <N> AbstractDiffModelVisitor<N> create(N value) {
            return new RemovedDiffModelVisitor<>(value);
        }
    }
}
