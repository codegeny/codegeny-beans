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
import org.codegeny.beans.model.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.codegeny.beans.diff.Diff.Status.ADDED;
import static org.codegeny.beans.diff.Diff.Status.MODIFIED;
import static org.codegeny.beans.diff.Diff.Status.REMOVED;
import static org.codegeny.beans.diff.Diff.Status.UNCHANGED;

/**
 * {@link ModelVisitor} which implements {@link Diff} computation for {@link org.codegeny.beans.model.Model}s.
 * This visitor needs the 2 instances to be diff'ed.
 *
 * @param <T> The model type.
 * @author Xavier DURY
 */
public final class ComputeDiffModelVisitor<T> implements ModelVisitor<T, Diff<T>> {

    /**
     * Static factory method.
     *
     * @param left  The left value to diff.
     * @param right The right value to diff.
     * @param <T> The model type.
     * @return A {@link ModelVisitor}.
     */
    public static <T> ModelVisitor<T, Diff<T>> of(T left, T right) {
        return left == null ^ right == null
                ? new ConstantDiffModelVisitor<>(left, right)
                : new ComputeDiffModelVisitor<>(left, right);
    }

    /**
     * The left value.
     */
    private final T left;

    /**
     * The right value.
     */
    private final T right;

    /**
     * Constructor.
     *
     * @param left  The left value to diff.
     * @param right The right value to diff.
     */
    private ComputeDiffModelVisitor(T left, T right) {
        this.left = left;
        this.right = right;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Diff<T> visitMap(MapModel<T, K, V> map) {
        Map<K, V> leftMap = map.toMap(left);
        Map<K, V> rightMap = map.toMap(right);
        Map<K, K> leftKeys = leftMap.keySet().stream().collect(Collectors.toMap(Function.identity(), Function.identity()));
        Map<K, K> rightKeys = rightMap.keySet().stream().collect(Collectors.toMap(Function.identity(), Function.identity()));
        Set<K> keys = new HashSet<>();
        keys.addAll(leftMap.keySet());
        keys.addAll(rightMap.keySet());
        Map<Diff<K>, Diff<V>> result = keys.stream().collect(toMap(k -> map.getKeyModel().diff(leftKeys.get(k), rightKeys.get(k)), k -> map.getValueModel().diff(leftMap.get(k), rightMap.get(k))));
        return Diff.map(Status.combineAll(result.values()), left, right, result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Diff<T> visitValue(ValueModel<T> value) {
        return Diff.simple(value.compare(left, right) == 0 ? UNCHANGED : MODIFIED, left, right);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Diff<T> visitBean(BeanModel<T> bean) {
        Map<String, Diff<?>> properties = bean.getProperties().stream().collect(toMap(Property::getName, this::visitProperty));
        return Diff.bean(Status.combineAll(properties.values()), left, right, properties);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Using Myers diff algorithm.
     */
    @Override
    public <E> Diff<T> visitList(ListModel<T, E> list) {
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
                            result.add(0, list.getElementModel().diff(leftList.get(--x), rightList.get(--y)));
                        }
                        if (d > 0) {
                            result.add(0, list.getElementModel().diff(x == px ? null : leftList.get(x = px), y == py ? null : rightList.get(y = py)));
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
        Map<E, E> leftMap = set.toSet(left).stream().collect(Collectors.toMap(Function.identity(), Function.identity()));
        Map<E, E> rightMap = set.toSet(right).stream().collect(Collectors.toMap(Function.identity(), Function.identity()));
        Set<E> keys = new HashSet<>();
        keys.addAll(leftMap.keySet());
        keys.addAll(rightMap.keySet());
        Set<Diff<E>> result = keys.stream().map(e -> set.getElementModel().diff(leftMap.get(e), rightMap.get(e))).collect(Collectors.toSet());
        return Diff.set(Status.combineAll(result), left, right, result);
    }

    /**
     * Visit a property.
     *
     * @param property The property.
     * @param <P>      The property type.
     * @return A diff.
     */
    private <P> Diff<P> visitProperty(Property<? super T, P> property) {
        return property.getModel().diff(property.get(left), property.get(right));
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
         * Constructor.
         *
         * @param left  The left value.
         * @param right The right value.
         */
        ConstantDiffModelVisitor(T left, T right) {
            this.left = left;
            this.right = right;
        }

        /**
         * Get the diff status.
         *
         * @return The diff status.
         */
        private Status status() {
            return left == null ? ADDED : REMOVED;
        }

        /**
         * Get the target. ADDED = right, REMOVED = left.
         *
         * @return The target.
         */
        private T target() {
            return left == null ? right : left;
        }

        /**
         * Create a visitor for a sub-node.
         *
         * @param value The value.
         * @param <C>   The base type (super type of &gt;N&lt;) for which the diff should be produced.
         * @return A visitor.
         */
        private <C> ConstantDiffModelVisitor<C> newVisitor(C value) {
            return new ConstantDiffModelVisitor<>(left == null ? null : value, left == null ? value : null);
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
