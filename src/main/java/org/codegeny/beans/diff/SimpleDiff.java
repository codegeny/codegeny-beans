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

import static java.util.Objects.requireNonNull;

/**
 * Implementation of <code>{@link Diff}</code> for simple values.
 *
 * @param <T> The type of the 2 compared objects.
 * @author Xavier DURY
 */
public final class SimpleDiff<T> implements Diff<T> {

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
    SimpleDiff(Status status, T left, T right) {
        this.status = requireNonNull(status, "Status cannot be null");
        this.left = left;
        this.right = right;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> R accept(DiffVisitor<T, R> visitor) {
        return visitor.visitSimple(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getLeft() {
        return left;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getRight() {
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
        return "SimpleDiff{" +
                "left=" + left +
                ", right=" + right +
                ", status=" + status +
                '}';
    }
}
