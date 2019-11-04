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

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;

/**
 * Implementation of <code>{@link Diff}</code> for maps.
 *
 * @param <M> The type of map.
 * @param <K> The type of map key.
 * @param <V> The type of map value.
 * @author Xavier DURY
 */
public final class MapDiff<M, K, V> extends AbstractMap<Diff<K>, Diff<V>> implements Diff<M> {

    /**
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * The left value.
     */
    private final M left;

    /**
     * The right value.
     */
    private final M right;

    /**
     * The status.
     */
    private final Status status;

    /**
     * The map of diffs.
     */
    private final Map<Diff<K>, Diff<V>> map;

    /**
     * Constructor.
     *
     * @param status The status.
     * @param left   The left value.
     * @param right  The right value.
     * @param map    The map of diffs.
     */
    MapDiff(Status status, M left, M right, Map<? extends Diff<K>, ? extends Diff<V>> map) {
        this.status = requireNonNull(status, "Status cannot be null");
        this.left = left;
        this.right = right;
        this.map = unmodifiableMap(map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> R accept(DiffVisitor<M, R> visitor) {
        return visitor.visitMap(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public M getLeft() {
        return left;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public M getRight() {
        return right;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status getStatus() {
        return status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Entry<Diff<K>, Diff<V>>> entrySet() {
        return map.entrySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "MapDiff{" +
                "left=" + left +
                ", right=" + right +
                ", status=" + status +
                '}';
    }
}
