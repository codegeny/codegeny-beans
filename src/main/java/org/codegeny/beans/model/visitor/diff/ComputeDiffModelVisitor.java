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
import org.codegeny.beans.diff.ListDiff;
import org.codegeny.beans.diff.MapDiff;
import org.codegeny.beans.diff.SimpleDiff;
import org.codegeny.beans.hash.AddAndXorHasher;
import org.codegeny.beans.model.BeanModel;
import org.codegeny.beans.model.ListModel;
import org.codegeny.beans.model.MapModel;
import org.codegeny.beans.model.ModelVisitor;
import org.codegeny.beans.model.Property;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.IntConsumer;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;
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
 * @param <T> The model status.
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
     * The minimum matching score above which 2 objects can be considered the same "instance".
     */
    private final double threshold;

    /**
     * The score optimizer.
     */
    private final ScoreOptimizer optimizer;

    /**
     * Constructor with default score optimizer.
     *
     * @param left      The left value to diff.
     * @param right     The right value to diff.
     * @param threshold The minimum matching score above which 2 objects can be considered the same "instance".
     */
    public ComputeDiffModelVisitor(T left, T right, double threshold) {
        this(left, right, threshold, LocalScoreOptimizer.INSTANCE);
    }

    /**
     * Constructor.
     *
     * @param left      The left value to diff.
     * @param right     The right value to diff.
     * @param threshold The minimum matching score above which 2 objects can be considered the same "instance".
     * @param optimizer The score optimizer.
     */
    public ComputeDiffModelVisitor(T left, T right, double threshold, ScoreOptimizer optimizer) {
        if (threshold <= 0 || threshold > 1) {
            throw new IllegalArgumentException("Threshold must be (strictly greater-than 0) and (lower-than or equal-to 1)");
        }
        this.left = left;
        this.right = right;
        this.threshold = threshold;
        this.optimizer = requireNonNull(optimizer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> MapDiff<T, K, V> visitMap(MapModel<T, K, V> map) {
        if (left == null ^ right == null) {
            return (left == null ? added(right) : removed(left)).visitMap(map);
        }
        Map<K, V> leftMap = map.toMap(left);
        Map<K, V> rightMap = map.toMap(right);
        Set<K> keys = new TreeSet<>(map.getKeyModel());
        keys.addAll(leftMap.keySet());
        keys.addAll(rightMap.keySet());
        Map<K, Diff<V>> result = keys.stream().collect(toMap(k -> k, k -> map.acceptValue(newVisitor(leftMap.get(k), rightMap.get(k)))));
        return Diff.map(Status.combineAll(result.values()), left, right, result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleDiff<T> visitValue(ValueModel<T> value) {
        return Diff.simple(left == null ^ right == null ? left == null ? ADDED : REMOVED : value.compare(left, right) == 0 ? UNCHANGED : MODIFIED, left, right);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapDiff<T, String, ?> visitBean(BeanModel<T> bean) {
        if (left == null ^ right == null) {
            return (left == null ? added(right) : removed(left)).visitBean(bean);
        }
        Map<String, Diff<?>> properties = bean.getProperties().stream().collect(toMap(Property::getName, this::visitProperty, throwingMerger(), LinkedHashMap::new));
        return Diff.map(Status.combineAll(properties.values()), left, right, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> ListDiff<T, E> visitList(ListModel<T, E> values) {
        List<E> leftList = values.toList(left);
        List<E> rightList = values.toList(right);
        List<Diff<E>> result = new LinkedList<>();
        boolean removeFirst = true;
        int i = 0, j = 0;
        int m = 0, n = 0;
        main:
        while (i < leftList.size() && j < rightList.size()) {
            E l = leftList.get(i), r = rightList.get(j);
            for (int a = i, b = j; a < leftList.size() || b < rightList.size(); a++, b++) {
                if (a < leftList.size()) {
                    Diff<E> diff = values.acceptElement(newVisitor(leftList.get(a), r));
                    if (diff.getScore() >= threshold) {
                        if (removeFirst) {
                            range(i - m, i = a).forEach(q -> result.add(values.acceptElement(removed(leftList.get(q)))));
                            range(j - n, j).forEach(q -> result.add(values.acceptElement(added(rightList.get(q)))));
                            removeFirst = n == 0;
                        } else {
                            range(j - n, j).forEach(q -> result.add(values.acceptElement(added(rightList.get(q)))));
                            range(i - m, i = a).forEach(q -> result.add(values.acceptElement(removed(leftList.get(q)))));
                            removeFirst = m > 0;
                        }
                        result.add(diff);
                        i++;
                        j++;
                        m = n = 0;
                        continue main;
                    }
                }
                if (b < rightList.size()) { // TODO: do not compare initial values twice
                    Diff<E> diff = values.acceptElement(newVisitor(l, rightList.get(b)));
                    if (diff.getScore() >= threshold) {
                        if (removeFirst) {
                            range(i - m, i).forEach(q -> result.add(values.acceptElement(removed(leftList.get(q)))));
                            range(j - n, j = b).forEach(q -> result.add(values.acceptElement(added(rightList.get(q)))));
                            removeFirst = n == 0;
                        } else {
                            range(j - n, j = b).forEach(q -> result.add(values.acceptElement(added(rightList.get(q)))));
                            range(i - m, i).forEach(q -> result.add(values.acceptElement(removed(leftList.get(q)))));
                            removeFirst = m > 0;
                        }
                        result.add(diff);
                        i++;
                        j++;
                        m = n = 0;
                        continue main;
                    }
                }
            }
            i++;
            j++;
            m++;
            n++;
        }
        if (removeFirst) {
            range(i - m, leftList.size()).forEach(q -> result.add(values.acceptElement(removed(leftList.get(q)))));
            range(j - n, rightList.size()).forEach(q -> result.add(values.acceptElement(added(rightList.get(q)))));
        } else {
            range(j - n, rightList.size()).forEach(q -> result.add(values.acceptElement(added(rightList.get(q)))));
            range(i - m, leftList.size()).forEach(q -> result.add(values.acceptElement(removed(leftList.get(q)))));
        }
        return Diff.list(Diff.Status.combineAll(result), left, right, result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> ListDiff<T, E> visitSet(SetModel<T, E> values) {

        List<E> leftValues = new ArrayList<>(values.toSet(left));
        List<E> rightValues = new ArrayList<>(values.toSet(right));

        final int leftSize = leftValues.size();
        final int rightSize = rightValues.size();

        List<Diff<E>> list = new LinkedList<>();

        // used objects (all false to begin)

        boolean[] leftUsed = new boolean[leftSize];
        boolean[] rightUsed = new boolean[rightSize];

        // hash all objects to guide comparison (same hash = maybe same object)

        int[] leftHashes = leftValues.stream().mapToInt(e -> values.getElementModel().hash(e, new AddAndXorHasher())).toArray();
        int[] rightHashes = rightValues.stream().mapToInt(e -> values.getElementModel().hash(e, new AddAndXorHasher())).toArray();

        Adder<E> addBoth = (i, j, diff) -> {
            rightUsed[j] = leftUsed[i] = true;
            list.add(diff);
        };

        // if two objects have the same hash, compare them, if equal remove them from the list and add a diff

        for (int i = 0; i < leftSize; i++) {
            for (int j = 0; !leftUsed[i] && j < rightSize; j++) {
                if (!rightUsed[j] && leftHashes[i] == rightHashes[j]) {
                    E left = leftValues.get(i);
                    E right = rightValues.get(j);
                    Diff<E> diff = values.acceptElement(newVisitor(left, right));
                    if (!diff.getStatus().isChanged()) {
                        addBoth.accept(i, j, diff);
                        break;
                    }
                }
            }
        }

        // for remaining, objects calculate a diff matrix for each possible pair

        @SuppressWarnings("unchecked")
        Diff<E>[][] matrix = new Diff[leftSize][rightSize];

        range(0, leftSize).filter(i -> !leftUsed[i]).forEach(i -> range(0, rightSize).filter(j -> !rightUsed[j]).forEach(j -> {
            E left = leftValues.get(i);
            E right = rightValues.get(j);
            Diff<E> diff = values.acceptElement(newVisitor(left, right));
            matrix[i][j] = diff.getScore() >= threshold ? diff : null;
        }));

        @SuppressWarnings("unchecked")
        Diff<E>[] leftNulls = new Diff[leftSize];

        @SuppressWarnings("unchecked")
        Diff<E>[] rightNulls = new Diff[rightSize];

        IntConsumer addLeft = i -> {
            leftUsed[i] = true;
            list.add(leftNulls[i]);
        };

        IntConsumer addRight = j -> {
            rightUsed[j] = true;
            list.add(rightNulls[j]);
        };

        // calculate left/null and right/null

        range(0, leftSize).filter(i -> !leftUsed[i]).forEach(i -> leftNulls[i] = values.getElementModel().accept(removed(leftValues.get(i)))); // left->null:REMOVED
        range(0, rightSize).filter(j -> !rightUsed[j]).forEach(j -> rightNulls[j] = values.getElementModel().accept(added(rightValues.get(j)))); // null->right:ADDED

        // if a column or row is full of nulls, remove it

        range(0, leftSize).filter(i -> !leftUsed[i]).filter(i -> range(0, rightSize).filter(j -> !rightUsed[j]).allMatch(j -> matrix[i][j] == null)).forEach(addLeft);
        range(0, rightSize).filter(j -> !rightUsed[j]).filter(j -> range(0, leftSize).filter(i -> !leftUsed[i]).allMatch(i -> matrix[i][j] == null)).forEach(addRight);

        // try to find the permutation that maximize the score

        int[] leftMapping = range(0, leftSize).filter(i -> !leftUsed[i]).toArray();
        int[] rightMapping = range(0, rightSize).filter(j -> !rightUsed[j]).toArray();

        if (leftMapping.length > 0 && rightMapping.length > 0) {

            BiFunction<Integer, Integer, Diff<E>> source = leftMapping.length < rightMapping.length
                    ? (a, b) -> matrix[leftMapping[a]][rightMapping[b]]
                    : (a, b) -> matrix[leftMapping[b]][rightMapping[a]];

            double[][] scores = new double[min(rightMapping.length, leftMapping.length)][max(rightMapping.length, leftMapping.length)];
            for (int k = 0; k < min(rightMapping.length, leftMapping.length); k++) {
                for (int n = 0; n < max(rightMapping.length, leftMapping.length); n++) {
                    Diff<E> diff = source.apply(k, n);
                    scores[k][n] = diff != null ? diff.getScore() : 0; // TODO sum of both scores (0 + 0 for normalized, -x + -x for absolute)
                }
            }

            int[] mapping = optimizer.solve(min(rightMapping.length, leftMapping.length), max(rightMapping.length, leftMapping.length), scores);
            range(0, mapping.length).forEach(i -> {
                int x = leftMapping[leftMapping.length >= rightMapping.length ? mapping[i] : i];
                int y = rightMapping[leftMapping.length >= rightMapping.length ? i : mapping[i]];
                Diff<E> diff = matrix[x][y];
                if (diff != null) {
                    addBoth.accept(x, y, diff);
                } else {
                    addLeft.accept(x);
                    addRight.accept(y);
                }
            });
        }

        // the rest is the non-matching values

        range(0, leftSize).filter(i -> !leftUsed[i]).forEach(addLeft);
        range(0, rightSize).filter(j -> !rightUsed[j]).forEach(addRight);

        // sort the result

        list.sort(comparing(e -> e.getLeft() == null ? e.getRight() : e.getLeft(), nullsLast(values.getElementModel())));
        return Diff.list(Diff.Status.combineAll(list), left, right, list);
    }

    /**
     * Visit a property.
     *
     * @param property The property.
     * @param <P>      The property status.
     * @return A diff.
     */
    private <P> Diff<P> visitProperty(Property<? super T, P> property) {
        return property.getModel().accept(newVisitor(property.get(left), property.get(right)));
    }

    /**
     * Create a new visitor.
     *
     * @param left  The new left value.
     * @param right The new right value.
     * @param <S>   The model status for the new visitor.
     * @return A new visitor.
     */
    private <S> ComputeDiffModelVisitor<S> newVisitor(S left, S right) {
        return new ComputeDiffModelVisitor<>(left, right, threshold, optimizer);
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
     * Internal added interface.
     *
     * @param <T> The diff'ed type.
     */
    private interface Adder<T> {

        void accept(int x, int y, Diff<T> diff);
    }

    /**
     * Create a REMOVED model visitor.
     *
     * @param left The left value.
     * @param <S>  The value type.
     * @return A model visitor.
     */
    private static <S> ConstantDiffModelVisitor<S> removed(S left) {
        return new ConstantDiffModelVisitor<>(left, null, false);
    }

    /**
     * Create an ADDED model visitor.
     *
     * @param right The right value.
     * @param <S>   The value type.
     * @return A model visitor.
     */
    private static <S> ConstantDiffModelVisitor<S> added(S right) {
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
         * @param <N>   The node type.
         * @return A visitor.
         */
        private <N> ConstantDiffModelVisitor<N> newVisitor(N value) {
            return new ConstantDiffModelVisitor<>(added ? null : value, added ? value : null, added);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public MapDiff<T, String, ?> visitBean(BeanModel<T> bean) {
            return Diff.map(status(), left, right, bean.getProperties().stream().collect(toMap(Property::getName, this::visitProperty)));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <E> ListDiff<T, E> visitSet(SetModel<T, E> values) {
            return Diff.list(status(), left, right, values.toSet(target()).stream().map(e -> values.acceptElement(newVisitor(e))).collect(toList()));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <E> ListDiff<T, E> visitList(ListModel<T, E> values) {
            return Diff.list(status(), left, right, values.toList(target()).stream().map(e -> values.acceptElement(newVisitor(e))).collect(toList()));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <K, V> MapDiff<T, K, V> visitMap(MapModel<T, K, V> map) {
            return Diff.map(status(), left, right, map.toMap(target()).entrySet().stream().collect(toMap(Map.Entry::getKey, e -> map.acceptValue(newVisitor(e.getValue())))));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SimpleDiff<T> visitValue(ValueModel<T> value) {
            return Diff.simple(status(), left, right);
        }

        /**
         * Visit a property.
         *
         * @param property The property.
         * @param <P>      The property status.
         * @return A diff.
         */
        private <P> Diff<P> visitProperty(Property<? super T, P> property) {
            return property.accept(newVisitor(property.get(target())));
        }
    }
}
