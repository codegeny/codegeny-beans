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
 * Implementation of <code>{@link Diff}</code> for beans.
 *
 * @param <B> The type of bean.
 * @author Xavier DURY
 */
public final class BeanDiff<B> extends AbstractMap<String, Diff<?>> implements Diff<B> {

    /**
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * The left value.
     */
    private final B left;

    /**
     * The right value.
     */
    private final B right;

    /**
     * The status.
     */
    private final Status status;

    /**
     * The map of diffs.
     */
    private final Map<String, Diff<?>> properties;

    /**
     * Constructor.
     *
     * @param status     The status.
     * @param left       The left value.
     * @param right      The right value.
     * @param properties The map of diffs.
     */
    BeanDiff(Status status, B left, B right, Map<String, ? extends Diff<?>> properties) {
        this.status = requireNonNull(status, "Status cannot be null");
        this.left = left;
        this.right = right;
        this.properties = unmodifiableMap(properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> R accept(DiffVisitor<B, R> visitor) {
        return visitor.visitBean(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Entry<String, Diff<?>>> entrySet() {
        return properties.entrySet();
    }

    /**
     * Get the map of diff'ed properties.
     *
     * @return The properties.
     */
    public Map<String, Diff<?>> getProperties() {
        return properties;
    }

    /**
     * Get the diff'ed for the given property name.
     *
     * @param name The property name.
     * @return That property's diff.
     */
    public Diff<?> getProperty(String name) {
        return properties.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public B getLeft() {
        return left;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public B getRight() {
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
    public String toString() {
        return "MapDiff{" +
                "left=" + left +
                ", right=" + right +
                ", status=" + status +
                '}';
    }
}
