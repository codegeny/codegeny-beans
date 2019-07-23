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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;

/**
 * A diff represents a comparison between 2 objects (left and right). A diff provides a matching score (between 0 and 1 inclusive) and a <code>{@link Status}</code>.
 * All implementations must be immutable, thread-safe and <code>{@link Serializable}</code> (as long as &lt;T&gt; type is also <code>{@link Serializable}</code>).
 *
 * @param <T> The type of the 2 compared objects.
 * @author Xavier DURY
 */
public interface Diff<T> extends Serializable {

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
    static <L, E> ListDiff<L, E> list(Status status, L left, L right, List<? extends Diff<? extends E>> list) {
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
    static <M, K, V> MapDiff<M, K, V> map(Status status, M left, M right, Map<K, ? extends Diff<? extends V>> map) {
        return new MapDiff<>(status, left, right, map);
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
    static <T> SimpleDiff<T> simple(Status status, T left, T right) {
        return new SimpleDiff<>(status, left, right);
    }

    /**
     * Accept a visitor.
     *
     * @param visitor The visitor.
     * @param <R>     The visitor result type.
     * @return The result.
     */
    <R> R accept(DiffVisitor<T, R> visitor);

    /**
     * Get the left value for this diff.
     *
     * @return The left value.
     */
    T getLeft();

    /**
     * Get the right value for this diff.
     *
     * @return The right value.
     */

    T getRight();

    /**
     * The score which has a range of [0; 1].
     * <ul>
     * <li>Each terminal value has a score of either 0 (not matched) or 1
     * (matched).</li>
     * <li>Each bean has an averaged score of all its properties scores.</li>
     * <li>Each collection has an averaged score of its elements scores.</li>
     * <li>Each map has an averaged score of its values scores.</li>
     * </ul>
     *
     * @return The normalized score ranging from 0 to 1.
     */
    double getScore();

    /**
     * Get the status for this diff which can be either <code>ADDED</code>, <code>REMOVED</code>, <code>MODIFIED</code> or <code>UNCHANGED</code>.
     *
     * @return The status.
     */
    Status getStatus();

    /**
     * Transform this diff to a map [path &rarr; <code>{@link Diff}</code>].
     *
     * @return A map.
     */
    default Map<String, Diff<?>> toMap() {
        Map<String, Diff<?>> map = new LinkedHashMap<>();
        traverse((p, d) -> map.put(p.toString(), d));
        return map;
    }

    /**
     * Transform this diff to a string.
     *
     * @return A string.
     */
    default String describe() {
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
    default Diff<?> get(Path<?> path) {
        return accept(new GetDiffVisitor<>(path));
    }

    /**
     * Traverse the diff tree.
     *
     * @param consumer The consumer.
     */
    default void traverse(BiConsumer<? super Path<?>, ? super Diff<?>> consumer) {
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
    enum Status {

        ADDED, MODIFIED, REMOVED, UNCHANGED;

        /**
         * Combine 2 statuses given the following rules:
         * <ol>
         * <li>2 identical statuses must give the same status</li>
         * <li><code>MODIFIED</code> + any status must give <code>MODIFIED</code></li>
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
    }
}
