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

import java.util.Collection;

import static java.util.Objects.requireNonNull;

/**
 * Abstract implementation of <code>{@link Diff}</code>.
 *
 * @param <T> The type of the 2 compared objects.
 * @author Xavier DURY
 */
abstract class AbstractDiff<T> implements Diff<T> {

    private static final long serialVersionUID = 1L;

    private final T left, right;
    private final double normalizedScore;
    private final Status status;

    AbstractDiff(double normalizedScore, Status status, T left, T right) {
        this.normalizedScore = normalizedScore;
        this.status = requireNonNull(status, "Status cannot be null");
        this.left = left;
        this.right = right;
    }

    AbstractDiff(Collection<? extends Diff<?>> diffs, Status status, T left, T right) {
        this(status.isChanged() ? diffs.stream().mapToDouble(Diff::getScore).average().orElse(0.0) : 1.0, status, left, right);
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
    public double getScore() {
        return normalizedScore;
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
        return String.format("%s[status = %s, score = %.3f, left = %s, right = %s]", getClass().getSimpleName(), status, normalizedScore, left, right);
    }
}
