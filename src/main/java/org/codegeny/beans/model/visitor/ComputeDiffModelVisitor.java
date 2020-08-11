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

import org.codegeny.beans.diff.Diff;
import org.codegeny.beans.diff.Diff.Status;
import org.codegeny.beans.model.BeanModel;
import org.codegeny.beans.model.ListModel;
import org.codegeny.beans.model.MapModel;
import org.codegeny.beans.model.ModelVisitor;
import org.codegeny.beans.model.Property;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.codegeny.beans.diff.Diff.Status.ADDED;
import static org.codegeny.beans.diff.Diff.Status.MODIFIED;
import static org.codegeny.beans.diff.Diff.Status.REMOVED;
import static org.codegeny.beans.diff.Diff.Status.UNCHANGED;

/**
 * {@link ModelVisitor} which implements {@link Diff} computation for {@link org.codegeny.beans.model.Model}s.
 * This visitor needs the 2 instances to be diff'ed. This visitor can also be parameterized with a threshold
 * representing the limit (which must be in ]0;1]) of when 2 objects are considered the same (for example, at 0.8, any
 * 2 objects which have a matching score greater or equals to 0.8 could be considered the same instance with
 * modifications).
 *
 * @param <T> The model type.
 * @author Xavier DURY
 */
public final class ComputeDiffModelVisitor<T> implements ModelVisitor<T, Diff<T>> {

    /**
     * The left value;
     */
    private final T left;

    /**
     * The right value.
     */
    private final T right;

