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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
 * @param <B> The base type (super type of &gt;T&lt;) for which the diff should be produced.
 * @author Xavier DURY
 */
public final class ComputeDiffModelVisitor<T extends B, B> implements ModelVisitor<T, Diff<B>> {

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
    public <K, V> MapDiff<B, K, V> visitMap(MapModel<T, K, V> map) {
        if (left == null ^ right == null) {
            return (left == null ? ComputeDiffModelVisitor.<T, B>added(right) : ComputeDiffModelVisitor.<T, B>removed(left)).visitMap(map);
        }
        Map<K, V> leftMap = map.toMap(left);
        Map<K, V> rightMap = map.toMap(right);
        Map<K, K> leftKeys = new HashMap<>();
        Map<K, K> rightKeys = new HashMap<>();
        Set<K> keys = new HashSet<>();
        leftMap.keySet().forEach(k -> leftKeys.put(k, k));
        rightMap.keySet().forEach(k -> rightKeys.put(k, k));
        keys.addAll(leftMap.keySet());
        keys.addAll(rightMap.keySet());
        Map<Diff<K>, Diff<V>> result = keys.stream().collect(toMap(k -> map.acceptKey(newVisitor(leftKeys.get(k), rightKeys.get(k))), k -> map.acceptValue(newVisitor(leftMap.get(k), rightMap.get(k)))));
        return Diff.map(Status.combineAll(result.values()), left, right, result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleDiff<B> visitValue(ValueModel<T> value) {
        return Diff.simple(left == null ^ right == null ? left == null ? ADDED : REMOVED : value.compare(left, right) == 0 ? UNCHANGED : MODIFIED, left, right);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BeanDiff<B> visitBean(BeanModel<T> bean) {
        if (left == null ^ right == null) {
            return (left == null ? ComputeDiffModelVisitor.<T, B>added(right) : ComputeDiffModelVisitor.<T, B>removed(left)).visitBean(bean);
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
    public <E> ListDiff<B, E> visitList(ListModel<T, E> values) {
        List<E> leftList = values.toList(left);
        List<E> rightList = values.toList(right);
        int n = leftList.size();
        int m = rightList.size();
        int max = n + m;
        int[] v = new int[2 * max + 1];
        List<int[]> trace = new ArrayList<>();
        for (int d = 0; d <= max; d++) {
            trace.add(v.clone());
            for (int k = -d; k <= d; k += 2) {
                int x = k == -d || k != d && v[max + k - 1] < v[max + k + 1] ? v[max + k + 1] : v[max + k - 1] + 1;
                int y = x - k;
                while (x < n && y < m && Objects.equals(leftList.get(x), rightList.get(y))) {
                    x++;
                    y++;
                }
                v[max + k] = x;
                if (x >= n && y >= m) {
                    List<Diff<E>> result = new LinkedList<>();
                    for (x = n, y = m; d >= 0; d--) {
                        v = trace.get(d);
                        k = x - y;
                        int pk = k == -d || k != d && v[max + k - 1] < v[max + k + 1] ? k + 1 : k - 1;
                        int px = v[max + pk];
                        int py = px - pk;
                        while (x > px && y > py) {
                            result.add(0, diff(x - 1, y - 1, x, y, leftList, rightList, values));
                            x--;
                            y--;
                        }
                        if (d > 0) {
                            result.add(0, diff(px, py, x, y, leftList, rightList, values));
                        }
                        x = px;
                        y = py;
                    }
                    return Diff.list(Diff.Status.combineAll(result), left, right, result);
                }
            }
        }
        throw new InternalError("Unreachable");
    }

    private <E> Diff<E> diff(int px, int py, int x, int y, List<E> left, List<E> right, ListModel<T, E> model) {
        if (x == px) {
            return model.acceptElement(added(right.get(py)));
        } else if (y == py) {
            return model.acceptElement(removed(left.get(px)));
        } else {
            return model.acceptElement(newVisitor(left.get(px), right.get(py)));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> ListDiff<B, E> visitSet(SetModel<T, E> values) {

        if (left == null ^ right == null) {
            return (left == null ? ComputeDiffModelVisitor.<T, B>added(right) : ComputeDiffModelVisitor.<T, B>removed(left)).visitSet(values);
        }

        Map<E, E> leftMap = new HashMap<>();
        Map<E, E> rightMap = new HashMap<>();
        values.toSet(left).forEach(e -> leftMap.put(e, e));
        values.toSet(right).forEach(e -> rightMap.put(e, e));

        Set<E> keys = new HashSet<>();
        keys.addAll(leftMap.keySet());
        keys.addAll(rightMap.keySet());

        List<Diff<E>> result = keys.stream().map(e -> values.acceptElement(newVisitor(leftMap.get(e), rightMap.get(e)))).collect(Collectors.toList());
        return Diff.list(Status.combineAll(result), left, right, result);
    }

    /**
     * Visit a property.
     *
     * @param property The property.
     * @param <P>      The property type.
     * @return A diff.
     */
    private <P> Diff<Object> visitProperty(Property<? super T, P> property) {
        return property.getModel().accept(newVisitor(property.get(left), property.get(right)));
    }

    /**
     * Create a new visitor.
     *
     * @param left  The new left value.
     * @param right The new right value.
     * @param <S>   The model type for the new visitor.
     * @param <C>   The base type (super type of &gt;S&lt;) for which the diff should be produced.
     * @return A new visitor.
     */
    private <S extends C, C> ComputeDiffModelVisitor<S, C> newVisitor(S left, S right) {
        return new ComputeDiffModelVisitor<>(left, right);
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
     * Create a REMOVED model visitor.
     *
     * @param left The left value.
     * @param <S>  The value type.
     * @param <B>  The base type (super type of &gt;T&lt;) for which the diff should be produced.
     * @return A model visitor.
     */
    private static <S extends B, B> ConstantDiffModelVisitor<S, B> removed(S left) {
        return new ConstantDiffModelVisitor<>(left, null, false);
    }

    /**
     * Create an ADDED model visitor.
     *
     * @param right The right value.
     * @param <S>   The value type.
     * @param <B>   The base type (super type of &gt;T&lt;) for which the diff should be produced.
     * @return A model visitor.
     */
    private static <S extends B, B> ConstantDiffModelVisitor<S, B> added(S right) {
        return new ConstantDiffModelVisitor<>(null, right, true);
    }

    /**
     * Model visitor to be used for children of ADDED/REMOVED nodes.
     *
     * @param <T> The model type.
     * @param <B> The base type (super type of &gt;T&lt;) for which the diff should be produced.
     */
    private static final class ConstantDiffModelVisitor<T extends B, B> implements ModelVisitor<T, Diff<B>> {

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
         * @param <N>   The node type.
         * @param <C>   The base type (super type of &gt;N&lt;) for which the diff should be produced.
         * @return A visitor.
         */
        private <N extends C, C> ConstantDiffModelVisitor<N, C> newVisitor(N value) {
            return new ConstantDiffModelVisitor<>(added ? null : value, added ? value : null, added);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public BeanDiff<B> visitBean(BeanModel<T> bean) {
            return Diff.bean(status(), left, right, bean.getProperties().stream().collect(toMap(Property::getName, this::visitProperty)));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <E> ListDiff<B, E> visitSet(SetModel<T, E> values) {
            return Diff.list(status(), left, right, values.toSet(target()).stream().map(e -> values.acceptElement(newVisitor(e))).collect(toList()));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <E> ListDiff<B, E> visitList(ListModel<T, E> values) {
            return Diff.list(status(), left, right, values.toList(target()).stream().map(e -> values.acceptElement(newVisitor(e))).collect(toList()));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <K, V> MapDiff<B, K, V> visitMap(MapModel<T, K, V> map) {
            return Diff.map(status(), left, right, map.toMap(target()).entrySet().stream().collect(toMap(e -> map.acceptKey(newVisitor(e.getKey())), e -> map.acceptValue(newVisitor(e.getValue())))));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SimpleDiff<B> visitValue(ValueModel<T> value) {
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
