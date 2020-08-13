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

import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * Implementation of <code>{@link Diff}</code> for maps.
 *
 * @param <M> The type of map.
 * @param <K> The type of map key.
 * @param <V> The type of map value.
 * @author Xavier DURY
 */
public final class MapDiff<M, K, V> extends Diff<M> {

    /**
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

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
        super(status, left, right);
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
     * Return the map of diffs.
     *
     * @return The map of diffs.
     */
    public Map<Diff<K>, Diff<V>> getMap() {
        return map;
    }
}
