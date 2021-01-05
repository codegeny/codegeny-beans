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
package org.codegeny.beans.diff;

import org.codegeny.beans.diff.visitor.GetDiffVisitor;
import org.codegeny.beans.diff.visitor.TraversingDiffVisitor;
import org.codegeny.beans.path.Path;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * A diff represents a comparison between 2 objects (left and right).
 *
 * @param <T> The type of the 2 compared objects.
 * @author Xavier DURY
 */
public abstract class Diff<T> implements Serializable {

    /**
     * Static method factory for <code>{@link SetDiff}</code>.
     *
     * @param status The status.
     * @param left   The left set.
     * @param right  The right set.
     * @param set    The diff'ed elements as a set.
     * @param <L>    The type of set.
     * @param <E>    The type of the set elements.
     * @return A <code>{@link SetDiff}</code>.
     */
    public static <L, E> SetDiff<L, E> set(Status status, L left, L right, Set<? extends Diff<E>> set) {
        return new SetDiff<>(status, left, right, set);
    }

    /**
     * Static method factory for <code>{@link ListDiff}</code>.
     *
     * @param status The status.
     * @param left   The left list.
     * @param right  The right list.
     * @param list   The diff'ed elements as a list.
     * @param <L>    The type of list.
     * @param <E>    The type of the list elements.
     * @return A <code>{@link ListDiff}</code>.
     */
    public static <L, E> ListDiff<L, E> list(Status status, L left, L right, List<? extends Diff<E>> list) {
        return new ListDiff<>(status, left, right, list);
    }

    /**
     * Static method factory for <code>{@link MapDiff}</code>.
     *
     * @param status The status.
     * @param left   The left map.
     * @param right  The right map.
     * @param map    The diffed values as a map.
     * @param <M>    The type of the map.
     * @param <K>    The type of the map keys.
     * @param <V>    The type of the map values.
     * @return A <code>{@link MapDiff}</code>.
     */
    public static <M, K, V> MapDiff<M, K, V> map(Status status, M left, M right, Map<? extends Diff<K>, ? extends Diff<V>> map) {
        return new MapDiff<>(status, left, right, map);
    }

    /**
     * Static method factory for <code>{@link MapDiff}</code>.
     *
     * @param status The status.
     * @param left   The left bean.
     * @param right  The right bean.
     * @param map    The diffed values as a map.
     * @param <B>    The type of the bean.
     * @return A <code>{@link BeanDiff}</code>.
     */
    public static <B> BeanDiff<B> bean(Status status, B left, B right, Map<String, ? extends Diff<?>> map) {
        return new BeanDiff<>(status, left, right, map);
    }

    /**
     * Static method factory for <code>{@link SimpleDiff}<c/ode>.
     *
     * @param status The status.
     * @param left   The left value.
     * @param right  The right value.
     * @param <T>    The type of the value.
     * @return A <code>{@link SimpleDiff}</code>.
     */
    public static <T> SimpleDiff<T> simple(Status status, T left, T right) {
        return new SimpleDiff<>(status, left, right);
    }

    /**
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * The left value.
     */
    private final T left;

    /**
     * The right value.
     */
    private final T right;

    /**
     * The status.
     */
    private final Status status;

    /**
     * Constructor.
     *
     * @param status The status.
     * @param left   The left value.
     * @param right  The right value.
     */
    Diff(Status status, T left, T right) {
        this.status = requireNonNull(status, "Status cannot be null");
        this.left = left;
        this.right = right;
    }

    /**
     * Accept a visitor.
     *
     * @param visitor The visitor.
     * @param <R>     The visitor result type.
     * @return The result.
     */
    public abstract <R> R accept(DiffVisitor<T, R> visitor);

    /**
     * Get the left value for this diff.
     *
     * @return The left value.
     */
    public final T getLeft() {
        return left;
    }

    /**
     * Get the right value for this diff.
     *
     * @return The right value.
     */

    public final T getRight() {
        return right;
    }

    /**
     * Get the status for this diff which can be either <code>ADDED</code>, <code>REMOVED</code>, <code>MODIFIED</code> or <code>UNCHANGED</code>.
     *
     * @return The status.
     */
    public final Status getStatus() {
        return status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        //return String.format("%s{status=%s, left=%s, right=%s}", getClass().getSimpleName(), status, left, right);
        return status.print(this);
    }

    /**
     * Transform this diff to a string.
     *
     * @return A string.
     */
    public final String describe() {
        StringBuilder builder = new StringBuilder();
        traverse((p, d) -> builder.append(p.toString()).append(" = ").append(d).append(System.lineSeparator()));
        return builder.toString();
    }

    /**
     * Extract the given path from the current diff.
     *
     * @param path The path.
     * @return The resulting diff.
     */
    public final Diff<?> get(Path<?> path) {
        return accept(new GetDiffVisitor<>(path));
    }

    /**
     * Traverse the diff tree.
     *
     * @param consumer The consumer.
     */
    public final void traverse(BiConsumer<? super Path<?>, ? super Diff<?>> consumer) {
        accept(new TraversingDiffVisitor<>(consumer));
    }

    /**
     * The status of the <code>{@link Diff}</code>. Can be:
     * <ul>
     * <li><code>ADDED</code> (left value does not exist while right does)</li>
     * <li><code>REMOVED</code> (right value does not exist while left does)</li>
     * <li><code>MODIFIED</code> (both values exist but are different)</li>
     * <li><code>UNCHANGED</code> (both values exist and are the same or both values do not exist)</li>
     * </ul>
     */
    public enum Status {

        /**
         * The value as been added to the right side and did not exist on the left side.
         */
        ADDED(diff -> String.format("+{%s}", diff.getRight())),

        /**
         * The value was present on the left side and has been removed from the right.
         */
        REMOVED(diff -> String.format("-{%s}", diff.getLeft())),

        /**
         * The value has changed between the left and right sides.
         */
        MODIFIED(diff -> String.format("~{%s}:{%s}", diff.getLeft(), diff.getRight())),

        /**
         * The value is the same on the right and the left.
         */
        UNCHANGED(diff -> String.format("={%s}", diff.getLeft()));

        /**
         * The print function.
         */
        private final Function<Diff<?>, String> printer;

        /**
         * Constructor.
         *
         * @param printer The print function.
         */
        Status(Function<Diff<?>, String> printer) {
            this.printer = printer;
        }

        /**
         * Combine 2 statuses given the following rules:
         * <ol>
         * <li>2 identical statuses must give the same status</li>
         * <li>2 different statuses must give the <code>MODIFIED</code> status</li>
         * </ol>
         *
         * @param that The other status.
         * @return The combined status.
         */
        public Status combineWith(Status that) {
            return equals(requireNonNull(that, "Status cannot be null")) ? this : MODIFIED;
        }

        /**
         * Is this status representing any change?
         *
         * @return True only if this status is <code>UNCHANGED</code>.
         */
        public boolean isChanged() {
            return !equals(UNCHANGED);
        }

        /**
         * Reduce a status from a list of diffs.
         *
         * @param diffs The diffs.
         * @return The combined status.
         */
        public static Status combineAll(Collection<? extends Diff<?>> diffs) {
            return diffs.stream().map(Diff::getStatus).reduce(Status::combineWith).orElse(UNCHANGED);
        }

        /**
         * Print a diff.
         *
         * @param diff The diff.
         * @return The string form.
         */
        public String print(Diff<?> diff) {
            return printer.apply(diff);
        }
    }
}