    /**
     * Constructor.
     *
     * @param left      The left value to diff.
     * @param right     The right value to diff.
     */
    public ComputeDiffModelVisitor(T left, T right) {
        this.left = left;
        this.right = right;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Diff<T> visitMap(MapModel<T, K, V> map) {
        if (left == null ^ right == null) {
            return map.accept(left == null ? added(right) : removed(left));
        }
        Map<K, V> leftMap = map.toMap(left);
        Map<K, V> rightMap = map.toMap(right);
        Map<K, K> leftKeys = new HashMap<>();
        Map<K, K> rightKeys = new HashMap<>();
        leftMap.keySet().forEach(k -> leftKeys.put(k, k));
        rightMap.keySet().forEach(k -> rightKeys.put(k, k));
        Set<K> keys = new HashSet<>();
        keys.addAll(leftMap.keySet());
        keys.addAll(rightMap.keySet());
        Map<Diff<K>, Diff<V>> result = keys.stream().collect(toMap(k -> map.acceptKey(newVisitor(leftKeys.get(k), rightKeys.get(k))), k -> map.acceptValue(newVisitor(leftMap.get(k), rightMap.get(k)))));
        return Diff.map(Status.combineAll(result.values()), left, right, result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Diff<T> visitValue(ValueModel<T> value) {
        return Diff.simple(left == null ^ right == null ? left == null ? ADDED : REMOVED : value.compare(left, right) == 0 ? UNCHANGED : MODIFIED, left, right);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Diff<T> visitBean(BeanModel<T> bean) {
        if (left == null ^ right == null) {
            return bean.accept(left == null ? added(right) : removed(left));
        }
        Map<String, Diff<?>> properties = bean.getProperties().stream().collect(toMap(Property::getName, this::visitProperty, throwingMerger(), LinkedHashMap::new));
        return Diff.bean(Status.combineAll(properties.values()), left, right, properties);
    }

    /**
     * {@inheritDoc}
     *
     * Using Myers diff algorithm.
     */
    @Override
    public <E> Diff<T> visitList(ListModel<T, E> list) {
        if (left == null ^ right == null) {
            return list.accept(left == null ? added(right) : removed(left));
        }
        List<E> leftList = list.toList(left);
        List<E> rightList = list.toList(right);
        int n = leftList.size();
        int m = rightList.size();
        int z = n + m;
        int[] v = new int[z + 1 + z];
        List<int[]> trace = new ArrayList<>();
        for (int d = 0; d <= z; d++) {
            trace.add(v.clone());
            for (int k = -d; k <= d; k += 2) {
                int x = k == -d || k != d && v[z + k - 1] < v[z + k + 1] ? v[z + k + 1] : v[z + k - 1] + 1;
                int y = x - k;
                while (x < n && y < m && Objects.equals(leftList.get(x), rightList.get(y))) {
                    x++;
                    y++;
                }
                v[z + k] = x;
                if (x == n && y == m) {
                    List<Diff<E>> result = new ArrayList<>(d);
                    do {
                        v = trace.get(d);
                        k = x - y;
                        int pk = k == -d || k != d && v[z + k - 1] < v[z + k + 1] ? k + 1 : k - 1;
                        int px = v[z + pk];
                        int py = px - pk;
                        while (x > px && y > py) {
                            result.add(0, list.acceptElement(newVisitor(leftList.get(--x), rightList.get(--y))));
                        }
                        if (d > 0) {
                            result.add(0, list.acceptElement(newVisitor(x == px ? null : leftList.get(x = px), y == py ? null : rightList.get(y = py))));
                        }
                    } while (--d >= 0);
                    return Diff.list(Diff.Status.combineAll(result), left, right, result);
                }
            }
        }
        throw new InternalError("Unreachable");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> Diff<T> visitSet(SetModel<T, E> set) {
        if (left == null ^ right == null) {
            return set.accept(left == null ? added(right) : removed(left));
        }
        Map<E, E> leftMap = new HashMap<>();
        Map<E, E> rightMap = new HashMap<>();
        set.toSet(left).forEach(e -> leftMap.put(e, e));
        set.toSet(right).forEach(e -> rightMap.put(e, e));
        Set<E> keys = new HashSet<>();
        keys.addAll(leftMap.keySet());
        keys.addAll(rightMap.keySet());
        List<Diff<E>> result = keys.stream().map(e -> set.acceptElement(newVisitor(leftMap.get(e), rightMap.get(e)))).collect(Collectors.toList());
        return Diff.list(Status.combineAll(result), left, right, result);
    }

    /**
     * Visit a property.
     *
     * @param property The property.
     * @param <P>      The property type.
     * @return A diff.
     */
    private <P> Diff<P> visitProperty(Property<? super T, P> property) {
        return property.getModel().accept(newVisitor(property.get(left), property.get(right)));
    }

    /**
     * Binary operator which prevent merging two keys.
     *
     * @param <T> The key type.
     * @return A binary operator.
     */
    private static <T> BinaryOperator<T> throwingMerger() {
        return (u, v) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", u));
        };
    }

    /**
     * Create a new visitor.
     *
     * @param left  The new left value.
     * @param right The new right value.
     * @param <T>   The model type for the new visitor.
     * @return A new visitor.
     */
    private static <T> ComputeDiffModelVisitor<T> newVisitor(T left, T right) {
        return new ComputeDiffModelVisitor<>(left, right);
    }

    /**
     * Create a REMOVED model visitor.
     *
     * @param left The left value.
     * @param <T>  The value type.
     * @return A model visitor.
     */
    private static <T> ConstantDiffModelVisitor<T> removed(T left) {
        return new ConstantDiffModelVisitor<>(left, null, false);
    }

    /**
     * Create an ADDED model visitor.
     *
     * @param right The right value.
     * @param <T>   The value type.
     * @return A model visitor.
     */
    private static <T> ConstantDiffModelVisitor<T> added(T right) {
        return new ConstantDiffModelVisitor<>(null, right, true);
    }

    /**
     * Model visitor to be used for children of ADDED/REMOVED nodes.
     *
     * @param <T> The model type.
     */
    private static final class ConstantDiffModelVisitor<T> implements ModelVisitor<T, Diff<T>> {

        /**
         * The left value.
         */
        private final T left;

        /**
         * The right value.
         */
        private final T right;

        /**
         * Indicator for being ADDED (or REMOVED).
         */
        private final boolean added;

        /**
         * Constructor.
         *
         * @param left  The left value.
         * @param right The right value.
         * @param added Indicator for being ADDED (or REMOVED).
         */
        ConstantDiffModelVisitor(T left, T right, boolean added) {
            this.left = left;
            this.right = right;
            this.added = added;
        }

        /**
         * Get the diff status.
         *
         * @return The diff status.
         */
        private Status status() {
            return added ? ADDED : REMOVED;
        }

        /**
         * Get the target. ADDED = right, REMOVED = left.
         *
         * @return The target.
         */
        private T target() {
            return added ? right : left;
        }

        /**
         * Create a visitor for a sub-node.
         *
         * @param value The value.
         * @param <C>   The base type (super type of &gt;N&lt;) for which the diff should be produced.
         * @return A visitor.
         */
        private <C> ConstantDiffModelVisitor<C> newVisitor(C value) {
            return new ConstantDiffModelVisitor<>(added ? null : value, added ? value : null, added);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Diff<T> visitBean(BeanModel<T> bean) {
            return Diff.bean(status(), left, right, bean.getProperties().stream().collect(toMap(Property::getName, this::visitProperty)));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <E> Diff<T> visitSet(SetModel<T, E> values) {
            return Diff.list(status(), left, right, values.toSet(target()).stream().map(e -> values.acceptElement(newVisitor(e))).collect(toList()));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <E> Diff<T> visitList(ListModel<T, E> values) {
            return Diff.list(status(), left, right, values.toList(target()).stream().map(e -> values.acceptElement(newVisitor(e))).collect(toList()));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <K, V> Diff<T> visitMap(MapModel<T, K, V> map) {
            return Diff.map(status(), left, right, map.toMap(target()).entrySet().stream().collect(toMap(e -> map.acceptKey(newVisitor(e.getKey())), e -> map.acceptValue(newVisitor(e.getValue())))));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Diff<T> visitValue(ValueModel<T> value) {
            return Diff.simple(status(), left, right);
        }

        /**
         * Visit a property.
         *
         * @param property The property.
         * @param <P>      The property type.
         * @return A diff.
         */
        private <P> Diff<P> visitProperty(Property<? super T, P> property) {
            return property.accept(newVisitor(property.get(target())));
        }
    }
}
